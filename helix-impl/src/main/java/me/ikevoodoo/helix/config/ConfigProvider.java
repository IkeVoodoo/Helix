package me.ikevoodoo.helix.config;

import me.ikevoodoo.helix.api.config.HelixConfigurationProvider;
import me.ikevoodoo.helix.api.config.builder.ConfigurationBuilder;
import me.ikevoodoo.helix.config.builder.SimpleConfigurationBuilder;

public class ConfigProvider implements HelixConfigurationProvider {

    @Override
    public ConfigurationBuilder createBuilder() {
        return new SimpleConfigurationBuilder("root");
    }
}
