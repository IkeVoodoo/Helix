package me.ikevoodoo.helix.api;

import me.ikevoodoo.helix.api.commands.HelixCommandRegistry;
import me.ikevoodoo.helix.api.config.HelixConfigurationProvider;
import me.ikevoodoo.helix.api.events.HelixEventRegistry;
import me.ikevoodoo.helix.api.items.HelixItemRegistry;
import me.ikevoodoo.helix.api.players.HelixPlayerManager;
import me.ikevoodoo.helix.api.plugins.loading.HelixPluginLoader;
import me.ikevoodoo.helix.api.reporting.HelixErrorReporter;
import me.ikevoodoo.helix.api.scheduling.HelixScheduler;
import me.ikevoodoo.helix.api.screens.HelixScreenRegistry;
import me.ikevoodoo.helix.api.semver.Version;
import me.ikevoodoo.helix.api.tags.HelixTagManager;
import me.ikevoodoo.helix.api.words.HelixWorldManager;
import org.jetbrains.annotations.ApiStatus;

import java.io.File;

public final class Helix {

    private static HelixProvider helixProvider;

    @ApiStatus.Internal
    public static void setHelixProvider(HelixProvider helixProvider) {
        if (Helix.helixProvider != null) {
            throw new IllegalStateException("HelixProvider was already set!");
        }

        Helix.helixProvider = helixProvider;
    }

    public static HelixProvider provider() {
        return Helix.helixProvider;
    }

    public static HelixPluginLoader pluginLoader() {
        return provider().pluginLoader();
    }

    public static HelixEventRegistry events() {
        return provider().events();
    }

    public static HelixItemRegistry items() { return provider().items(); }

    public static HelixCommandRegistry commands() {
        return provider().commands();
    }

    public static HelixErrorReporter errors() {
        return provider().errors();
    }

    public static HelixConfigurationProvider config() {
        return provider().config();
    }

    public static HelixScheduler scheduler() {
        return provider().scheduler();
    }

    public static HelixScreenRegistry screens() {
        return provider().screens();
    }

    public static HelixWorldManager worlds() {
        return provider().worlds();
    }

    public static HelixPlayerManager players() {
        return provider().players();
    }

    public static HelixTagManager tags() {
        return provider().tags();
    }

    public static Version version() {
        return provider().version();
    }

    public static File providerFile() {
        return provider().providerFile();
    }
}
