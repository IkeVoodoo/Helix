package me.ikevoodoo.helix.api.config.components.arrays;

import me.ikevoodoo.helix.api.config.ConfigComponent;
import me.ikevoodoo.helix.api.config.parsing.CompoundTypeParser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface CompoundArrayConfigComponent<T> extends ConfigComponent {

    CompoundTypeParser<T> parser();

    CompoundArrayConfigComponent<T> parser(CompoundTypeParser<T> parser);

    @NotNull
    String indexString();

    CompoundArrayConfigComponent<T> indexString(@Nullable String indexString);

    T[] defaultValues();

    T[] values();

    CompoundArrayConfigComponent<T> values(T[] value);

    Class<T> type();

}
