package me.ikevoodoo.helix.repo;

import com.github.zafarkhaja.semver.Parser;
import com.github.zafarkhaja.semver.Version;
import com.github.zafarkhaja.semver.expr.Expression;
import com.github.zafarkhaja.semver.expr.ExpressionParser;
import me.ikevoodoo.helix.utils.VersionUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

public final class PluginRepositoryVersions {

    private static final Parser<Expression> EXPRESSION_PARSER = ExpressionParser.newInstance();
    private final @NotNull Map<Version, Expression> supportedVersions;

    public PluginRepositoryVersions(Map<String, String> versionMap) {
        this.supportedVersions = createVersionMap(versionMap);
    }

    public Version checkSupported(@NotNull Version version) {
        for (var entry : this.supportedVersions.entrySet()) {
            if (version.satisfies(entry.getValue())) {
                return entry.getKey();
            }
        }

        return null;
    }

    private static Map<Version, Expression> createVersionMap(Map<String, String> versionMap) {
        var versions = new TreeMap<Version, Expression>((o1, o2) -> -o1.compareTo(o2));

        var current = VersionUtils.getMinecraftVersionSemver();

        for (var entry : versionMap.entrySet()) {
            var key = entry.getKey();
            var value = entry.getValue();

            synchronized (EXPRESSION_PARSER) {
                var check = EXPRESSION_PARSER.parse(value);
                if (!current.satisfies(check)) {
                    continue;
                }

                versions.put(Version.parse(key), new PluginRepositoryVersionExpression(check, value));
            }
        }

        return versions;
    }

    public @NotNull Map<Version, Expression> supportedVersions() {
        return supportedVersions;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (PluginRepositoryVersions) obj;
        return Objects.equals(this.supportedVersions, that.supportedVersions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(supportedVersions);
    }

    @Override
    public String toString() {
        return "PluginRepositoryVersions[" +
                "supportedVersions=" + supportedVersions + ']';
    }


}
