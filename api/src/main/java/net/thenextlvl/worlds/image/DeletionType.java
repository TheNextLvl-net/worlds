package net.thenextlvl.worlds.image;

public enum DeletionType {
    WORLD, WORLD_AND_IMAGE;

    public boolean keepImage() {
        return equals(WORLD);
    }
}
