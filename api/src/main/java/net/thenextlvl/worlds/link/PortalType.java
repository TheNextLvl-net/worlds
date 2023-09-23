package net.thenextlvl.worlds.link;

public enum PortalType {
    NETHER_PORTAL,
    END_PORTAL,
    END_GATEWAY,
    CUSTOM;

    @Override
    public String toString() {
        return name().toLowerCase().replace("_", " ");
    }
}
