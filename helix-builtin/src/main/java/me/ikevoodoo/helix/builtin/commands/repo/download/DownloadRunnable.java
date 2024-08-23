package me.ikevoodoo.helix.builtin.commands.repo.download;

import me.ikevoodoo.helix.api.Helix;
import me.ikevoodoo.helix.builtin.commands.repo.RepositoryCommandShared;
import me.ikevoodoo.helix.builtin.commands.repo.update.utils.EntryUpdateData;
import me.ikevoodoo.helix.repo.RepoDownloadCallback;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;

public record DownloadRunnable(EntryUpdateData data, RepoDownloadCallback callback) implements Runnable {
    @Override
    public void run() {
        if (!data().pluginId().equals("helix")) {
            Helix.scheduler().sync(() -> {
                if (!Helix.pluginLoader().unload(this.data.pluginId())) {
                    throw new IllegalStateException();
                }
            });
        }

        var file = RepositoryCommandShared.download(
                this.data.repository(),
                this.data.resourceId(),
                this.callback
        );

        if (!data().pluginId().equals("helix")) {
            Helix.scheduler().sync(() -> {
                try {
                    Helix.pluginLoader().load(file);
                } catch (InvalidPluginException | InvalidDescriptionException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }
}
