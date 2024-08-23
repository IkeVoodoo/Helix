package me.ikevoodoo.helix.builtin.commands.items;

import me.ikevoodoo.helix.api.Helix;
import me.ikevoodoo.helix.api.commands.CommandExecutionResult;
import me.ikevoodoo.helix.api.commands.HelixCommand;
import me.ikevoodoo.helix.api.commands.HelixCommandParameters;
import me.ikevoodoo.helix.api.commands.arguments.ArgumentList;
import me.ikevoodoo.helix.api.commands.parsers.HelixItemParser;
import me.ikevoodoo.helix.api.namespaced.UniqueIdentifier;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class GiveItemCommand extends HelixCommand {
    @Override
    protected HelixCommandParameters makeParameters() {
        return HelixCommandParameters.create("give")
                .argument("item", new HelixItemParser());
    }

    @Override
    public CommandExecutionResult handlePlayerSender(@NotNull Player player, @NotNull ArgumentList args) {
        var item = args.<UniqueIdentifier>getArgument("item");
        var stack = Helix.items().createItem(item, null);

        var result = player.getInventory().addItem(stack);
        if (!result.isEmpty()) {
            player.sendMessage("§cYour inventory is full!");
            return CommandExecutionResult.FAILURE;
        }

        player.sendMessage("§aYou have been given: §r" + Objects.requireNonNull(stack.getItemMeta()).getDisplayName());
        return CommandExecutionResult.SUCCESS;
    }
}
