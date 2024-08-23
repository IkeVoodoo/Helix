package me.ikevoodoo.helix.builtin.commands.repo.update.utils;

public class RepoUpdateCommons {

    private static final long KILOBYTE = 1024;
    private static final int MAX_ZEROES = 63;

    public static String formatSize(long value) {
        if (value < KILOBYTE) return value + " B";
        var leading = Long.numberOfLeadingZeros(value);

        int category = (MAX_ZEROES - leading) / 10;

        var doubleValue = (double) value / (1L << (category * 10));
        var categoryString = " KMGTPE".charAt(category);

        return String.format("%.1f %sB", doubleValue, categoryString).replace("  ", " ");
    }

}
