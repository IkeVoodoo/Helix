package me.ikevoodoo.helix.api.config.components.values;

import me.ikevoodoo.helix.api.config.ConfigComponent;
import me.ikevoodoo.helix.api.config.parsing.CompoundTypeParser;

public interface CompoundConfigComponent<T> extends ConfigComponent {

    CompoundTypeParser<T> parser();

    CompoundConfigComponent<T> parser(CompoundTypeParser<T> parser);

    T defaultValue();

    T value();

    CompoundConfigComponent<T> value(T value);

    Class<T> type();

}
