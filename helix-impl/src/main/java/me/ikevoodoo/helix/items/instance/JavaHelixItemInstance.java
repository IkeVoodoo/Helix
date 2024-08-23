package me.ikevoodoo.helix.items.instance;

import me.ikevoodoo.helix.api.items.HelixItem;
import me.ikevoodoo.helix.api.items.instance.HelixItemInstance;
import me.ikevoodoo.helix.api.items.variables.HelixItemVariables;
import me.ikevoodoo.helix.api.plugins.HelixPlugin;
import me.ikevoodoo.helix.items.variables.JavaHelixItemVariables;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

public class JavaHelixItemInstance extends HelixItemInstance {
    private final NamespacedKey variableKey;
    private HelixItemVariables variables;

    public JavaHelixItemInstance(HelixPlugin plugin, HelixItem item, ItemStack stack, NamespacedKey variableKey) {
        super(plugin, item, stack);
        this.variableKey = variableKey;
    }

    @Override
    public HelixItemVariables getVariables() {
        if (this.variables == null) {
            this.variables = new JavaHelixItemVariables(this.variableKey, this.getPlugin(), this.getStack());
        }

        return this.variables;
    }
}
