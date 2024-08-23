package me.ikevoodoo.helix.listeners;

import me.ikevoodoo.helix.api.Helix;
import me.ikevoodoo.helix.api.screens.ScreenAction;
import me.ikevoodoo.helix.api.screens.SlotPosition;
import me.ikevoodoo.helix.screens.HelixScreenRegistryImpl;
import me.ikevoodoo.helix.screens.components.HelixScreenEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class InventoryListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        var clicked = event.getClickedInventory();
        if (clicked != player.getOpenInventory().getTopInventory()) return;

        var registry = (HelixScreenRegistryImpl) Helix.screens();

        var screen = registry.getOpenPage(player);
        if (screen == null) return;

        var action = ScreenAction.getScreenAction(event);

        var ev = new HelixScreenEvent(
                action,
                player,
                SlotPosition.fromSlot(event.getSlot(), screen.dimensions())
        );

        registry.handleEvent(event.getClickedInventory(), ev);

        if(ev.cancelled()) event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClose(InventoryCloseEvent event) {
        if(!(event.getPlayer() instanceof Player player)) return;

        var registry = (HelixScreenRegistryImpl) Helix.screens();

        registry.closed(player, event.getInventory());
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent event) {
        var player = event.getPlayer();
        var inventory = player.getOpenInventory().getTopInventory();

        var registry = (HelixScreenRegistryImpl) Helix.screens();

        registry.closed(player, inventory);
    }
}
