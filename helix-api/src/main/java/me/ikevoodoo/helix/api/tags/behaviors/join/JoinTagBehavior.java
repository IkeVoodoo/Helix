package me.ikevoodoo.helix.api.tags.behaviors.join;

import me.ikevoodoo.helix.api.tags.behaviors.TagBehavior;
import org.jetbrains.annotations.ApiStatus;

public class JoinTagBehavior extends TagBehavior<JoinTagContext> {

    @ApiStatus.Internal
    public JoinTagBehavior() {

    }

    @Override
    public boolean async() {
        return false;
    }

}
