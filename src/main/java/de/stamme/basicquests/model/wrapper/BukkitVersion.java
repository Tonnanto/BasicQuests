package de.stamme.basicquests.model.wrapper;

public enum BukkitVersion {
    v1_16,
    v1_17,
    v1_18,
    v1_19,
    v1_20;

    public boolean isBelowOrEqual(BukkitVersion version) {
        return this.ordinal() <= version.ordinal();
    }
}
