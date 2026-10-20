package com.exodusvehicles.entity;

/** Something the repair wrench can work on. */
public interface Repairable {
    float getHealth();

    float getMaxHealth();

    /** Adds health (capped); false if it can't be repaired (e.g. a burnt-out wreck). */
    boolean repairBy(float amount);
}
