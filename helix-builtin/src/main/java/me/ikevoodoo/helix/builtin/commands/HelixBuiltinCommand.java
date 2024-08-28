package me.ikevoodoo.helix.builtin.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import me.ikevoodoo.helix.BukkitHelixProvider;
import me.ikevoodoo.helix.api.Helix;
import me.ikevoodoo.helix.api.messages.colors.MinecraftColor;
import me.ikevoodoo.helix.api.commands.CommandExecutionResult;
import me.ikevoodoo.helix.api.commands.HelixCommand;
import me.ikevoodoo.helix.api.commands.HelixCommandParameters;
import me.ikevoodoo.helix.api.commands.arguments.ArgumentList;
import me.ikevoodoo.helix.api.commands.parsers.FileParser;
import me.ikevoodoo.helix.api.commands.parsers.HelixPluginParser;
import me.ikevoodoo.helix.api.commands.parsers.WorldParser;
import me.ikevoodoo.helix.api.config.Configuration;
import me.ikevoodoo.helix.api.logging.HelixLogger;
import me.ikevoodoo.helix.api.logging.LoggerLevel;
import me.ikevoodoo.helix.api.messages.MessageBuilder;
import me.ikevoodoo.helix.api.plugins.HelixPlugin;
import me.ikevoodoo.helix.api.reporting.ErrorType;
import me.ikevoodoo.helix.builtin.commands.items.ItemCommand;
import me.ikevoodoo.helix.builtin.commands.parsers.RepositoryEntryParser;
import me.ikevoodoo.helix.builtin.commands.repo.RepositoryCommandShared;
import me.ikevoodoo.helix.builtin.commands.repo.update.RepoCheckUpdatesCommand;
import me.ikevoodoo.helix.builtin.commands.repo.update.RepoUpdateAllCommand;
import me.ikevoodoo.helix.builtin.commands.repo.update.RepoUpdateCommand;
import me.ikevoodoo.helix.commands.parsers.RepositoryArgumentParser;
import me.ikevoodoo.helix.logging.LoggerColoring;
import me.ikevoodoo.helix.plugins.JavaHelixPlugin;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static me.ikevoodoo.helix.logging.LoggerColoring.chatColor;

public class HelixBuiltinCommand extends HelixCommand {

    private final Configuration repositoryConfig;

    public HelixBuiltinCommand(Configuration repositoryConfig) {
        this.repositoryConfig = repositoryConfig;
    }
    
    @Override
    protected HelixCommandParameters makeParameters() {
        return HelixCommandParameters.create("helix")
                .childCommand(new LoadCommand())
                .childCommand(new UnloadCommand())
                .childCommand(new EnableCommand())
                .childCommand(new DisableCommand())
                .childCommand(new ReloadCommand())
                .childCommand(new AddWorldCommand())
                .childCommand(new RemoveWorldCommand())
                .childCommand(new RepositoryCommand(this.repositoryConfig))
                .childCommand(new ItemCommand())
                .childCommand(new ErrorCommand());
    }

    private static class ErrorCommand extends HelixCommand {
        @Override
        protected HelixCommandParameters makeParameters() {
            return HelixCommandParameters.create("errors");
        }

        @Override
        public CommandExecutionResult handleConsoleSender(@NotNull ConsoleCommandSender sender, @NotNull ArgumentList args) {
            var errors = Helix.errors().getErrors();
            for (var error : errors.entrySet()) {
                var message = error.getKey();
                var type = error.getValue();

                HelixLogger.println(type.getLevel(), message);
            }

            return CommandExecutionResult.HANDLED;
        }

        @Override
        public CommandExecutionResult handlePlayerSender(@NotNull Player player, @NotNull ArgumentList args) {
            var errors = Helix.errors().getErrors();
            for (var error : errors.entrySet()) {
                var message = error.getKey();
                var type = error.getValue();

                player.sendMessage(type.getLevel().getPlayerText() + LoggerColoring.toColorCodes(message));
            }

            return CommandExecutionResult.HANDLED;
        }
    }

    private static class LoadCommand extends HelixCommand {

        @Override
        protected HelixCommandParameters makeParameters() {
            return HelixCommandParameters.create("load")
                    .argument("file", new FileParser(Helix.pluginLoader().getPluginFolder(), file -> {
                        var name = file.getName();
                        if(!name.endsWith(".jar")) {
                            return false;
                        }

                        return !Helix.pluginLoader().isLoaded(file);
                    }));
        }

        @Override
        public CommandExecutionResult handleGenericSender(@NotNull CommandSender sender, @NotNull ArgumentList args) {
            var file = args.<File>getArgument("file");

            try {
                var result = Helix.pluginLoader().load(file);

                if (result == null) {
                    sender.sendMessage("§cThe file is not a valid plugin file!");
                    return CommandExecutionResult.FAILURE;
                }

                sender.sendMessage("§aSuccessfully loaded the plugin! Enable it with §f/helix enable " + result.getBukkitPlugin().getName());
                return CommandExecutionResult.HANDLED;
            } catch (InvalidPluginException | InvalidDescriptionException e) {
                sender.sendMessage("§cCould not load plugin: " + e.getMessage());
                return CommandExecutionResult.FAILURE;
            }
        }
    }

    private static class UnloadCommand extends HelixCommand {

        @Override
        protected HelixCommandParameters makeParameters() {
            return HelixCommandParameters.create("unload")
                    .argument("plugin", new HelixPluginParser(HelixPluginParser.LoadedFlag.LOADED));
        }

        @Override
        public CommandExecutionResult handleGenericSender(@NotNull CommandSender sender, @NotNull ArgumentList args) {
            var plugin = args.<HelixPlugin>getArgument("plugin");

            try {
                Helix.pluginLoader().unload(plugin);

                sender.sendMessage("§aSuccessfully unloaded the plugin!");
                return CommandExecutionResult.HANDLED;
            } catch (Throwable e) {
                sender.sendMessage("§cCould not load plugin: " + e.getMessage());
                return CommandExecutionResult.FAILURE;
            }
        }
    }

    private static class EnableCommand extends HelixCommand {

        @Override
        protected HelixCommandParameters makeParameters() {
            return HelixCommandParameters.create("enable")
                    .argument("plugin", new HelixPluginParser(HelixPluginParser.LoadedFlag.DISABLED));
        }

        @Override
        public CommandExecutionResult handleGenericSender(@NotNull CommandSender sender, @NotNull ArgumentList args) {
            var plugin = args.<HelixPlugin>getArgument("plugin");

            try {
                Helix.pluginLoader().enable(plugin);
                sender.sendMessage("§aSuccessfully enabled plugin §f" + plugin.getBukkitPlugin().getName() + "§a!");
                return CommandExecutionResult.HANDLED;
            } catch (Throwable e) {
                sender.sendMessage("§cCould not enable plugin: " + e.getMessage());
                return CommandExecutionResult.FAILURE;
            }
        }
    }

    private static class DisableCommand extends HelixCommand {
        @Override
        protected HelixCommandParameters makeParameters() {
            return HelixCommandParameters.create("disable")
                    .argument("plugin", new HelixPluginParser(HelixPluginParser.LoadedFlag.ENABLED));
        }

        @Override
        public CommandExecutionResult handleGenericSender(@NotNull CommandSender sender, @NotNull ArgumentList args) {
            var plugin = args.<HelixPlugin>getArgument("plugin");

            try {
                Helix.pluginLoader().disable(plugin);
                sender.sendMessage("§aSuccessfully disabled plugin §f" + plugin.getBukkitPlugin().getName() + "§a!");
                return CommandExecutionResult.HANDLED;
            } catch (Throwable e) {
                sender.sendMessage("§cCould not disable plugin: " + e.getMessage());
                return CommandExecutionResult.FAILURE;
            }
        }
    }

    private static class ReloadCommand extends HelixCommand {
        @Override
        protected HelixCommandParameters makeParameters() {
            return HelixCommandParameters.create("reload")
                    .argument("plugin", new HelixPluginParser(HelixPluginParser.LoadedFlag.ENABLED));
        }

        @Override
        public CommandExecutionResult handleGenericSender(@NotNull CommandSender sender, @NotNull ArgumentList args) {
            var plugin = args.<HelixPlugin>getArgument("plugin");

            try {
                var loader = Helix.pluginLoader();

                var errorReporter = Helix.errors();

                errorReporter.beginSession();

                var reloaded = loader.reload(plugin);
                if(reloaded == null) {
                    sender.sendMessage("§cCould not reload the plugin!");
                    return CommandExecutionResult.FAILURE;
                }

                var errors = errorReporter.getErrors();

                errorReporter.endSession();

                if (!errors.isEmpty()) {
                    sender.sendMessage("§cThere have been §f" + errors.size() + " §cerror(s) while reloading the plugin!");

                    var warnings = new ArrayList<String>();

                    var iter = errors.entrySet().iterator();
                    while (iter.hasNext()) {
                        var next = iter.next();
                        if (next.getValue() == ErrorType.WARNING) {
                            warnings.add(next.getKey());
                            iter.remove();
                        }
                    }

                    if (!errors.isEmpty()) {
                        sender.sendMessage("§7====== §f" + errors.size() + " §c§l" + chatColor(ErrorType.ERROR.getName(errors.size())) + " §7======");
                        for (var error : errors.keySet()) {
                            sender.sendMessage("  - " + chatColor(error));
                        }
                    }

                    if (!warnings.isEmpty()) {
                        sender.sendMessage("§e====== §f" + warnings.size() + " §e§l" + chatColor(ErrorType.WARNING.getName(warnings.size())) + " §e======");
                        for (var warning : warnings) {
                            sender.sendMessage("  - " + chatColor(warning));
                        }
                    }

                    return CommandExecutionResult.FAILURE;
                }

                sender.sendMessage("§aSuccessfully reloaded plugin §f" + reloaded.getBukkitPlugin().getName() + "§a!");
                return CommandExecutionResult.HANDLED;
            } catch (Throwable e) {
                sender.sendMessage("§cCould not reload plugin: " + e.getMessage());
                return CommandExecutionResult.FAILURE;
            }
        }
    }

    private static class AddWorldCommand extends HelixCommand {
        @Override
        protected HelixCommandParameters makeParameters() {
            return HelixCommandParameters.create("add-world")
                    .argument("plugin", new HelixPluginParser(HelixPluginParser.LoadedFlag.LOADED))
                    .argument("world", new WorldParser(WorldParser.WorldFlag.DENIED_BY_PLUGIN));
        }

        @Override
        public CommandExecutionResult handleGenericSender(@NotNull CommandSender sender, @NotNull ArgumentList args) {
            var plugin = args.<HelixPlugin>getArgument("plugin");
            var world = args.<World>getArgument("world");

            if (!(plugin instanceof JavaHelixPlugin helixPlugin)) {
                sender.sendMessage("§cUnable to edit that helix plugin!");
                return CommandExecutionResult.FAILURE;
            }

            helixPlugin.addAllowedWorld(world.getUID());
            helixPlugin.saveAllowedWorlds();
            sender.sendMessage("§aSuccessfully added world §f" + world.getName() + "§a to §f" + plugin.getBukkitPlugin().getName());
            return CommandExecutionResult.HANDLED;
        }
    }

    private static class RemoveWorldCommand extends HelixCommand {
        @Override
        protected HelixCommandParameters makeParameters() {
            return HelixCommandParameters.create("remove-world")
                    .argument("plugin", new HelixPluginParser(HelixPluginParser.LoadedFlag.LOADED))
                    .argument("world", new WorldParser(WorldParser.WorldFlag.ALLOWED_BY_PLUGIN));
        }

        @Override
        public CommandExecutionResult handleGenericSender(@NotNull CommandSender sender, @NotNull ArgumentList args) {
            var plugin = args.<HelixPlugin>getArgument("plugin");
            var world = args.<World>getArgument("world");

            if (!(plugin instanceof JavaHelixPlugin helixPlugin)) {
                sender.sendMessage("§cUnable to edit that helix plugin!");
                return CommandExecutionResult.FAILURE;
            }

            helixPlugin.removeAllowedWorld(world.getUID());
            helixPlugin.saveAllowedWorlds();
            sender.sendMessage("§aSuccessfully removed world §f" + world.getName() + "§a from §f" + plugin.getBukkitPlugin().getName());
            return CommandExecutionResult.HANDLED;
        }
    }

    private static class RepositoryCommand extends HelixCommand {

        private final Configuration repositoryConfig;

        private RepositoryCommand(Configuration repositoryConfig) {
            this.repositoryConfig = repositoryConfig;
        }

        @Override
        protected HelixCommandParameters makeParameters() {
            return HelixCommandParameters.create("repo")
                    .childCommand(new DownloadCommand())
                    .childCommand(new SearchCommand())
                    .childCommand(new InfoCommand())
                    .childCommand(new AddRepoCommand(this.repositoryConfig))
                    .childCommand(new RemoveRepoCommand(this.repositoryConfig))
                    .childCommand(new RefreshRepositoriesCommand(this.repositoryConfig))
                    .childCommand(new RepoUpdateCommand())
                    .childCommand(new RepoCheckUpdatesCommand())
                    .childCommand(new RepoUpdateAllCommand());
        }

        @Override
        public CommandExecutionResult handlePlayerSender(@NotNull Player sender, @NotNull ArgumentList args) {
            var plugin = JavaPlugin.getPlugin(BukkitHelixProvider.class);
            var repoManager = plugin.getRepositoryManager();
            var repositories = repoManager.getRepositories();
            if (repositories.isEmpty()) {
                sender.sendMessage("§cNo repositories available.");
                return CommandExecutionResult.HANDLED;
            }

            sender.sendMessage("§a=== LISTING REPOSITORIES ===");
            for (var repo : repositories.entrySet()) {
                sender.sendMessage("§8 - §f%s §7(%s)".formatted(repo.getKey(), repo.getValue().getUrl()));
            }

            return CommandExecutionResult.HANDLED;
        }

        @Override
        public CommandExecutionResult handleConsoleSender(@NotNull ConsoleCommandSender sender, @NotNull ArgumentList args) {
            var plugin = JavaPlugin.getPlugin(BukkitHelixProvider.class);
            var repoManager = plugin.getRepositoryManager();
            var repositories = repoManager.getRepositories();
            if (repositories.isEmpty()) {
                HelixLogger.warning("<light:red>No repositories available.");
                return CommandExecutionResult.HANDLED;
            }

            HelixLogger.info("<green>=== <light:green>LISTING REPOSITORIES <green>===");
            for (var repo : repositories.entrySet()) {
                HelixLogger.info("<gray> - <light:white>%s <light:gray>(%s)", repo.getKey(), repo.getValue().getUrl());
            }

            return CommandExecutionResult.HANDLED;
        }

        private static class AddRepoCommand extends HelixCommand {

            private final Configuration repositoryConfig;

            private AddRepoCommand(Configuration repositoryConfig) {
                this.repositoryConfig = repositoryConfig;
            }

            @Override
            protected HelixCommandParameters makeParameters() {
                return HelixCommandParameters.create("add")
                        .argument("id", StringArgumentType.word())
                        .argument("repository", StringArgumentType.greedyString());
            }

            @Override
            public CommandExecutionResult handlePlayerSender(@NotNull Player player, @NotNull ArgumentList args) {
                var id = args.<String>getArgument("id");
                var repo = args.<String>getArgument("repository");

                saveRepository(id, repo);

                player.sendMessage("§6Successfully added repository: '§a%s §7(%s)§6'. Please refresh your repositories.".formatted(id, repo));
                return CommandExecutionResult.HANDLED;
            }

            @Override
            public CommandExecutionResult handleConsoleSender(@NotNull ConsoleCommandSender sender, @NotNull ArgumentList args) {
                var id = args.<String>getArgument("id");
                var repo = args.<String>getArgument("repository");

                saveRepository(id, repo);

                HelixLogger.ok("Successfully added repository: '<light:green>%s <light:gray>(%s)<reset>'. Please refresh your repositories.", id, repo);
                return CommandExecutionResult.HANDLED;
            }

            private void saveRepository(String id, String repo) {
                var repos = this.repositoryConfig.<String>getMap("repositories");
                repos.put(id, repo);

                this.repositoryConfig.save();
            }

        }

        private static class RemoveRepoCommand extends HelixCommand {

            private final Configuration repositoryConfig;

            private RemoveRepoCommand(Configuration repositoryConfig) {
                this.repositoryConfig = repositoryConfig;
            }

            @Override
            protected HelixCommandParameters makeParameters() {
                return HelixCommandParameters.create("remove")
                        .argument("repository", RepositoryArgumentParser.INSTANCE);
            }

            @Override
            public CommandExecutionResult handlePlayerSender(@NotNull Player player, @NotNull ArgumentList args) {
                var repo = removeRepository(args);

                player.sendMessage("§aSuccessfully removed repository: '§f%s§a'. Please refresh your repositories.".formatted(repo));
                return CommandExecutionResult.HANDLED;
            }

            @Override
            public CommandExecutionResult handleConsoleSender(@NotNull ConsoleCommandSender sender, @NotNull ArgumentList args) {
                var repo = removeRepository(args);

                HelixLogger.ok("Successfully removed repository: '<light:green>%s<reset>'. Please refresh your repositories.".formatted(repo));
                return CommandExecutionResult.HANDLED;
            }

            private String removeRepository(@NotNull ArgumentList args) {
                var repo = args.<String>getArgument("repository");

                var repos = this.repositoryConfig.<String>getMap("repositories");
                repos.remove(repo);

                this.repositoryConfig.save();
                return repo;
            }

        }

        private static class RefreshRepositoriesCommand extends HelixCommand {

            private final Configuration repositoryConfig;

            private RefreshRepositoriesCommand(Configuration repositoryConfig) {
                this.repositoryConfig = repositoryConfig;
            }

            @Override
            protected HelixCommandParameters makeParameters() {
                return HelixCommandParameters.create("refresh");
            }

            @Override
            public CommandExecutionResult handlePlayerSender(@NotNull Player player, @NotNull ArgumentList args) {
                var result = refreshRepositories();
                if (!result) {
                    player.sendMessage("§cUnable to refresh repositories.");
                    return CommandExecutionResult.FAILURE;
                }

                player.sendMessage("§aSuccessfully refreshed repositories.");
                return CommandExecutionResult.HANDLED;
            }

            @Override
            public CommandExecutionResult handleConsoleSender(@NotNull ConsoleCommandSender sender, @NotNull ArgumentList args) {
                var result = refreshRepositories();
                if (!result) {
                    HelixLogger.error("Unable to refresh repositories.");
                    return CommandExecutionResult.FAILURE;
                }

                HelixLogger.ok("Successfully refreshed repositories.");
                return CommandExecutionResult.HANDLED;
            }

            private boolean refreshRepositories() {
                var loaded = this.repositoryConfig.load();
                if (!loaded) {
                    return false;
                }

                var plugin = JavaPlugin.getPlugin(BukkitHelixProvider.class);
                var repoManager = plugin.getRepositoryManager();

                repoManager.cleanRepositories();
                repoManager.addRepositories(this.repositoryConfig.getMap("repositories"));

                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> repoManager.refreshListings());
                return true;
            }
        }

        private static class DownloadCommand extends HelixCommand {

            @Override
            protected HelixCommandParameters makeParameters() {
                return HelixCommandParameters.create("download")
                        .argument("repository", RepositoryArgumentParser.INSTANCE)
                        .argument("resource", RepositoryEntryParser.INSTANCE);
            }

            @Override
            public CommandExecutionResult handleGenericSender(@NotNull CommandSender sender, @NotNull ArgumentList args) {
                var repository = args.<String>getArgument("repository");
                var resource = args.<String>getArgument("resource");

                RepositoryCommandShared.downloadAsync(repository, resource, sender instanceof Player player ? player : null);
                return CommandExecutionResult.HANDLED;
            }
        }

        private static class SearchCommand extends HelixCommand {
            @Override
            protected HelixCommandParameters makeParameters() {
                return HelixCommandParameters.create("search")
                        .argument("resource", RepositoryEntryParser.INSTANCE);
            }

            @Override
            public CommandExecutionResult handlePlayerSender(@NotNull Player player, @NotNull ArgumentList args) {
                var resource = args.<String>getArgument("resource");
                var plugin = JavaPlugin.getPlugin(BukkitHelixProvider.class);
                var repoManager = plugin.getRepositoryManager();

                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                    repoManager.refreshListings(); // Let's refresh the listings

                    var result = repoManager.searchRepositories(resource);

                    var printedAny = false;

                    for (var entry : result.entrySet()) {
                        if (entry == null) continue;

                        var res = entry.getValue();
                        if (res.entries().isEmpty()) continue;

                        var highest = (double) res.bestScore();
                        if (highest == 0) continue;

                        for (var val : res.entries().entrySet()) {
                            var perc = val.getValue() / highest;
                            if (perc <= 0.6) {
                                continue;
                            }

                            var en = val.getKey();
                            if (!en.isSupported()) continue;

                            var detailHover =  new Text(
                                    TextComponent.fromLegacyText(
                                            "§7Authors: §6" + en.authors() + "\n" +
                                                    "§7ID: §f" + en.resourceId()
                                    )
                            );

                            var header = new MessageBuilder()
                                    .literal("--> ")
                                    .color(MinecraftColor.DARK_GRAY)

                                    .literal(en.displayName())
                                    .color(MinecraftColor.GREEN)
                                    .hover(HoverEvent.Action.SHOW_TEXT, detailHover)

                                    .literal(" ")

                                    .literal(en.highestSupportedVersion().toString())
                                    .color(MinecraftColor.GRAY)
                                    .buildArray();

                            var desc = en.description();
                            var description = desc.substring(0, Math.min(desc.length(), 97));
                            if (description.length() < desc.length()) {
                                description += "...";
                            }
                            var body = new TextComponent(description);

                            var cmdlet = "/helix repo download %s %s".formatted(entry.getKey(), en.resourceId());
                            var infolet = "/helix repo info %s %s".formatted(entry.getKey(), en.resourceId());

                            var buttons = new MessageBuilder()
                                    .literal("[DOWNLOAD]")
                                    .color(MinecraftColor.fromHex("#3a96dd"))

                                    .click(ClickEvent.Action.RUN_COMMAND, cmdlet)

                                    .hover(
                                            HoverEvent.Action.SHOW_TEXT,
                                            new Text(TextComponent.fromLegacyText("§7Download with §f%s".formatted(cmdlet)))
                                    )

                                    .literal(" ")

                                    .literal("[INFO]")
                                    .color(MinecraftColor.YELLOW)
                                    .click(ClickEvent.Action.RUN_COMMAND, infolet)
                                    .hover(
                                            HoverEvent.Action.SHOW_TEXT,
                                            new Text(TextComponent.fromLegacyText("§fClick to view details!"))
                                    ).buildArray();

                            player.spigot().sendMessage(header);
                            player.spigot().sendMessage(body);
                            player.spigot().sendMessage(buttons);
                        }
                    }

                    if (!printedAny) {
                        player.sendMessage("§cNo results matched your query.");
                    }
                });

                return CommandExecutionResult.HANDLED;
            }

            @Override
            public CommandExecutionResult handleConsoleSender(@NotNull ConsoleCommandSender sender, @NotNull ArgumentList args) {
                var resource = args.<String>getArgument("resource");
                var plugin = JavaPlugin.getPlugin(BukkitHelixProvider.class);
                var repoManager = plugin.getRepositoryManager();

                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                    HelixLogger.print(LoggerLevel.INFO, "<yellow>Refreshing repository listings...");
                    repoManager.refreshListings(); // Let's refresh the listings
                    HelixLogger.ok("<yellow>Refreshed repository listings! ");
                    HelixLogger.info("<yellow>Searching for query: <white>%s", resource);

                    var result = repoManager.searchRepositories(resource);

                    var printedAny = false;

                    for (var entry : result.entrySet()) {
                        if (entry == null) continue;

                        var res = entry.getValue();
                        if (res.entries().isEmpty()) continue;

                        var highest = (double) res.bestScore();
                        if (highest == 0) continue;

                        // beauty, not needed really
                        var passedPercTest = false;

                        for (var val : res.entries().entrySet()) {
                            var perc = val.getValue() / highest;
                            if (perc <= 0.6) {
                                continue;
                            }

                            var en = val.getKey();
                            if (!en.isSupported()) continue;

                            if (!passedPercTest) {
                                HelixLogger.info("<light:gray>--> <white>%s", entry.getKey());
                                passedPercTest = true;
                            }

                            printedAny = true;

                            var displayPerc = (int) Math.round(perc * 100);

                            HelixLogger.info(
                                    "<lighter:gray>%3d%% <light:gray>| <light:green>%s <light:gray>v<rgb:110:97:135>%s <light:gray>by <yellow>%s <light:gray>(%s)",
                                    displayPerc,
                                    en.displayName(),
                                    en.highestSupportedVersion().toString(),
                                    en.authorString(),
                                    en.resourceId()
                            );
                        }
                    }

                    if (!printedAny) {
                        HelixLogger.warning("<light:red>No results matched your query.");
                    }
                });

                return CommandExecutionResult.HANDLED;
            }
        }

        private static class InfoCommand extends HelixCommand {
            @Override
            protected HelixCommandParameters makeParameters() {
                return HelixCommandParameters.create("info")
                        .argument("repository", RepositoryArgumentParser.INSTANCE)
                        .argument("resource", RepositoryEntryParser.INSTANCE);
            }

            @Override
            public CommandExecutionResult handlePlayerSender(@NotNull Player player, @NotNull ArgumentList args) {
                var repoId = args.<String>getArgument("repository");
                var resourceId = args.<String>getArgument("resource");
                var plugin = JavaPlugin.getPlugin(BukkitHelixProvider.class);
                var repoManager = plugin.getRepositoryManager();

                var repo = repoManager.getRepositories().get(repoId);
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                    try {
                        var resource = repo.getEntry(resourceId, true);
                        if (resource == null) {
                            player.sendMessage("§cUnknown resource with resourceId '§f%s§c'.".formatted(resourceId));
                            return;
                        }

                        var ver = resource.highestSupportedVersion();

                        player.sendMessage(
                                "§a%s §8v§x§6§e§6§1§8§7%s §8by §6%s §8(%s§8)".formatted(
                                    resource.displayName(),
                                    ver.toString(),
                                    resource.authorString(),
                                    resource.resourceId())
                        );
                        player.sendMessage("§7Created at: §f%s".formatted(resource.firstReleaseDate()));
                        player.sendMessage("§7Updated at: §f%s".formatted(resource.lastReleaseDate()));

                        var supported = resource.isSupported();
                        var supportedMessage = supported ? "§aSupported" : "§cUnsupported";


                        var latestSupported = resource.versions().supportedVersions().get(ver);
                        player.sendMessage("§7Supports versions: §f%s §8(%s§8)".formatted(latestSupported, supportedMessage));
                        player.sendMessage(resource.description());
                    } catch (IOException e) {
                        player.sendMessage("§cUnable to get info for resource '§f%s§c'.".formatted(resourceId));
                        player.sendMessage("§ccMessage: %s".formatted(e.getMessage()));

                        Helix.errors().reportError("Unable to fetch info for " + resourceId, ErrorType.WARNING);
                    }
                });
                return CommandExecutionResult.HANDLED;
            }

            @Override
            public CommandExecutionResult handleConsoleSender(@NotNull ConsoleCommandSender sender, @NotNull ArgumentList args) {
                var repoId = args.<String>getArgument("repository");
                var resourceId = args.<String>getArgument("resource");
                var plugin = JavaPlugin.getPlugin(BukkitHelixProvider.class);
                var repoManager = plugin.getRepositoryManager();

                var repo = repoManager.getRepositories().get(repoId);
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                    try {
                        var resource = repo.getEntry(resourceId, true);
                        if (resource == null) {
                            HelixLogger.warning("<light:red>Unknown resource with resourceId '<white>%s<light:red>'.", resourceId);
                            return;
                        }

                        HelixLogger.info(
                                "<light:green>%s <light:gray>v<rgb:110:97:135>%s <light:gray>by <yellow>%s <light:gray>(%s)",
                                resource.displayName(),
                                resource.highestSupportedVersion(),
                                resource.authorString(),
                                resource.resourceId()
                        );
                        HelixLogger.info("<light:gray>Created at: <white>%s", resource.firstReleaseDate());
                        HelixLogger.info("<light:gray>Updated at: <white>%s", resource.lastReleaseDate());

                        var supported = resource.isSupported();
                        var supportedMessage = supported ? "<light:green>Supported" : "<light:red>Unsupported";

                        HelixLogger.info("<light:gray>Supports versions: <white>%s <light:gray>(%s<light:gray>)", /*resource.supportedVersion()*/null, supportedMessage); // TODO
                        HelixLogger.info(resource.description());
                    } catch (IOException e) {
                        HelixLogger.warning("<light:red>Unable to get info for resource '<white>%s<light:red>'.", resourceId);
                        HelixLogger.warning("<light:red>Message: %s", e.getMessage());

                        Helix.errors().reportError("Unable to fetch info for " + resourceId, ErrorType.WARNING);
                    }
                });


                return CommandExecutionResult.HANDLED;
            }
        }
    }
}
