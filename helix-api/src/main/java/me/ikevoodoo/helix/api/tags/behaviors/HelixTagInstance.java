package me.ikevoodoo.helix.api.tags.behaviors;

import java.util.UUID;

public interface HelixTagInstance {

    UUID attachedTo();

    void remove();

}
