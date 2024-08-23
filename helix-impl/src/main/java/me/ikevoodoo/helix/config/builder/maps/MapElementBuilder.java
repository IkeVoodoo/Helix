package me.ikevoodoo.helix.config.builder.maps;

import me.ikevoodoo.helix.api.config.ConfigEntry;
import me.ikevoodoo.helix.api.config.builder.ConfigurationBuilder;
import me.ikevoodoo.helix.api.config.parsing.TypeParser;
import me.ikevoodoo.helix.config.builder.CommentableElementBuilder;
import me.ikevoodoo.helix.config.components.maps.SimpleParsedMapConfigComponent;

import java.util.Map;

public class MapElementBuilder<S, C> extends CommentableElementBuilder {

    private final ConfigurationBuilder parent;
    private final ConfigEntry entry;

    private final String name;
    private final Map<String, C> defaultValue;
    private final TypeParser<S, C> parser;
    private final Class<C> type;

    public MapElementBuilder(ConfigurationBuilder parent, ConfigEntry entry, String name, Map<String, C> defaultValue, TypeParser<S, C> parser, Class<C> type) {
        super(entry, name);
        this.parent = parent;
        this.entry = entry;
        this.name = name;
        this.defaultValue = defaultValue;
        this.parser = parser;
        this.type = type;
    }

    @Override
    public ConfigurationBuilder next() {
        var value = new SimpleParsedMapConfigComponent<S, C>(this.entry, this.name, this.type, this.defaultValue);
        value.parser(this.parser);
        this.entry.addComponent(value);
        return this.parent;
    }

}
