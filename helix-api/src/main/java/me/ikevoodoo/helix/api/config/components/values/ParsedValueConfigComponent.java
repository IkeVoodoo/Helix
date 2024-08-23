package me.ikevoodoo.helix.api.config.components.values;

import me.ikevoodoo.helix.api.config.parsing.TypeParser;

public interface ParsedValueConfigComponent<S, C> extends ValueConfigComponent<C> {
    
    TypeParser<S, C> parser();

    ParsedValueConfigComponent<S, C> parser(TypeParser<S, C> parser);
    
}
