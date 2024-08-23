package me.ikevoodoo.helix.config;

import me.ikevoodoo.helix.api.config.ConfigEntry;
import me.ikevoodoo.helix.api.config.errors.ConfigError;
import me.ikevoodoo.helix.api.config.errors.MissingValueConfigError;
import me.ikevoodoo.helix.config.components.SimpleTreeConfigComponent;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

public class ConfigSection extends SimpleTreeConfigComponent implements ConfigEntry {

    public ConfigSection(String name) {
        super(null, null, name);
    }

    @ApiStatus.Internal
    public ConfigSection(ConfigSection parent, String name) {
        super(null, parent, name);
    }

    @Override
    public void write(@NotNull StringBuilder builder, int indent) {
        if (this.parent() != null) {
            builder.append("    ".repeat(indent)).append(this.id()).append(":\n");
        }

        super.write(builder, indent);
    }

    @Override
    public void writeError(@NotNull StringBuilder builder, @NotNull ConfigError error, int indent) {
        if (error instanceof MissingValueConfigError) {
            builder.append("<light:green>+").append("    ".repeat(indent).substring(indent == 0 ? 0 : 1)).append(this.id()).append(":\n");
        } else if (this.parent() != null) {
            builder.append("    ".repeat(indent)).append(this.id()).append(":\n");
        }


        super.writeError(builder, error, indent);
    }

}
