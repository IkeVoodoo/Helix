package me.ikevoodoo.helix.builtin.commands.repo.update;

import me.ikevoodoo.helix.BukkitHelixProvider;
import me.ikevoodoo.helix.api.Helix;
import me.ikevoodoo.helix.api.commands.CommandExecutionResult;
import me.ikevoodoo.helix.api.commands.HelixCommand;
import me.ikevoodoo.helix.api.commands.HelixCommandParameters;
import me.ikevoodoo.helix.api.commands.arguments.ArgumentList;
import me.ikevoodoo.helix.api.commands.parsers.HelixPluginParser;
import me.ikevoodoo.helix.api.logging.HelixLogger;
import me.ikevoodoo.helix.api.plugins.HelixPlugin;
import me.ikevoodoo.helix.builtin.commands.repo.RepositoryCommandShared;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.function.Consumer;

import static me.ikevoodoo.helix.logging.LoggerColoring.replaceColoring;
import static me.ikevoodoo.helix.logging.LoggerColoring.toColorCodes;

public class RepoUpdateCommand extends HelixCommand {

    @Override
    protected HelixCommandParameters makeParameters() {
        return HelixCommandParameters.create("update")
                .argument("plugin", new HelixPluginParser(HelixPluginParser.LoadedFlag.LOADED));
    }

    @Override
    public CommandExecutionResult handleGenericSender(@NotNull CommandSender sender, @NotNull ArgumentList args) {
        var plugin = args.<HelixPlugin>getArgument("plugin");
        var version = plugin.getBukkitPlugin().getDescription().getVersion();
        var id = plugin.getResourceId();

        var enabled = plugin.getBukkitPlugin().isEnabled();

        var loader = Helix.pluginLoader();

        var corePlugin = JavaPlugin.getPlugin(BukkitHelixProvider.class);
        var repoManager = corePlugin.getRepositoryManager();

        Consumer<String> success = sender instanceof Player player ? msg -> player.sendMessage(toColorCodes(replaceColoring(msg))) : HelixLogger::info;
        Consumer<String> error = sender instanceof Player player ? msg -> player.sendMessage(toColorCodes(replaceColoring(msg))) : HelixLogger::error;

        var origin = repoManager.getRepositories().get(plugin.getOriginRepository());
        if (origin == null) {
            error.accept("<light:red>Unable to find repository with id '%s'".formatted(id));
            return CommandExecutionResult.FAILURE;
        }

        success.accept("<light:green>Attempting to update plugin: %s".formatted(id));

        loader.unload(plugin);

        Bukkit.getScheduler().runTaskAsynchronously(corePlugin, () -> {
            try {
                if (!origin.ping()) {
                    error.accept("<light:red>The repository from where this plugin was downloaded is currently offline! Please try again later.");
                    return;
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            success.accept("<yellow>Downloading and restarting the plugin...");

            var file = RepositoryCommandShared.download(origin.getId(), id, sender instanceof Player player ? player : null);
            if (file == null) return;

            Bukkit.getScheduler().runTask(corePlugin, () -> {
                try {
                    var loaded = loader.load(file);
                    if (loaded == null) {
                        error.accept("<light:red>Failed to load plugin: %s".formatted(id));
                        return;
                    }

                    success.accept("<yellow>Done! Automatically loaded plugin! It was updated from version <light:red>v" + version + " <yellow>to <light:green>v" + loaded.getBukkitPlugin().getDescription().getVersion());

                    if (enabled) {
                        loader.enable(loaded);
                        success.accept("<yellow>The plugin has been successfully enabled!");
                        return;
                    }

                    success.accept("<yellow>Please enable the plugin with <light:white>/helix enable " + loaded.getId());
                    return;
                } catch (Throwable throwable) {
                    HelixLogger.reportError(throwable);
                }

                error.accept("<light:red>Failed to load plugin! See error reports for more information.");
            });
        });

        return CommandExecutionResult.HANDLED;
    }
}