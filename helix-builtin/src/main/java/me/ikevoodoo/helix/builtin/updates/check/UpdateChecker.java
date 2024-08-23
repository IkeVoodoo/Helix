package me.ikevoodoo.helix.builtin.updates.check;

import me.ikevoodoo.helix.api.messages.colors.MinecraftColor;
import me.ikevoodoo.helix.api.messages.MessageBuilder;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.Nullable;

public class UpdateChecker {

    /**
     * Signals that an update is required to any player that has the permission.
     * */
    public static void signalUpdateCheck(@Nullable String permission) {
        var built = createUpdateCheckComponents();
        if (built == null) return;

        for (var player : Bukkit.getOnlinePlayers()) {
            if (permission != null && !player.hasPermission(permission)) {
                continue;
            }

            player.spigot().sendMessage(built);
        }
    }

    public static BaseComponent createUpdateCheckComponents() {
        var result = UpdateRetriever.queryAvailable();
        if (result.hasError()) {
            return null;
        }

        var list = result.value().value();

        var valid = list.valid();
        if (valid.isEmpty()) return null;

        var yesHover = new MessageBuilder()
                .literal("Update ").color(MinecraftColor.GOLD)
                .literal("%3d", valid.size()).color(MinecraftColor.DARK_AQUA)
                .literal(" plugins.").color(MinecraftColor.GOLD)
                .buildArray();

        return new MessageBuilder()
                .literal("--=== HELIX ===--\n")
                .literal("There are ").color(MinecraftColor.GOLD)
                .literal("%3d", valid.size()).color(MinecraftColor.DARK_AQUA)
                .literal(" plugins that need to be updated! Do you wish to update them now?").color(MinecraftColor.GOLD)
                .literal(" [yes]")
                .color(MinecraftColor.GREEN)
                .bold(true)
                .click(ClickEvent.Action.SUGGEST_COMMAND, "/helix repo update-all")
                .hover(HoverEvent.Action.SHOW_TEXT, new Text(yesHover))
                .build();
    }


}
