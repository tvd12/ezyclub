package org.youngmonkeys.ezyclub.util;

public final class EzyclubLevels {

    private static final long[] LEVEL_POINTS = {
        0, 10, 30, 70, 150, 300, 600, 1200, 2500
    };

    private EzyclubLevels() {}

    public static int maxLevel() {
        return LEVEL_POINTS.length;
    }

    public static int levelOf(long points) {
        int level = 1;
        for (int i = 0; i < LEVEL_POINTS.length; ++i) {
            if (points >= LEVEL_POINTS[i]) {
                level = i + 1;
            }
        }
        return level;
    }

    public static long pointsOfLevel(int level) {
        int index = Math.max(1, Math.min(level, LEVEL_POINTS.length)) - 1;
        return LEVEL_POINTS[index];
    }

    public static long pointsToNextLevel(long points) {
        int level = levelOf(points);
        if (level >= LEVEL_POINTS.length) {
            return 0;
        }
        return LEVEL_POINTS[level] - points;
    }
}
