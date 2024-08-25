package me.ikevoodoo.helix;

import com.github.zafarkhaja.semver.Version;
import me.ikevoodoo.helix.api.Helix;
import me.ikevoodoo.helix.api.HelixProvider;
import me.ikevoodoo.helix.api.commands.HelixCommandRegistry;
import me.ikevoodoo.helix.api.config.HelixConfigurationProvider;
import me.ikevoodoo.helix.api.events.HelixEventRegistry;
import me.ikevoodoo.helix.api.helper.FileHelper;
import me.ikevoodoo.helix.api.items.HelixItemRegistry;
import me.ikevoodoo.helix.api.logging.HelixLogger;
import me.ikevoodoo.helix.api.logging.LoggerLevel;
import me.ikevoodoo.helix.api.namespaced.UniqueIdentifier;
import me.ikevoodoo.helix.api.players.HelixPlayerManager;
import me.ikevoodoo.helix.api.plugins.loading.HelixPluginLoader;
import me.ikevoodoo.helix.api.reporting.ErrorType;
import me.ikevoodoo.helix.api.scheduling.HelixScheduler;
import me.ikevoodoo.helix.api.screens.HelixScreenRegistry;
import me.ikevoodoo.helix.api.tags.HelixTagManager;
import me.ikevoodoo.helix.api.words.HelixWorldManager;
import me.ikevoodoo.helix.commands.JavaHelixCommandRegistry;
import me.ikevoodoo.helix.config.ConfigProvider;
import me.ikevoodoo.helix.events.ListenerRegistry;
import me.ikevoodoo.helix.items.JavaHelixItemRegistry;
import me.ikevoodoo.helix.listeners.CommandListener;
import me.ikevoodoo.helix.listeners.InventoryListener;
import me.ikevoodoo.helix.listeners.ItemUseListener;
import me.ikevoodoo.helix.listeners.PlayerConnectionListener;
import me.ikevoodoo.helix.listeners.PlayerMoveWorldsListener;
import me.ikevoodoo.helix.listeners.WorldLoadListener;
import me.ikevoodoo.helix.listeners.custom.PlayerDamageListener;
import me.ikevoodoo.helix.logging.HelixPluginLogger;
import me.ikevoodoo.helix.players.BukkitHelixPlayerManager;
import me.ikevoodoo.helix.plugins.loading.JavaHelixPluginLoader;
import me.ikevoodoo.helix.repo.PluginRepositoryManager;
import me.ikevoodoo.helix.reporting.BukkitHelixErrorReporter;
import me.ikevoodoo.helix.scheduling.BukkitHelixScheduler;
import me.ikevoodoo.helix.screens.HelixScreenRegistryImpl;
import me.ikevoodoo.helix.tags.BukkitHelixTagManager;
import me.ikevoodoo.helix.worlds.BukkitHelixWorldManager;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Properties;

public final class BukkitHelixProvider extends JavaPlugin implements HelixProvider {

    private final JavaHelixPluginLoader helixPluginLoader = new JavaHelixPluginLoader(
            new File(getDataFolder(), "plugins")
    );
    private final ListenerRegistry listenerRegistry = new ListenerRegistry();
    private final HelixItemRegistry helixItemRegistry = new JavaHelixItemRegistry(
            UniqueIdentifier.helix("variables"),
            UniqueIdentifier.helix("item_id")
    );
    private final JavaHelixCommandRegistry helixCommandRegistry = new JavaHelixCommandRegistry();
    private final PluginRepositoryManager repositoryManager = new PluginRepositoryManager(
            this.helixPluginLoader.getPluginFolder()
    );
    private final BukkitHelixErrorReporter errorReporter = new BukkitHelixErrorReporter();
    private final ConfigProvider configManager = new ConfigProvider();
    private final BukkitHelixScheduler scheduler = new BukkitHelixScheduler();
    private final HelixScreenRegistry screenRegistry = new HelixScreenRegistryImpl();
    private final HelixWorldManager worldManager = new BukkitHelixWorldManager();
    private final HelixPlayerManager playerManager = new BukkitHelixPlayerManager();
    private final HelixTagManager tagManager = new BukkitHelixTagManager();
    private final Version VERSION;

    private Path builtinFile;

    public BukkitHelixProvider() {
        var props = new Properties();
        try {
            props.load(BukkitHelixProvider.class.getResourceAsStream("/version.properties"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        VERSION = Version.parse(props.getProperty("VERSION", "0.0.1"));
    }

    @Override
    public void onLoad() {
        new HelixPluginLogger();

        Helix.setHelixProvider(this);
    }

    @Override
    public void onEnable() {
        if(!getDataFolder().isDirectory() && !getDataFolder().mkdir()) {
            throw new IllegalStateException("Helix could not start as it was unable to create it's data folder!");
        }

        this.errorReporter.beginSession();
        HelixLogger.printCaret(true);

        boolean isPaper = false;
        try {
            Class.forName("com.destroystokyo.paper.ParticleBuilder");
            isPaper = true;
        } catch (ClassNotFoundException ignored) {

        }

        if (isPaper) {
            HelixLogger.syncLog(() -> {
                HelixLogger.error("------ PSA! ------");
                HelixLogger.reportError("DO NOT USE PAPER!");
                HelixLogger.error("The plugin has detected the usage of PaperMC (Or forks), please switch away immediately if you care about your players.");
                HelixLogger.error("Paper has no regards for the stability, safety and integrity of your server and WILL break things without asking.");
                HelixLogger.error("Plugins may stop working randomly, may not function or worse, cause game-breaking glitches because of Paper's bad practices.");
                HelixLogger.error("If you truly do care about your server, you are urged to switch to Spigot.");
                HelixLogger.error("Helix will try to load, however it is not guaranteed to work.");
            });
        }

        getServer().getPluginManager().registerEvents(new WorldLoadListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerMoveWorldsListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerDamageListener(), this);
        getServer().getPluginManager().registerEvents(new CommandListener(this.helixCommandRegistry.getDispatcher()), this);
        getServer().getPluginManager().registerEvents(new ItemUseListener(), this);
        getServer().getPluginManager().registerEvents(new InventoryListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerConnectionListener(), this);

        this.bootUp();
    }

    @Override
    public void onDisable() {
        this.unloadAll();

        this.helixPluginLoader.shutdown();

        this.errorReporter.endSession();
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();

        this.unloadAll();
        this.loadPluginsAndRepos();
    }

    // FIXME remove this shit wtf is this shit
    public void movePlugin(Plugin plugin) {
        try {
            var file = new File(plugin.getClass()
                    .getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI());

            this.helixPluginLoader.disablePlugin(plugin);
            this.helixPluginLoader.unloadPlugin(plugin);

            var dir = this.helixPluginLoader.getPluginFolder();
            Files.move(file.toPath(), dir.toPath().resolve(file.getName()), StandardCopyOption.REPLACE_EXISTING);
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public PluginRepositoryManager getRepositoryManager() {
        return repositoryManager;
    }

    private void bootUp() {
        HelixLogger.info("<yellow>Helix is starting, please wait.");

        this.loadPluginsAndRepos();
    }

    private void loadPluginsAndRepos() {
        var repositorySize = this.repositoryManager.getPluginRepositoryCount();
        var repositorySuffix = repositorySize != 1 ? "ies" : "y";
        HelixLogger.info("<yellow>Checking %s plugin repositor%s...", repositorySize, repositorySuffix);

        this.repositoryManager.refreshListings();

        var pluginSize = 0;
        String pluginSuffix = "s";
        var files = this.helixPluginLoader.getPluginFolder().listFiles();

        if (files != null) {
            for (var file : files) {
                if (file.isDirectory()) continue;

                var isJar = FileHelper.isJar(file);
                if (!isJar) continue;

                pluginSize++;
            }

            pluginSuffix = pluginSize != 1 ? "s" : "";
            HelixLogger.info("<yellow>Attempting to load %s plugin%s.", pluginSize, pluginSuffix);
        } else {
            HelixLogger.error("Unable to access plugin files.");
        }

        this.loadAll();

        var newRepoSize = this.repositoryManager.getPluginRepositoryCount();
        repositorySuffix = newRepoSize != 1 ? "ies" : "y";

        var sb = new StringBuilder();
        for (var type : ErrorType.values()) {
            if (type == ErrorType.INTERNAL) continue;

            var count = this.errorReporter.errorCount(type);
            if (count == 0) continue;

            var name = type.getName(count);

            sb.append("<white>%s %s<white>, ".formatted(count, name));
        }
        if (!sb.isEmpty()) {
            sb.setLength(sb.length() - 9);
        }

        HelixLogger.println(
                sb.isEmpty() ? LoggerLevel.OK : LoggerLevel.WARNING,
                "<bold:green>Done, loaded %s plugin%s and %s repositor%s. %s",
                pluginSize,
                pluginSuffix,
                newRepoSize,
                repositorySuffix,
                sb
        );
    }

    private void unloadAll() {
        this.helixPluginLoader.unloadAll();
        this.repositoryManager.cleanRepositories();
        this.errorReporter.getSession().clear();

        try {
            if (this.builtinFile != null) {
                Files.delete(this.builtinFile);
            }
        } catch (IOException e) {
            HelixLogger.error(e);
        }
    }

    private void loadAll() {
        try(var resource = getClass().getResourceAsStream("/builtin.jar")) {
            if (resource == null) return;

            this.builtinFile = this.getDataFolder().toPath().resolve("plugins/builtin.jar");
            var stream = Files.newOutputStream(this.builtinFile, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
            resource.transferTo(stream);
        } catch (IOException e) {
            HelixLogger.reportError(e);
        }

        this.helixPluginLoader.loadAll();
        this.helixPluginLoader.enableAll();
    }

    @Override
    public @NotNull HelixPluginLoader pluginLoader() {
        return this.helixPluginLoader;
    }

    @Override
    public @NotNull HelixEventRegistry events() {
        return this.listenerRegistry;
    }

    @Override
    public @NotNull HelixItemRegistry items() {
        return this.helixItemRegistry;
    }

    @Override
    public @NotNull HelixCommandRegistry commands() {
        return this.helixCommandRegistry;
    }

    @Override
    public @NotNull BukkitHelixErrorReporter errors() {
        return this.errorReporter;
    }

    @Override
    public @NotNull HelixConfigurationProvider config() {
        return this.configManager;
    }

    @Override
    public @NotNull HelixScheduler scheduler() {
        return this.scheduler;
    }

    @Override
    public @NotNull HelixTagManager tags() {
        return this.tagManager;
    }

    @Override
    public @NotNull Version version() {
        return VERSION;
    }

    @Override
    public @NotNull File providerFile() {
        return getFile();
    }

    @Override
    public @NotNull HelixScreenRegistry screens() {
        return this.screenRegistry;
    }

    @Override
    public @NotNull HelixWorldManager worlds() {
        return this.worldManager;
    }

    @Override
    public @NotNull HelixPlayerManager players() {
        return this.playerManager;
    }
}
