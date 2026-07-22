package com.save_bed.block;

import com.save_bed.SaveBed;
import com.save_bed.block.entity.EnchantedBedBlockEntity;
import net.minecraft.block.BedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class EnchantedBedBlock extends BedBlock {
    public EnchantedBedBlock(Settings settings) {
        super(DyeColor.RED, settings);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new EnchantedBedBlockEntity(pos, state);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!world.isClient() && hand == Hand.MAIN_HAND && player instanceof ServerPlayerEntity) {
            BlockEntity be = world.getBlockEntity(pos);
            if (be instanceof EnchantedBedBlockEntity) {
                EnchantedBedBlockEntity bedBe = (EnchantedBedBlockEntity) be;
                if (player.getUuid().equals(bedBe.getOwnerUuid())) {
                    com.save_bed.logic.BedLogic.openTakeUI((ServerPlayerEntity) player);
                }
            }
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);
        if (!world.isClient() && placer instanceof ServerPlayerEntity) {
            com.save_bed.logic.BedLogic.bindBedToPlayer((ServerPlayerEntity) placer, pos, world);
        }
    }

    @Override
    public float calcBlockBreakingDelta(BlockState state, PlayerEntity player, BlockView world, BlockPos pos) {
        BlockEntity be = world.getBlockEntity(pos);
        if (be instanceof EnchantedBedBlockEntity) {
            EnchantedBedBlockEntity bedBe = (EnchantedBedBlockEntity) be;
            if (player.getUuid().equals(bedBe.getOwnerUuid())) {
                return 0.0f; // Unbreakable for owner
            }
        }
        return super.calcBlockBreakingDelta(state, player, world, pos);
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.isOf(newState.getBlock())) {
            if (!world.isClient() && state.get(BedBlock.PART) == net.minecraft.block.enums.BedPart.HEAD) {
                BlockEntity be = world.getBlockEntity(pos);
                if (be instanceof EnchantedBedBlockEntity) {
                    EnchantedBedBlockEntity bedBe = (EnchantedBedBlockEntity) be;
                    ItemStack stack = new ItemStack(SaveBed.ENCHANTED_BED);
                    if (bedBe.getOwnerUuid() != null) {
                        NbtCompound nbt = stack.getOrCreateNbt();
                        nbt.putUuid("OwnerUUID", bedBe.getOwnerUuid());
                        nbt.putString("OwnerName", bedBe.getOwnerName());
                    }
                    Block.dropStack(world, pos, stack);
                }
            }
        }
        super.onStateReplaced(state, world, pos, newState, moved);
    }
}
