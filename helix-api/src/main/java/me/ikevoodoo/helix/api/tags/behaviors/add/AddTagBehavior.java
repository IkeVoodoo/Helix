package me.ikevoodoo.helix.api.tags.behaviors.add;

import me.ikevoodoo.helix.api.tags.behaviors.TagBehavior;
import org.jetbrains.annotations.ApiStatus;

public class AddTagBehavior extends TagBehavior<AddTagContext> {

    @ApiStatus.Internal
    public AddTagBehavior() {

    }

    @Override
    public boolean async() {
        return false;
    }

}
