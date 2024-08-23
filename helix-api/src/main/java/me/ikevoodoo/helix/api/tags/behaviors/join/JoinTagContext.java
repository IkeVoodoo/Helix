package me.ikevoodoo.helix.api.tags.behaviors.join;

import me.ikevoodoo.helix.api.tags.behaviors.HelixTagContext;
import org.bukkit.World;
import org.bukkit.entity.Player;

public record JoinTagContext(World world, Player player) implements HelixTagContext {


}
