package me.ikevoodoo.helix.builtin.updates.check;

import me.ikevoodoo.helix.BukkitHelixProvider;
import me.ikevoodoo.helix.api.Helix;
import me.ikevoodoo.helix.api.semver.Version;
import me.ikevoodoo.helix.api.semver.VersionCompareOperator;
import me.ikevoodoo.helix.api.types.Some;
import me.ikevoodoo.helix.api.types.Tuple;
import me.ikevoodoo.helix.builtin.commands.repo.update.utils.EntryUpdateData;
import me.ikevoodoo.helix.builtin.updates.UpdateList;
import me.ikevoodoo.helix.repo.PluginRepository;
import org.bukkit.plugin.java.JavaPlugin;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

public class UpdateRetriever {

    public static Some<Throwable, Tuple<Integer, UpdateList>> queryAvailable() {
        try {
            var plugins = Helix.pluginLoader().getPlugins();

            var helixPlugin = JavaPlugin.getPlugin(BukkitHelixProvider.class);
            var repoManager = helixPlugin.getRepositoryManager();

            var tasks = new ArrayList<CompletableFuture<Some<String, EntryUpdateData>>>();

            var highestKeyLength = 0;

            for (var entry : plugins.entrySet()) {
                var key = entry.getKey();
                var plugin = entry.getValue();

                var repo = repoManager.getRepositories().get(plugin.getOriginRepository());
                if (repo == null) continue;

                if (key.length() > highestKeyLength) {
                    highestKeyLength = key.length();
                }

                var id = plugin.getResourceId();
                var version = plugin.getPluginVersion() == null ? Version.of(0, 0, 0) : plugin.getPluginVersion();

                tasks.add(createCheckFuture(repo, id, key, version));
            }

            var central = repoManager.getRepositories().get("central");
            if (central != null /*&& central.getUrl().equals("central.repo.helix.refinedtech.dev")*/) {
                tasks.add(createCheckFuture(central, "helix", "helix", Helix.version()));
            }

            var results = new ArrayList<Some<String, EntryUpdateData>>();
            for (var task : tasks) {
                results.add(task.get());
            }

            var updateList = new UpdateList(results);

            return Some.value(Tuple.of(highestKeyLength, updateList));
        } catch (Throwable th) {
            th.printStackTrace(System.err);
            return Some.error(th);
        }
    }

    private static CompletableFuture<Some<String, EntryUpdateData>> createCheckFuture(PluginRepository repo, String id, String key, Version version) {
        var future = new CompletableFuture<Some<String, EntryUpdateData>>();
        CompletableFuture.runAsync(() -> future.complete(checkUpdate(repo, id, key, version)));
        return future;
    }

    private static Some<String, EntryUpdateData> checkUpdate(PluginRepository repo, String id, String key, Version version) {
        try {
            var latest = repo.getEntry(id, true);
            if (latest == null) {
                return Some.error("<light:red>Unable to update <light:yellow>" + key + " <light:red>as it's origin is unreachable.");
            }

            var highestSupported = latest.highestSupportedVersion();

            if (highestSupported != null && VersionCompareOperator.GREATER_THAN.compare(highestSupported, version)) {
                return Some.value(new EntryUpdateData(
                        key,
                        repo.getId(),
                        id,
                        highestSupported,
                        latest.resourceSize()
                ));
            }

            return Some.empty();
        } catch (ConnectException exception) {
            return Some.error("<light:red>Plugin repository <light:yellow>" + repo.getId() + " <light:red>is offline!");
        } catch (Throwable e) {
            e.printStackTrace(System.out);
            return Some.error(e.getLocalizedMessage());
        }
    }

}
