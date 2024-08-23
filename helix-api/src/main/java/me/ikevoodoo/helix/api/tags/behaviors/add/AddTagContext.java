package me.ikevoodoo.helix.api.tags.behaviors.add;

import me.ikevoodoo.helix.api.tags.behaviors.HelixTagContext;
import org.bukkit.World;

import java.util.UUID;

public record AddTagContext(World world, UUID target) implements HelixTagContext {


}
