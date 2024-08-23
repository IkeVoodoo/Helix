package me.ikevoodoo.helix.builtin.commands.items;

import me.ikevoodoo.helix.api.Helix;
import me.ikevoodoo.helix.api.commands.CommandExecutionResult;
import me.ikevoodoo.helix.api.commands.HelixCommand;
import me.ikevoodoo.helix.api.commands.HelixCommandParameters;
import me.ikevoodoo.helix.api.commands.arguments.ArgumentList;
import me.ikevoodoo.helix.api.commands.parsers.HelixItemVariableParser;
import me.ikevoodoo.helix.api.commands.parsers.HelixItemVariableValueParser;
import me.ikevoodoo.helix.api.messages.colors.MinecraftColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SetItemAttributeCommand extends HelixCommand {
    @Override
    protected HelixCommandParameters makeParameters() {
        return HelixCommandParameters.create("set-var")
                .argument("variable", HelixItemVariableParser.INSTANCE)
                .argument("value", HelixItemVariableValueParser.INSTANCE);
    }

    @Override
    public CommandExecutionResult handlePlayerSender(@NotNull Player player, @NotNull ArgumentList args) {
        var item = player.getInventory().getItemInMainHand();

        var instance = Helix.items().getItemFromStack(item);
        if (instance == null) {
            player.sendMessage(MinecraftColor.RED + "You must hold a custom item in your hands!");
            return CommandExecutionResult.FAILURE;
        }

        var variable = args.<String>getArgument("variable");
        var value = args.getArgument("value");
        var valueStr = value.toString();

        value = valueStr;

        var type = instance.getVariables().getType(variable);
        var complex = type.getComplexType();
        if (complex.equals(Byte.class)) {
            value = Byte.parseByte(valueStr);
        }

        if (complex.equals(Short.class)) {
            value = Short.parseShort(valueStr);
        }

        if (complex.equals(Integer.class)) {
            value = Integer.parseInt(valueStr);
        }

        if (complex.equals(Long.class)) {
            value = Long.parseLong(valueStr);
        }

        if (complex.equals(Float.class)) {
            value = Float.parseFloat(valueStr);
        }

        if (complex.equals(Double.class)) {
            value = Double.parseDouble(valueStr);
        }

        instance.getVariables().set(variable, type, value);
        player.sendMessage(MinecraftColor.GREEN + "Set variable "
                + MinecraftColor.AQUA + variable + MinecraftColor.GREEN
                + MinecraftColor.GREEN + " to "
                + MinecraftColor.GREEN + value);

        return CommandExecutionResult.SUCCESS;
    }
}
