package me.ikevoodoo.helix.builtin.commands.repo;

import me.ikevoodoo.helix.BukkitHelixProvider;
import me.ikevoodoo.helix.repo.RepoDownloadCallback;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.concurrent.CompletableFuture;

public class RepositoryCommandShared {

    public static CompletableFuture<File> downloadAsync(String repository, String resource, @Nullable Player player) {
        var plugin = JavaPlugin.getPlugin(BukkitHelixProvider.class);
        var future = new CompletableFuture<File>();

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> future.complete(download(repository, resource, player)));
        return future;
    }

    public static CompletableFuture<File> downloadAsync(String repository, String resource, RepoDownloadCallback callback) {
        var plugin = JavaPlugin.getPlugin(BukkitHelixProvider.class);
        var future = new CompletableFuture<File>();

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> future.complete(download(repository, resource, callback)));
        return future;
    }

    public static File download(String repository, String resource, @Nullable Player player) {
        return download(repository, resource, player != null ? new DownloadCallback(player, resource) : null);
    }

    public static File download(String repository, String resource, RepoDownloadCallback callback) {
        var plugin = JavaPlugin.getPlugin(BukkitHelixProvider.class);
        var repoManager = plugin.getRepositoryManager();

        return repoManager.downloadPlugin(repository, resource, callback);
    }

    private static final class DownloadCallback implements RepoDownloadCallback {
        private final Player player;
        private final String resource;
        private long size;

        private DownloadCallback(Player player, String resource) {
            this.player = player;

            var versionSeparator = resource.indexOf("==");

            this.resource = versionSeparator == -1 ? resource : resource.substring(0, versionSeparator);
        }

        @Override
        public void onSizeReceived(long size) {
            this.size = size;
            this.sendUpdate(0);
        }

        @Override
        public void onProgressUpdate(double progressUpdate) {
            this.sendUpdate(progressUpdate);
        }

        @Override
        public void onFailure() {
            this.player.sendMessage("§cFailed to download the resource §f%s§a!".formatted(this.resource));
        }

        @Override
        public void onSuccess() {
            this.player.sendMessage(
                    "§aSuccessfully downloaded resource §f%s§a! Load it with §f/helix load %s §athen enable it with §f/helix enable %s"
                            .formatted(this.resource, this.resource + ".jar", this.resource)
            );
        }

        private void sendUpdate(double progress) {
            this.player.spigot().sendMessage(
                    ChatMessageType.ACTION_BAR,
                    TextComponent.fromLegacyText(
                            "§eDownloading resource '§f%s§e' §x§3§8§4§3§5§e| §3%.2f MB §x§3§8§4§3§5§e| %s%-3s%%".formatted(
                                    this.resource,
                                    (double) this.size / 1024 / 1024,
                                    progress < 30
                                            ? "§c"
                                            : progress < 75
                                            ? "§e"
                                            : "§a",
                                    (int) Math.round(progress)
                            )
                    )
            );
        }

    }

}
