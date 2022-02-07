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

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BlockPosition blockPosition) {
            return x == blockPosition.getX() && z == blockPosition.getZ();
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return (Integer.valueOf(x).hashCode() >> 13) ^ Integer.valueOf(z).hashCode();
    }
}
