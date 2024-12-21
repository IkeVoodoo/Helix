package me.ikevoodoo.helix.api.tags.behaviors;

import me.ikevoodoo.helix.api.tags.behaviors.add.AddTagBehavior;
import me.ikevoodoo.helix.api.tags.behaviors.join.JoinTagBehavior;
import me.ikevoodoo.helix.api.tags.behaviors.join.async.AsyncJoinTagBehavior;
import me.ikevoodoo.helix.api.tags.behaviors.quit.QuitTagBehavior;
import me.ikevoodoo.helix.api.tags.behaviors.remove.RemoveTagBehavior;

public final class TagBehaviors {

    public static final JoinTagBehavior JOIN = new JoinTagBehavior();
    public static final QuitTagBehavior QUIT = new QuitTagBehavior();
    public static final AsyncJoinTagBehavior ASYNC_JOIN = new AsyncJoinTagBehavior();
    public static final AddTagBehavior ADD = new AddTagBehavior();
    public static final RemoveTagBehavior REMOVE = new RemoveTagBehavior();

}
