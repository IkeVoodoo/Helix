package me.ikevoodoo.helix.builtin.commands.repo.update.utils;

import me.ikevoodoo.helix.api.semver.Version;

public record EntryUpdateData(
        String pluginId,
        String repository,
        String resourceId,
        Version updatingTo,
        long size
) {
}
