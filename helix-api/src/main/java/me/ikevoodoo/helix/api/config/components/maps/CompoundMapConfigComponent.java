package me.ikevoodoo.helix.api.config.components.maps;

import me.ikevoodoo.helix.api.config.ConfigComponent;
import me.ikevoodoo.helix.api.config.parsing.CompoundTypeParser;

import java.util.Map;

public interface CompoundMapConfigComponent<T> extends ConfigComponent {

    CompoundTypeParser<T> parser();

    CompoundMapConfigComponent<T> parser(CompoundTypeParser<T> parser);

    Map<String, T> defaultValues();

    Map<String, T> values();

    CompoundMapConfigComponent<T> values(Map<String, T> value);

    Class<T> type();

}
