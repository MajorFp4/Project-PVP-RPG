package com.save_bed.block.entity;

import com.save_bed.SaveBed;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BedBlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;

import java.util.UUID;

public class EnchantedBedBlockEntity extends BedBlockEntity {
    private UUID ownerUuid;
    private String ownerName;
    private int baseLifes = 3;

    public EnchantedBedBlockEntity(BlockPos pos, BlockState state) {
        super(pos, state);
    }

    @Override
    public net.minecraft.block.entity.BlockEntityType<?> getType() {
        return SaveBed.ENCHANTED_BED_BLOCK_ENTITY;
    }

    public UUID getOwnerUuid() {
        return this.ownerUuid;
    }

    public void setOwnerUuid(UUID uuid) {
        this.ownerUuid = uuid;
        this.markDirty();
    }

    public String getOwnerName() {
        return this.ownerName;
    }

    public void setOwnerName(String name) {
        this.ownerName = name;
        this.markDirty();
    }

    public int getBaseLifes() {
        return this.baseLifes;
    }

    public void setBaseLifes(int lifes) {
        this.baseLifes = lifes;
        this.markDirty();
    }

    @Override
    public void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        if (this.ownerUuid != null) {
            nbt.putUuid("OwnerUUID", this.ownerUuid);
        }
        if (this.ownerName != null) {
            nbt.putString("OwnerName", this.ownerName);
        }
        nbt.putInt("BaseLifes", this.baseLifes);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        if (nbt.contains("OwnerUUID")) {
            this.ownerUuid = nbt.getUuid("OwnerUUID");
        }
        if (nbt.contains("OwnerName")) {
            this.ownerName = nbt.getString("OwnerName");
        }
        if (nbt.contains("BaseLifes")) {
            this.baseLifes = nbt.getInt("BaseLifes");
        } else {
            this.baseLifes = 3;
        }
    }

    @Override
    public BlockEntityUpdateS2CPacket toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        return createNbt();
    }
}
