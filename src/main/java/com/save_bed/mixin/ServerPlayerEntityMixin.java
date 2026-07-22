package com.save_bed.mixin;

import com.save_bed.util.PlayerBedTracker;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin implements PlayerBedTracker {
    @Unique
    private boolean save_bed$hasReceivedBed = false;
    @Unique
    private BlockPos save_bed$activeBedPos;
    @Unique
    private RegistryKey<World> save_bed$activeBedWorld;

    @Override
    public boolean save_bed$hasReceivedBed() {
        return this.save_bed$hasReceivedBed;
    }

    @Override
    public void save_bed$setReceivedBed(boolean received) {
        this.save_bed$hasReceivedBed = received;
    }

    @Override
    public BlockPos save_bed$getActiveBedPos() {
        return this.save_bed$activeBedPos;
    }

    @Override
    public void save_bed$setActiveBedPos(BlockPos pos) {
        this.save_bed$activeBedPos = pos;
    }

    @Override
    public RegistryKey<World> save_bed$getActiveBedWorld() {
        return this.save_bed$activeBedWorld;
    }

    @Override
    public void save_bed$setActiveBedWorld(RegistryKey<World> worldKey) {
        this.save_bed$activeBedWorld = worldKey;
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void injectWriteData(NbtCompound nbt, CallbackInfo ci) {
        nbt.putBoolean("hasReceivedEnchantedBed", this.save_bed$hasReceivedBed);
        if (this.save_bed$activeBedPos != null) {
            nbt.putInt("activeBedX", this.save_bed$activeBedPos.getX());
            nbt.putInt("activeBedY", this.save_bed$activeBedPos.getY());
            nbt.putInt("activeBedZ", this.save_bed$activeBedPos.getZ());
        }
        if (this.save_bed$activeBedWorld != null) {
            nbt.putString("activeBedWorld", this.save_bed$activeBedWorld.getValue().toString());
        }
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void injectReadData(NbtCompound nbt, CallbackInfo ci) {
        if (nbt.contains("hasReceivedEnchantedBed")) {
            this.save_bed$hasReceivedBed = nbt.getBoolean("hasReceivedEnchantedBed");
        }
        if (nbt.contains("activeBedX") && nbt.contains("activeBedY") && nbt.contains("activeBedZ")) {
            this.save_bed$activeBedPos = new BlockPos(nbt.getInt("activeBedX"), nbt.getInt("activeBedY"), nbt.getInt("activeBedZ"));
        } else {
            this.save_bed$activeBedPos = null;
        }
        if (nbt.contains("activeBedWorld")) {
            this.save_bed$activeBedWorld = RegistryKey.of(RegistryKeys.WORLD, new Identifier(nbt.getString("activeBedWorld")));
        } else {
            this.save_bed$activeBedWorld = null;
        }
    }
}
