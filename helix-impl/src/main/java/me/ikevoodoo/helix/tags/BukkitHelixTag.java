package me.ikevoodoo.helix.tags;

import me.ikevoodoo.helix.BukkitHelixProvider;
import me.ikevoodoo.helix.api.Helix;
import me.ikevoodoo.helix.api.namespaced.UniqueIdentifier;
import me.ikevoodoo.helix.api.storage.HelixDataStorage;
import me.ikevoodoo.helix.api.tags.HelixTag;
import me.ikevoodoo.helix.api.tags.behaviors.HelixTagContext;
import me.ikevoodoo.helix.api.tags.behaviors.HelixTagHandler;
import me.ikevoodoo.helix.api.tags.behaviors.TagBehavior;
import me.ikevoodoo.helix.api.tags.behaviors.TagBehaviors;
import me.ikevoodoo.helix.api.tags.behaviors.add.AddTagContext;
import me.ikevoodoo.helix.api.tags.behaviors.remove.RemoveTagContext;
import me.ikevoodoo.helix.utils.DatabaseUtils;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class BukkitHelixTag implements HelixTag {

    private final PreparedStatement addStatement;
    private final PreparedStatement removeStatement;
    private final PreparedStatement clearStatement;

    private final Map<UUID, BukkitHelixTagStorage> entries = new HashMap<>();
    private final Set<UUID> entryView = Collections.unmodifiableSet(this.entries.keySet());

    private final Map<UniqueIdentifier, TagListener<?>> listeners = new HashMap<>();

    public BukkitHelixTag(String id) {
        var tagFile = getFile(id);

        var load = tagFile.exists();

        var connection = DatabaseUtils.makeFileConnection(tagFile);
        if (connection == null) {
            throw new RuntimeException("Unable to create connection");
        }

        try {
            var createTable = connection.prepareStatement("CREATE TABLE IF NOT EXISTS ids (id_least BIGINT, id_most BIGINT, data BLOB)");
            createTable.execute();

            if (load) {
                var selectAll = connection.prepareStatement("SELECT * FROM ids");
                var res = selectAll.executeQuery();

                while (res.next()) {
                    var uuid = new UUID(res.getLong("id_least"), res.getLong("id_most"));

                    var arr = res.getBlob("data").getBinaryStream();

                    var storage = new BukkitHelixTagStorage();
                    storage.fromBytes(arr);
                    this.entries.put(uuid, storage);
                }
            }

            this.addStatement = connection.prepareStatement("INSERT INTO ids (id_least, id_most, data) VALUES (?, ?, ?)");
            this.removeStatement = connection.prepareStatement("DELETE FROM ids WHERE id_least = ? AND id_most = ?");
            this.clearStatement = connection.prepareStatement("DELETE FROM ids");
        } catch (SQLException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static @NotNull File getFile(String id) {
        var dataFolder = ((BukkitHelixProvider) Helix.provider()).getDataFolder();
        var parentFolder = new File(dataFolder, "tags");

        if (!parentFolder.exists() && !parentFolder.mkdirs()) {
            throw new RuntimeException("Unable to create tag folder");
        }

        var tagFolder = new File(parentFolder, id);
        if (!tagFolder.exists() && !tagFolder.mkdirs()) {
            throw new RuntimeException("Unable to create tag folder");
        }

        return new File(tagFolder, "data.db");
    }

    @Override
    public void add(UUID uuid, BiConsumer<UUID, HelixDataStorage> storage) {
        var tagStorage = new BukkitHelixTagStorage();
        storage.accept(uuid, tagStorage);
        this.entries.put(uuid, tagStorage);

        this.save(uuid);

        var player = Helix.players().getOnline(uuid);
        var world = player == null ? null : player.getWorld();
        this.fire(TagBehaviors.ADD, new AddTagContext(world, uuid));
    }

    @Override
    public void remove(UUID uuid) {
        this.delete(uuid);

        this.entries.remove(uuid);
    }

    @Override
    public boolean has(UUID uuid) {
        return this.entries.containsKey(uuid);
    }

    @Override
    public <T extends HelixTagContext> void on(TagBehavior<T> behavior, UniqueIdentifier id, HelixTagHandler<T> handler) {
        this.listeners.put(id, new TagListener<>(behavior, handler));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends HelixTagContext> void fire(TagBehavior<T> behavior, T context) {
        for (var iterator = this.entries.entrySet().iterator(); iterator.hasNext(); ) {
            var targetEntry = iterator.next();
            var targetId = targetEntry.getKey();
            var targetStorage = targetEntry.getValue();

            targetStorage.unfreeze();
            targetStorage.setEdited(false);

            var instance = new BukkitHelixTagInstance(targetId);

            for (var entry : this.listeners.entrySet()) {
                if (instance.isRemoved()) {
                    break;
                }

                TagListener<T> listener;

                try {
                    listener = (TagListener<T>) entry.getValue();
                } catch (ClassCastException ignored) {
                    continue;
                }

                if (listener.behavior != behavior) {
                    continue;
                }

                var id = entry.getKey();
                var plugin = id.namespaceAsPlugin();

                var world = context.world();
                if (plugin != null && world != null && !plugin.isWorldAllowed(world.getUID())) {
                    continue;
                }

                listener.handler.handle(context, targetStorage, instance);
            }

            if (instance.isRemoved() && behavior != TagBehaviors.REMOVE) {
                this.delete(targetId);
                iterator.remove();
                continue;
            }

            if (targetStorage.isEdited() && behavior != TagBehaviors.REMOVE) {
                this.save(targetId);
            }

            targetStorage.freeze();
        }
    }

    @Override
    public HelixDataStorage getData(UUID uuid) {
        var storage = this.entries.get(uuid);
        if (storage == null) return null;

        storage.freeze();

        return storage;
    }

    @Override
    public void editData(UUID uuid, Consumer<HelixDataStorage> consumer) {
        var storage = this.entries.get(uuid);
        if (storage == null) return;

        storage.unfreeze();
        storage.setEdited(false);

        consumer.accept(storage);

        if (storage.isEdited()) {
            this.save(uuid);
        }

        storage.freeze();
    }

    private void save(UUID uuid) {
        Helix.scheduler().async(() -> {
            try {
                this.addStatement.setLong(1, uuid.getLeastSignificantBits());
                this.addStatement.setLong(2, uuid.getMostSignificantBits());

                var baos = new ByteArrayOutputStream();
                var entry = this.entries.get(uuid);
                entry.toBytes(baos);

                var bain = new ByteArrayInputStream(baos.toByteArray());

                this.addStatement.setBlob(3, bain);
                this.addStatement.executeUpdate();
            } catch (SQLException | IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void delete(UUID uuid) {
        var player = Helix.players().getOnline(uuid);
        var world = player == null ? null : player.getWorld();
        this.fire(TagBehaviors.REMOVE, new RemoveTagContext(world, uuid));

        Helix.scheduler().async(() -> {
            try {
                this.removeStatement.setLong(1, uuid.getLeastSignificantBits());
                this.removeStatement.setLong(2, uuid.getMostSignificantBits());
                this.removeStatement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public Set<UUID> listAll() {
        return this.entryView;
    }

    @Override
    public void clearAll() {
        Helix.scheduler().async(() -> {
            try {
                this.clearStatement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private record TagListener<T extends HelixTagContext>(TagBehavior<T> behavior, HelixTagHandler<T> handler) {}
}
