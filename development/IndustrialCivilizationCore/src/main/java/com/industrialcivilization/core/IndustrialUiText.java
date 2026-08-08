package com.industrialcivilization.core;

import java.util.Locale;

/** Pure UI formatting shared by screen code and fast unit tests. */
public final class IndustrialUiText {
    private static final String[] SUFFIXES = {"", "k", "M", "G"};

    private IndustrialUiText() {}

    public static String compactNumber(long value) {
        double scaled = value;
        int suffix = 0;
        while (Math.abs(scaled) >= 999.5D && suffix < SUFFIXES.length - 1) {
            scaled /= 1000D;
            suffix++;
        }
        double nearest = Math.rint(scaled);
        if (Math.abs(scaled) < 10D && Math.abs(scaled - nearest) >= 0.05D) {
            return String.format(Locale.ROOT, "%.1f%s", scaled, SUFFIXES[suffix]);
        }
        return Long.toString(Math.round(scaled)) + SUFFIXES[suffix];
    }
}
