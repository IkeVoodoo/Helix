package me.ikevoodoo.helix.api.tags;

import me.ikevoodoo.helix.api.namespaced.UniqueIdentifier;
import me.ikevoodoo.helix.api.storage.HelixDataStorage;
import me.ikevoodoo.helix.api.tags.behaviors.HelixTagContext;
import me.ikevoodoo.helix.api.tags.behaviors.HelixTagHandler;
import me.ikevoodoo.helix.api.tags.behaviors.TagBehavior;

import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface HelixTag {

    void add(UUID uuid, BiConsumer<UUID, HelixDataStorage> storage);

    void remove(UUID uuid);

    boolean has(UUID uuid);

    <T extends HelixTagContext> void on(TagBehavior<T> behavior, UniqueIdentifier id, HelixTagHandler<T> handler);

    <T extends HelixTagContext> void fire(TagBehavior<T> behavior, T context);

    HelixDataStorage getData(UUID uuid);

    void editData(UUID uuid, Consumer<HelixDataStorage> consumer);

    Set<UUID> listAll();

    void clearAll();

}
