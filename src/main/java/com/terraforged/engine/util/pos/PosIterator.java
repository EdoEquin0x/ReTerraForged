/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.engine.util.pos;

public class PosIterator {
    private final int minX;
    private final int minZ;
    private final int maxX;
    private final int maxY;
    private final int maxZ;
    private final int size;
    private int x;
    private int y;
    private int z;
    private int index = -1;

    public PosIterator(int x, int y, int z, int width, int height, int length) {
        this.x = x - 1;
        this.y = y;
        this.z = z;
        this.minX = x;
        this.minZ = z;
        this.maxX = x + width;
        this.maxY = y + height;
        this.maxZ = z + length;
        this.size = width * height * length - 1;
    }

    public boolean next() {
        if (this.x + 1 < this.maxX) {
            ++this.x;
            ++this.index;
            return true;
        }
        if (this.z + 1 < this.maxZ) {
            this.x = this.minX;
            ++this.z;
            ++this.index;
            return true;
        }
        if (this.y + 1 < this.maxY) {
            this.x = this.minX - 1;
            this.z = this.minZ;
            ++this.y;
            return true;
        }
        return false;
    }

    public int size() {
        return this.size;
    }

    public int index() {
        return this.index;
    }

    public int x() {
        return this.x;
    }

    public int y() {
        return this.y;
    }

    public int z() {
        return this.z;
    }

    public static PosIterator radius2D(int x, int z, int radius) {
        int startX = x - radius;
        int startZ = z - radius;
        int size = radius * 2 + 1;
        return new PosIterator(startX, 0, startZ, size, 0, size);
    }

    public static PosIterator radius3D(int x, int y, int z, int radius) {
        int startX = x - radius;
        int startY = y - radius;
        int startZ = z - radius;
        int size = radius * 2 + 1;
        return new PosIterator(startX, startY, startZ, size, size, size);
    }

    public static PosIterator area(int x, int z, int width, int length) {
        return new PosIterator(x, 0, z, width, 0, length);
    }

    public static PosIterator volume3D(int x, int y, int z, int width, int height, int length) {
        return new PosIterator(x, y, z, width, height, length);
    }

    public static PosIterator range2D(int minX, int minZ, int maxX, int maxZ) {
        int width = maxX - minX;
        int length = maxZ - minZ;
        return new PosIterator(minX, 0, minZ, width, 0, length);
    }

    public static PosIterator range2D(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        int width = 1 + maxX - minX;
        int height = 1 + maxY - minY;
        int length = 1 + maxZ - minZ;
        return new PosIterator(minX, minY, minZ, width, height, length);
    }
}

