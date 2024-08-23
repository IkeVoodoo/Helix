package me.ikevoodoo.helix.api.config;

import me.ikevoodoo.helix.api.config.builder.ConfigurationBuilder;

public interface HelixConfigurationProvider {

    ConfigurationBuilder createBuilder();
}
