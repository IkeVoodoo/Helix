package me.ikevoodoo.helix.api.config.components.maps;

import me.ikevoodoo.helix.api.config.parsing.TypeParser;

public interface ParsedMapConfigComponent<S, C> extends MapConfigComponent<C> {

    TypeParser<S, C> parser();

    ParsedMapConfigComponent<S, C> parser(TypeParser<S, C> parser);

}
