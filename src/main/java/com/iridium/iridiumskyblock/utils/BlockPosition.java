package com.iridium.iridiumskyblock.utils;

public class BlockPosition {

    private int x;
    private int z;

    public BlockPosition(int x, int z) {
        this.x = x;
        this.z = z;
    }

    public int getZ() {
        return z;
    }

    public int getX() {
        return x;
    }
}
