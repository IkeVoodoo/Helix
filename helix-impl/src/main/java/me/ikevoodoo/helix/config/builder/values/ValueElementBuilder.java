package me.ikevoodoo.helix.config.builder.values;

import me.ikevoodoo.helix.api.config.ConfigEntry;
import me.ikevoodoo.helix.api.config.builder.ConfigurationBuilder;
import me.ikevoodoo.helix.api.config.parsing.TypeParser;
import me.ikevoodoo.helix.config.builder.CommentableElementBuilder;
import me.ikevoodoo.helix.config.components.values.SimpleParsedValueConfigComponent;

public class ValueElementBuilder<S, C> extends CommentableElementBuilder {

    private final ConfigurationBuilder parent;
    private final ConfigEntry entry;

    private final String name;
    private final C defaultValue;
    private final TypeParser<S, C> parser;

    public ValueElementBuilder(ConfigurationBuilder parent, ConfigEntry entry, String name, C defaultValue, TypeParser<S, C> parser) {
        super(entry, name);
        this.parent = parent;
        this.entry = entry;
        this.name = name;
        this.defaultValue = defaultValue;
        this.parser = parser;
    }

    @Override
    @SuppressWarnings("unchecked")
    public ConfigurationBuilder next() {
        var type = this.parser == null ? (Class<C>) this.defaultValue.getClass() : this.parser.complexType();

        var value = new SimpleParsedValueConfigComponent<S, C>(this.entry, this.name, type, this.defaultValue);
        value.parser(this.parser);
        this.entry.addComponent(value);
        return this.parent;
    }

}
