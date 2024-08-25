package me.ikevoodoo.helix.builtin;

import me.ikevoodoo.helix.BukkitHelixProvider;
import me.ikevoodoo.helix.api.Helix;
import me.ikevoodoo.helix.api.config.Configuration;
import me.ikevoodoo.helix.builtin.commands.HelixBuiltinCommand;
import me.ikevoodoo.helix.builtin.updates.check.UpdateChecker;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class HelixBuiltins extends JavaPlugin {

    private final Configuration repositoryConfig;

    public HelixBuiltins() {
        super();

        this.repositoryConfig = Helix.config().createBuilder()
                .map("repositories", Map.of("central", "central.repository.refined.host:25546"))
                .comment("List of plugin repositories")
                .next()
                .build(new File(getDataFolder(), "repositories.yml"));
    }

    @Override
    public void onEnable() {
        this.repositoryConfig.loadOrCreate();

        Helix.commands().register(this, new HelixBuiltinCommand(this.repositoryConfig));

        ((BukkitHelixProvider) Helix.provider())
                .getRepositoryManager()
                .addRepositories(this.repositoryConfig.getMap("repositories"));

        Helix.scheduler().timer(this::checkForUpdates, 5, TimeUnit.MINUTES);
    }

    private void checkForUpdates() {
        UpdateChecker.signalUpdateCheck("helix.update.check");
    }
}
