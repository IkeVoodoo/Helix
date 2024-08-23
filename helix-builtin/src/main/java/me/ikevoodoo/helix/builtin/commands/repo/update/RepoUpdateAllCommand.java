package me.ikevoodoo.helix.builtin.commands.repo.update;

import me.ikevoodoo.helix.api.Helix;
import me.ikevoodoo.helix.api.commands.CommandExecutionResult;
import me.ikevoodoo.helix.api.commands.HelixCommand;
import me.ikevoodoo.helix.api.commands.HelixCommandParameters;
import me.ikevoodoo.helix.api.commands.arguments.ArgumentList;
import me.ikevoodoo.helix.api.logging.HelixLogger;
import me.ikevoodoo.helix.api.logging.LoggerLevel;
import me.ikevoodoo.helix.builtin.commands.repo.download.DownloadRunnable;
import me.ikevoodoo.helix.builtin.commands.repo.update.utils.EntryUpdateData;
import me.ikevoodoo.helix.builtin.updates.check.UpdateRetriever;
import me.ikevoodoo.helix.repo.RepoDownloadCallback;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class RepoUpdateAllCommand extends HelixCommand {
    @Override
    protected HelixCommandParameters makeParameters() {
        return HelixCommandParameters.create("update-all");
    }

    @Override
    public CommandExecutionResult handlePlayerSender(@NotNull Player player, @NotNull ArgumentList args) {
        var resultSome = UpdateRetriever.queryAvailable();
        if (resultSome.hasError()) {
            player.sendMessage("§cAn error occurred while trying to get the plugin list. Please try again later.");
            HelixLogger.reportError(resultSome.error());
            return CommandExecutionResult.FAILURE;
        }

        var resultTuple = resultSome.value();

        var results = resultTuple.value();

        if (results.empty()) {
            player.sendMessage("§eNo plugins could be updated, maybe the repositories are down?");
            return CommandExecutionResult.HANDLED;
        }

        var updating = results.valid().size();

        if (updating == 0) {
            player.sendMessage("§aEvery plugin is up-to-date!");
            return CommandExecutionResult.HANDLED;
        }

        for (var result : results.valid()) {
            if (result.pluginId().equals("helix")) {
                Helix.scheduler().async(new DownloadRunnable(result, new RepoDownloadCallback() {
                    @Override
                    public void onSizeReceived(long size) {

                    }

                    @Override
                    public void onProgressUpdate(double progressUpdate) {

                    }

                    @Override
                    public void onFailure() {
                        player.sendMessage("§cHelix requires to be update but failed to do so, please update helix manually before updating!");
                    }

                    @Override
                    public void onSuccess() {
                        player.sendMessage("§aHelix has been successfully updated! The server will shutdown in 60 seconds. type §3/stop §ato shutdown now.");
                        Helix.scheduler().syncLater(Bukkit::shutdown, 60, TimeUnit.SECONDS);
                    }
                }));
                return CommandExecutionResult.HANDLED;
            }
        }

        var pool = new ForkJoinPool(4);

        var tasks = new ArrayList<Runnable>();
        for (int i = 0; i < results.validSize(); i++) {
            var result = results.valid(i);

            tasks.add(new DownloadRunnable(result, new RepoDownloadCallback() {
                @Override
                public void onSizeReceived(long size) {

                }

                @Override
                public void onProgressUpdate(double progressUpdate) {

                }

                @Override
                public void onFailure() {
                    player.sendMessage("§cFailed to update §3" + result.pluginId());
                }

                @Override
                public void onSuccess() {
                    player.sendMessage("§aSuccessfully updated §3" + result.pluginId());
                }
            }));
        }

        for (var task : tasks) {
            pool.execute(task);
        }

        pool.shutdown();

        return CommandExecutionResult.HANDLED;
    }

    @Override
    public CommandExecutionResult handleConsoleSender(@NotNull ConsoleCommandSender sender, @NotNull ArgumentList args) {
        HelixLogger.print(Level.INFO, "Awaiting information...");

        var resultSome = UpdateRetriever.queryAvailable();
        if (resultSome.hasError()) {
            HelixLogger.error("An error occurred while trying to get the plugin list. Please try again later.");
            HelixLogger.reportError(resultSome.error());
            return CommandExecutionResult.FAILURE;
        }

        var resultTuple = resultSome.value();

        var results = resultTuple.value();

        if (results.empty()) {
            HelixLogger.info("No plugins could be updated, maybe the repositories are down?");
            return CommandExecutionResult.HANDLED;
        }

        var updating = results.valid().size();

        if (updating == 0) {
            HelixLogger.ok("Every plugin is up-to-date!");
            return CommandExecutionResult.HANDLED;
        }

        for (var result : results.valid()) {
            if (result.pluginId().equals("helix")) {
                Helix.scheduler().async(new DownloadRunnable(result, new ConsoleDownloadCallback(result, 0)));
                return CommandExecutionResult.HANDLED;
            }
        }

        var pool = new ForkJoinPool(4);

        var tasks = new ArrayList<Runnable>();
        for (int i = 0; i < results.validSize(); i++) {
            var result = results.valid(i);

            tasks.add(new DownloadRunnable(result, new ConsoleDownloadCallback(result, i)));
        }

        for (var task : tasks) {
            pool.execute(task);
        }

        pool.shutdown();

        return CommandExecutionResult.HANDLED;
    }

    private record ConsoleDownloadCallback(EntryUpdateData updateData, int index) implements RepoDownloadCallback {
        private static final int MAX_LEN = 50;

        private ConsoleDownloadCallback(EntryUpdateData updateData, int index) {
            this.updateData = updateData;
            this.index = index;

            log(() -> HelixLogger.print(LoggerLevel.INFO, "Awaiting information for " + this.updateData.pluginId()));
        }

        @Override
        public void onSizeReceived(long size) {
            printProgress(-1D);
        }

        @Override
        public void onProgressUpdate(double progressUpdate) {
            printProgress(progressUpdate);
        }

        @Override
        public void onFailure() {
            if (this.updateData.pluginId().equals("helix")) {
                HelixLogger.printCaret(true);
                HelixLogger.error("Unable to download helix! Please update helix manually before trying to update other plugins!");
                return;
            }

            log(() -> {
                HelixLogger.clearLine();
                HelixLogger.print(LoggerLevel.ERROR, "Failed to download " + this.updateData.pluginId());
            });
        }

        @Override
        public void onSuccess() {
            if (this.updateData.pluginId().equals("helix")) {
                HelixLogger.printCaret(true);
                HelixLogger.ok("Successfully downloaded helix! The server will shutdown in 60 seconds, you can use /stop to shutdown now.");
                HelixLogger.ok("Once the server is offline simply start it again and helix will be updated!");
                Helix.scheduler().syncLater(Bukkit::shutdown, 60, TimeUnit.SECONDS);
                Helix.scheduler().syncLater(() -> HelixLogger.warning("The server is shutting down in 30 seconds..."), 30, TimeUnit.SECONDS);
                return;
            }

            log(() -> {
                HelixLogger.clearLine();
                HelixLogger.print(LoggerLevel.OK, "Downloaded " + this.updateData.pluginId());
            });
        }

        private void printProgress(double progress) {
            log(() -> {
                var status = progress < 0 ? "Awaiting information..." : "Downloading " + this.updateData.pluginId();

                HelixLogger.print(Level.INFO, status);

                var currProgress = Math.max(progress, 0) / 100D;

                var len = ConsoleDownloadCallback.MAX_LEN - 3;
                var progressLen = (int) Math.floor(len * currProgress);
                var emptyLen = len - progressLen;
                if (emptyLen + progressLen < len) {
                    emptyLen += len - progressLen;
                }

                HelixLogger.print(" [");
                HelixLogger.print("=".repeat(progressLen));
                HelixLogger.print(" ".repeat(emptyLen));
                HelixLogger.print("] ");

                if (progress < 0) {
                    HelixLogger.print("--%%");
                } else {
                    HelixLogger.print("%.2f%%", progress);
                }
            });
        }

        private void log(Runnable runnable) {
            HelixLogger.syncLog(() -> {
                HelixLogger.printCaret(false);
                HelixLogger.down(this.index);

                runnable.run();

                HelixLogger.up(this.index);
                HelixLogger.left(1000);
                HelixLogger.printCaret(true);
            });
        }
    }

}
