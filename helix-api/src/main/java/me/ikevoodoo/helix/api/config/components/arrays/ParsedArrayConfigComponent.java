package me.ikevoodoo.helix.api.config.components.arrays;

import me.ikevoodoo.helix.api.config.parsing.TypeParser;

public interface ParsedArrayConfigComponent<S, C> extends ArrayConfigComponent<C> {

    TypeParser<S, C> parser();

    ParsedArrayConfigComponent<S, C> parser(TypeParser<S, C> parser);

}
