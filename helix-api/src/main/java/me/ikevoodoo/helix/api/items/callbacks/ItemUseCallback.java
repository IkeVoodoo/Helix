package me.ikevoodoo.helix.api.items.callbacks;

import me.ikevoodoo.helix.api.items.instance.HelixItemInstance;
import me.ikevoodoo.helix.api.items.context.ItemUseContext;

public abstract class ItemUseCallback<T extends ItemUseContext> {

    private boolean removed;

    public final void setRemoved(boolean removed) {
        this.removed = removed;
    }

    public final boolean isRemoved() {
        return removed;
    }

    public abstract ItemUseResult onItemUse(T context, HelixItemInstance itemInstance);

}
