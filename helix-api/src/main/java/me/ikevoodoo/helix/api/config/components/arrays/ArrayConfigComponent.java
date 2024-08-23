package me.ikevoodoo.helix.api.config.components.arrays;

import me.ikevoodoo.helix.api.config.ConfigComponent;

public interface ArrayConfigComponent<T> extends ConfigComponent {

    T[] defaultValues();

    T[] values();

    ArrayConfigComponent<T> values(T[] value);

    Class<T> type();


}
