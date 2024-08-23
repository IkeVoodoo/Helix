package me.ikevoodoo.helix.listeners;

import me.ikevoodoo.helix.api.Helix;
import me.ikevoodoo.helix.api.tags.behaviors.TagBehaviors;
import me.ikevoodoo.helix.api.tags.behaviors.join.JoinTagContext;
import me.ikevoodoo.helix.api.tags.behaviors.join.async.AsyncJoinTagContext;
import me.ikevoodoo.helix.events.ListenerRegistry;
import me.ikevoodoo.helix.events.wrappers.PlayerJoinWrapperEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerConnectionListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        var player = event.getPlayer();
        var world = player.getWorld();
        var join = new PlayerJoinWrapperEvent(player, event.getJoinMessage(), world.getUID());

        var listenerRegistry = (ListenerRegistry) Helix.events();
        listenerRegistry.broadcast(join);

        Helix.tags().fire(TagBehaviors.JOIN, new JoinTagContext(world, player));
    }

    @EventHandler
    public void onPlayerJoin(AsyncPlayerPreLoginEvent event) {
        var player = event.getUniqueId();

        var ctx = new AsyncJoinTagContext(player, event.getAddress());
        Helix.tags().fire(TagBehaviors.ASYNC_JOIN, ctx);

        if (ctx.getKickMessage() != null) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, ctx.getKickMessage());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent event) {
        var player = event.getPlayer();
        var world = player.getWorld();
        var join = new PlayerJoinWrapperEvent(player, event.getQuitMessage(), world.getUID());

        var listenerRegistry = (ListenerRegistry) Helix.events();
        listenerRegistry.broadcast(join);
    }
}
