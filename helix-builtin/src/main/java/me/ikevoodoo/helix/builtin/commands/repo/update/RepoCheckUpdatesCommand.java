package me.ikevoodoo.helix.builtin.commands.repo.update;

import me.ikevoodoo.helix.api.Helix;
import me.ikevoodoo.helix.api.commands.CommandExecutionResult;
import me.ikevoodoo.helix.api.commands.HelixCommand;
import me.ikevoodoo.helix.api.commands.HelixCommandParameters;
import me.ikevoodoo.helix.api.commands.arguments.ArgumentList;
import me.ikevoodoo.helix.api.logging.HelixLogger;
import me.ikevoodoo.helix.api.messages.MessageBuilder;
import me.ikevoodoo.helix.api.messages.colors.MinecraftColor;
import me.ikevoodoo.helix.builtin.commands.repo.update.utils.RepoUpdateCommons;
import me.ikevoodoo.helix.builtin.updates.check.UpdateRetriever;
import org.bukkit.command.ConsoleCommandSender;
import org.jetbrains.annotations.NotNull;

import static me.ikevoodoo.helix.api.helper.FileHelper.compressedEntrySize;
import static me.ikevoodoo.helix.api.helper.FileHelper.fileSize;
import static me.ikevoodoo.helix.api.helper.StringHelper.trimLengthContinued;

public class RepoCheckUpdatesCommand extends HelixCommand {
    @Override
    protected HelixCommandParameters makeParameters() {
        return HelixCommandParameters.create("check-updates");
    }

    @Override
    public CommandExecutionResult handleConsoleSender(@NotNull ConsoleCommandSender sender, @NotNull ArgumentList args) {
        var plugins = Helix.pluginLoader().getPlugins();

        var resultSome = UpdateRetriever.queryAvailable();
        if (resultSome.hasError()) {
            HelixLogger.error("An error occurred while trying to get the plugin list. Please try again later.");
            HelixLogger.reportError(resultSome.error());
            return CommandExecutionResult.FAILURE;
        }

        var resultTuple = resultSome.value();

        var highestKeyLength = resultTuple.key();
        var results = resultTuple.value();

        if (results.empty()) {
            HelixLogger.info("No plugins could be updated, maybe the repositories are down?");
            return CommandExecutionResult.HANDLED;
        }

        var updateComponents = new MessageBuilder();

        var spaceLength = Math.min(Math.max(highestKeyLength + 5, 10), 15);

        var length = spaceLength * 4;
        var spaceFormat = "%-" + spaceLength + "s";

        updateComponents.literal("=".repeat(length)).literal("\n");
        updateComponents.literal(spaceFormat, "PLUGIN");
        updateComponents.literal(spaceFormat, "VERSION");
        updateComponents.literal(spaceFormat, "REPOSITORY");
        updateComponents.literal(spaceFormat, "SIZE").literal("\n");
        updateComponents.literal("=".repeat(length)).literal("\n");

        var updating = 0;
        var currentSize = 0L;
        var totalSize = 0L;

        for (var entry : results.valid()) {
            var plugin = plugins.get(entry.pluginId());
            var pluginFile = plugin == null ? Helix.providerFile() : plugin.getPluginFile();
            var pluginRepo = plugin == null ? "central" : plugin.getOriginRepository();
            var pluginName = plugin == null ? "helix" : plugin.getBukkitPlugin().getName();

            var version = entry.updatingTo();
            var size = entry.size();
            var sizeString = RepoUpdateCommons.formatSize(size);


            var pluginSize = fileSize(pluginFile);
            currentSize += pluginSize;

            if (plugin != null && pluginSize > 0) {
                var infoSize = compressedEntrySize(pluginFile, "download.info");

                currentSize -= Math.max(infoSize, 0);
            }

            var repo = trimLengthContinued(pluginRepo, spaceLength);
            pluginName = trimLengthContinued(pluginName, spaceLength);

            updateComponents.literal(spaceFormat, pluginName).color(MinecraftColor.GREEN);
            updateComponents.literal(spaceFormat, version.toString());
            updateComponents.literal(spaceFormat, repo);
            updateComponents.literal(spaceFormat, sizeString);
            updateComponents.literal("\n");

            updating++;
            totalSize += size;
        }

        if (updating == 0) {
            sender.sendMessage("Every plugin is up-to-date!");
            return CommandExecutionResult.HANDLED;
        }

        updateComponents.literal("\n");
        updateComponents.literal("Download Summary\n");
        updateComponents.literal("=".repeat(length)).literal("\n");
        updateComponents.literal("Install %3d Plugins\n", updating);
        updateComponents.literal("\n");
        updateComponents.literal("\n");
        updateComponents.literal("Total download size: %s\n", RepoUpdateCommons.formatSize(totalSize));

        var sizeDifference = totalSize - currentSize;

        updateComponents.literal("Additional space required: ");
        if (sizeDifference < 0) {
            updateComponents.literal("-")
                    .color(MinecraftColor.GREEN)
                    .literal(RepoUpdateCommons.formatSize(Math.abs(sizeDifference)))
                    .color(MinecraftColor.GREEN);
        } else if (sizeDifference == 0) {
            updateComponents.literal("0").color(MinecraftColor.YELLOW);
        } else {
            updateComponents.literal("+")
                    .color(MinecraftColor.RED)
                    .literal(RepoUpdateCommons.formatSize(sizeDifference))
                    .color(MinecraftColor.RED);
        }
        updateComponents.literal("\n");

        updateComponents.literal("Run ").color(MinecraftColor.WHITE);
        updateComponents.literal("/helix repo update-all ").color(MinecraftColor.AQUA);
        updateComponents.literal("to update.").color(MinecraftColor.WHITE);

        sender.spigot().sendMessage(updateComponents.buildArray());

        return CommandExecutionResult.HANDLED;
    }

}
