package me.ikevoodoo.helix.builtin.commands.repo.update.utils;

import com.github.zafarkhaja.semver.Version;

public record EntryUpdateData(
        String pluginId,
        String repository,
        String resourceId,
        Version updatingTo,
        long size
) {
}
