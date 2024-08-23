package me.ikevoodoo.helix.builtin.commands.items;

import me.ikevoodoo.helix.api.commands.HelixCommand;
import me.ikevoodoo.helix.api.commands.HelixCommandParameters;

public class ItemCommand extends HelixCommand {
    @Override
    protected HelixCommandParameters makeParameters() {
        return HelixCommandParameters.create("items")
                .childCommand(new GiveItemCommand())
                .childCommand(new SetItemAttributeCommand());
    }
}
