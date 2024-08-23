package me.ikevoodoo.helix.api.items.display;

import me.ikevoodoo.helix.api.messages.colors.MinecraftColor;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public record ItemTextDisplayData(String displayName, List<String> lore) implements ItemDisplayHolder {

    public ItemTextDisplayData(String displayName) {
        this(displayName, List.of());
    }

    public ItemTextDisplayData(List<String> lore) {
        this(null, lore);
    }

    public ItemTextDisplayData withColoredLore() {
        return new ItemTextDisplayData(
                this.displayName,
                this.lore.stream().map(s -> MinecraftColor.replaceColorCodes('&', s)).toList()
        );
    }

    @Override
    public void apply(ItemMeta meta) {
        if (this.displayName != null) {
            meta.setDisplayName(this.displayName);
        }

        meta.setLore(this.lore);
    }
}
