package com.save_bed.util;

import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface PlayerBedTracker {
    boolean save_bed$hasReceivedBed();
    void save_bed$setReceivedBed(boolean received);

    BlockPos save_bed$getActiveBedPos();
    void save_bed$setActiveBedPos(BlockPos pos);

    RegistryKey<World> save_bed$getActiveBedWorld();
    void save_bed$setActiveBedWorld(RegistryKey<World> worldKey);
}
