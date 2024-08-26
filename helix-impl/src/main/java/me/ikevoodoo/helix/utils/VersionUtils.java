package me.ikevoodoo.helix.utils;

import com.github.zafarkhaja.semver.Version;
import org.bukkit.Bukkit;

public final class VersionUtils {

    private static String minecraftVersion;
    private static Version minecraftVersionSemver;

    public static String getMinecraftVersion() {
        if (VersionUtils.minecraftVersion == null) {
            VersionUtils.minecraftVersion = Bukkit.getBukkitVersion().split("-")[0];
        }

        return VersionUtils.minecraftVersion;
    }

    public static Version getMinecraftVersionSemver() {
        if(VersionUtils.minecraftVersionSemver == null) {
            var mcVer = getMinecraftVersion();

            var hasNoThird = mcVer.split("\\.").length == 2;

            if (hasNoThird) {
                mcVer += ".0";
            }

            VersionUtils.minecraftVersionSemver = Version.parse(mcVer);
        }

        return VersionUtils.minecraftVersionSemver;
    }

}
