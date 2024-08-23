package me.ikevoodoo.helix.api.plugins;

import com.github.zafarkhaja.semver.Version;
import me.ikevoodoo.helix.api.Helix;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public interface HelixPlugin {

    String getId();

    Set<UUID> getAllowedWorlds();

    Plugin getBukkitPlugin();

    boolean isWorldAllowed(UUID id);

    void addAllowedWorld(UUID id);
    void removeAllowedWorld(UUID id);

    String getOriginRepository();
    String getResourceId();

    File getPluginFile();

    Version getPluginVersion();

    boolean isBuiltin();

    static HelixPlugin getProvidingPlugin(Class<?> clazz) {
        var plugin = JavaPlugin.getProvidingPlugin(clazz);

        return Helix.pluginLoader().getPlugin(plugin.getName().toLowerCase(Locale.ROOT));
    }

}
