package me.ikevoodoo.helix.listeners;

import me.ikevoodoo.helix.api.Helix;
import me.ikevoodoo.helix.plugins.JavaHelixPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;

public class WorldLoadListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onWorldLoad(WorldLoadEvent event) {
        var plugins = Helix.pluginLoader().getPlugins();

        for (var plugin : plugins.values()) {
            var loaded = (JavaHelixPlugin) plugin;

            loaded.reloadAllowedWorlds();
        }
    }
}
