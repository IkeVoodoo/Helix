package me.ikevoodoo.helix.api.semver;

public record VersionExpression(VersionCompareOperator operator, Version version) {

    public static VersionExpression parse(String expression) {
        var trimmed = expression.trim();
        if (trimmed.isEmpty()) return null;

        var pattern = VersionCompareOperator.getMatchPattern();

        var matcher = pattern.matcher(trimmed);
        if (!matcher.find()) {
            throw new IllegalArgumentException("Invalid expression: " + expression);
        }

        var versionOperatorString = matcher.group(1);
        var versionOperator = VersionCompareOperator.fromString(versionOperatorString);
        var versionString = trimmed.substring(matcher.end(1));
        var version = Version.parse(versionString);

        return new VersionExpression(versionOperator, version);
    }

    @Override
    public String toString() {
        return this.operator.getOperator() + this.version.toString();
    }
}
