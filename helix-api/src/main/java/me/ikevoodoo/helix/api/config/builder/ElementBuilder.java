package me.ikevoodoo.helix.api.config.builder;

import me.ikevoodoo.helix.api.config.Commentable;

public interface ElementBuilder extends Commentable<ElementBuilder> {

    ConfigurationBuilder next();

}
