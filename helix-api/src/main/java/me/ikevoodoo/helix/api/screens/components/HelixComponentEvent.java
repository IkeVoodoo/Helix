package me.ikevoodoo.helix.api.screens.components;

import me.ikevoodoo.helix.api.screens.ScreenAction;
import me.ikevoodoo.helix.api.screens.SlotPosition;
import org.bukkit.entity.Player;

public interface HelixComponentEvent {

    ScreenAction action();

    Player player();

    SlotPosition clickPosition();

    boolean cancelled();

    void cancel();

}
