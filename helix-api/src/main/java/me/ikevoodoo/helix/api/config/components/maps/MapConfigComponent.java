package me.ikevoodoo.helix.api.config.components.maps;

import me.ikevoodoo.helix.api.config.ConfigComponent;

import java.util.Map;

public interface MapConfigComponent<T> extends ConfigComponent {

    Map<String, T> defaultValues();

    Map<String, T> values();

    Class<T> type();


}
