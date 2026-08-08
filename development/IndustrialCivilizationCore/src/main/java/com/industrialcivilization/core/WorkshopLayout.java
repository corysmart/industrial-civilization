package com.industrialcivilization.core;

/** Pure workshop-footprint geometry shared by runtime placement and JUnit. */
public final class WorkshopLayout {
    public enum Join { NONE, EAST_WEST, NORTH_SOUTH }

    public static final class Bounds {
        public final int minX, maxX, minZ, maxZ;

        Bounds(int minX, int maxX, int minZ, int maxZ) {
            this.minX = minX;
            this.maxX = maxX;
            this.minZ = minZ;
            this.maxZ = maxZ;
        }

        public boolean overlaps(Bounds other) {
            return minX <= other.maxX && maxX >= other.minX
                && minZ <= other.maxZ && maxZ >= other.minZ;
        }
    }

    /** North/south layouts are 9x7; east/west layouts rotate to 7x9. */
    public static Bounds bounds(int centerX, int centerZ, boolean rotated) {
        int halfX = rotated ? 3 : 4;
        int halfZ = rotated ? 4 : 3;
        return new Bounds(centerX - halfX, centerX + halfX,
            centerZ - halfZ, centerZ + halfZ);
    }

    /** Workshops join only when their footprints touch without overlapping. */
    public static Join join(Bounds a, Bounds b) {
        int zOverlap = overlap(a.minZ, a.maxZ, b.minZ, b.maxZ);
        if (zOverlap >= 3 && (a.maxX + 1 == b.minX || b.maxX + 1 == a.minX)) {
            return Join.EAST_WEST;
        }
        int xOverlap = overlap(a.minX, a.maxX, b.minX, b.maxX);
        if (xOverlap >= 3 && (a.maxZ + 1 == b.minZ || b.maxZ + 1 == a.minZ)) {
            return Join.NORTH_SOUTH;
        }
        return Join.NONE;
    }

    public static int overlap(int minA, int maxA, int minB, int maxB) {
        return Math.max(0, Math.min(maxA, maxB) - Math.max(minA, minB) + 1);
    }

    public static int centerOfOverlap(int minA, int maxA, int minB, int maxB) {
        int min = Math.max(minA, minB);
        int max = Math.min(maxA, maxB);
        return min + (max - min) / 2;
    }

    private WorkshopLayout() {}
}
