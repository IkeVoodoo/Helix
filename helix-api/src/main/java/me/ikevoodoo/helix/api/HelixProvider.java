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
import org.jetbrains.annotations.NotNull;

import java.io.File;

public interface HelixProvider {

    @NotNull HelixPluginLoader pluginLoader();

    @NotNull HelixEventRegistry events();

    @NotNull HelixItemRegistry items();

    @NotNull HelixCommandRegistry commands();

    @NotNull HelixScreenRegistry screens();

    @NotNull HelixWorldManager worlds();

    @NotNull HelixPlayerManager players();

    @NotNull HelixConfigurationProvider config();

    @NotNull HelixErrorReporter errors();

    @NotNull HelixScheduler scheduler();

    @NotNull
    HelixTagManager tags();

    @NotNull
    Version version();

    @NotNull
    File providerFile();
}
