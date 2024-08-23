package me.ikevoodoo.helix.api.config.components;

import me.ikevoodoo.helix.api.config.ConfigComponent;
import me.ikevoodoo.helix.api.config.ConfigEntry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface TreeConfigComponent extends ConfigComponent, ConfigEntry {

    @NotNull
    String path();

    @Nullable
    TreeConfigComponent parent();

}
