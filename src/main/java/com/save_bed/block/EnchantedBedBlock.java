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
    public java.util.List<ItemStack> getDroppedStacks(BlockState state, net.minecraft.loot.context.LootContextParameterSet.Builder builder) {
        return java.util.Collections.emptyList();
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!world.isClient() && hand == Hand.MAIN_HAND && player instanceof ServerPlayerEntity) {
            BlockEntity be = world.getBlockEntity(pos);
            if (be instanceof EnchantedBedBlockEntity) {
                EnchantedBedBlockEntity bedBe = (EnchantedBedBlockEntity) be;
                if (bedBe.isOwner(player)) {
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
            if (bedBe.isOwner(player)) {
                return 0.0f; // Unbreakable for owner
            }
        }
        return super.calcBlockBreakingDelta(state, player, world, pos);
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        super.onStateReplaced(state, world, pos, newState, moved);
    }
}
