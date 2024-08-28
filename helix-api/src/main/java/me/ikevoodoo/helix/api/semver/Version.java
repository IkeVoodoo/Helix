package me.ikevoodoo.helix.api.semver;

import org.jetbrains.annotations.NotNull;

public record Version(int major, int minor, int patch) implements Comparable<Version> {

    public static Version parse(String version) {
        var trimmed  = version.trim();
        if (trimmed.isEmpty()) return null;

        var split = trimmed.split("\\.");

        var major = split.length > 0 ? Integer.parseInt(split[0]) : 0;
        var minor = split.length > 1 ? Integer.parseInt(split[1]) : 0;
        var patch = split.length > 2 ? Integer.parseInt(split[2]) : 0;

        return new Version(major, minor, patch);
    }

    public boolean satisfies(VersionExpression expression) {
        return expression.operator().compare(this, expression.version());
    }

    @Override
    public String toString() {
        return this.major + "." + this.minor + "." + this.patch;
    }

    @Override
    public int compareTo(@NotNull Version o) {
        if (VersionCompareOperator.GREATER_THAN.compare(this, o)) return -1;
        if (VersionCompareOperator.LESS_THAN.compare(this, o)) return 1;

        return 0;
    }
}
