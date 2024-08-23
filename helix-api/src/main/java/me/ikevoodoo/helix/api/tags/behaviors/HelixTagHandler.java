package me.ikevoodoo.helix.api.tags.behaviors;

import me.ikevoodoo.helix.api.storage.HelixDataStorage;

public interface HelixTagHandler<T extends HelixTagContext> {

    TagResult handle(T context, HelixDataStorage storage, HelixTagInstance instance);

}
