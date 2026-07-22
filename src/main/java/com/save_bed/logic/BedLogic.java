package com.save_bed.logic;

import com.save_bed.SaveBed;
import com.save_bed.block.EnchantedBedBlock;
import com.save_bed.block.entity.EnchantedBedBlockEntity;
import com.save_bed.util.PlayerBedTracker;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.block.BedBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import java.util.UUID;

public class BedLogic {

    public static void register() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            checkAndTriggerFirstJoin(player);
        });

        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
            PlayerBedTracker oldTracker = (PlayerBedTracker) oldPlayer;
            PlayerBedTracker newTracker = (PlayerBedTracker) newPlayer;
            newTracker.save_bed$setReceivedBed(oldTracker.save_bed$hasReceivedBed());
            newTracker.save_bed$setActiveBedPos(oldTracker.save_bed$getActiveBedPos());
            newTracker.save_bed$setActiveBedWorld(oldTracker.save_bed$getActiveBedWorld());
        });

        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            processPlayerRespawn(newPlayer);
        });

        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
            if (blockEntity instanceof EnchantedBedBlockEntity) {
                EnchantedBedBlockEntity bedBe = (EnchantedBedBlockEntity) blockEntity;
                if (bedBe.isOwner(player)) {
                    return false; // Prevent owner from breaking their own bed
                } else {
                    if (!world.isClient() && player instanceof ServerPlayerEntity) {
                        processEnemyBreak(world, pos, (ServerPlayerEntity) player);
                    }
                    return false; // Cancel default breaking
                }
            }
            return true;
        });

        // Register server C2S packet receiver for bed pickup
        ServerPlayNetworking.registerGlobalReceiver(new Identifier(SaveBed.MOD_ID, "take_bed"), (server, player, handler, buf, responseSender) -> {
            BlockPos pos = buf.readBlockPos();
            server.execute(() -> {
                pickupBed(player, pos);
            });
        });
    }

    public static void checkAndTriggerFirstJoin(ServerPlayerEntity player) {
        PlayerBedTracker tracker = (PlayerBedTracker) player;
        if (!tracker.save_bed$hasReceivedBed()) {
            giveEnchantedBed(player);
            tracker.save_bed$setReceivedBed(true);
        }
    }

    public static void giveEnchantedBed(ServerPlayerEntity player) {
        ItemStack stack = new ItemStack(SaveBed.ENCHANTED_BED);
        NbtCompound nbt = stack.getOrCreateNbt();
        nbt.putUuid("OwnerUUID", player.getUuid());
        nbt.putString("OwnerName", player.getGameProfile().getName());

        if (!player.getInventory().insertStack(stack)) {
            player.dropItem(stack, false);
        }
    }

    public static void bindBedToPlayer(ServerPlayerEntity player, BlockPos pos, World world) {
        PlayerBedTracker tracker = (PlayerBedTracker) player;
        tracker.save_bed$setActiveBedPos(pos);
        tracker.save_bed$setActiveBedWorld(world.getRegistryKey());

        // Update BlockEntity owner data for both bed parts
        BlockEntity be = world.getBlockEntity(pos);
        if (be instanceof EnchantedBedBlockEntity) {
            EnchantedBedBlockEntity bedBe = (EnchantedBedBlockEntity) be;
            bedBe.setOwnerUuid(player.getUuid());
            bedBe.setOwnerName(player.getGameProfile().getName());
            // Notify clients so they receive the owner UUID via toUpdatePacket()
            BlockState placedState = world.getBlockState(pos);
            world.updateListeners(pos, placedState, placedState, net.minecraft.block.Block.NOTIFY_ALL);
        }

        BlockState state = world.getBlockState(pos);
        if (state.getBlock() instanceof EnchantedBedBlock) {
            BlockPos otherPos = pos.offset(BedBlock.getOppositePartDirection(state));
            BlockEntity otherBe = world.getBlockEntity(otherPos);
            if (otherBe instanceof EnchantedBedBlockEntity) {
                EnchantedBedBlockEntity otherBedBe = (EnchantedBedBlockEntity) otherBe;
                otherBedBe.setOwnerUuid(player.getUuid());
                otherBedBe.setOwnerName(player.getGameProfile().getName());
                BlockState otherState = world.getBlockState(otherPos);
                world.updateListeners(otherPos, otherState, otherState, net.minecraft.block.Block.NOTIFY_ALL);
            }
        }

        // Play XP orb pickup sound at bed location
        world.playSound(null, pos, SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.BLOCKS, 1.0f, 1.0f);

        // Set vanilla spawn point
        player.setSpawnPoint(world.getRegistryKey(), pos, 0.0f, true, false);
    }

    public static void processPlayerRespawn(ServerPlayerEntity player) {
        PlayerBedTracker tracker = (PlayerBedTracker) player;
        BlockPos pos = tracker.save_bed$getActiveBedPos();
        RegistryKey<World> worldKey = tracker.save_bed$getActiveBedWorld();

        boolean bedValid = false;
        if (pos != null && worldKey != null) {
            ServerWorld world = player.server.getWorld(worldKey);
            if (world != null) {
                BlockState state = world.getBlockState(pos);
                if (state.getBlock() instanceof EnchantedBedBlock) {
                    BlockEntity be = world.getBlockEntity(pos);
                    if (be instanceof EnchantedBedBlockEntity) {
                        EnchantedBedBlockEntity bedBe = (EnchantedBedBlockEntity) be;
                        if (bedBe.isOwner(player)) {
                            bedValid = true;
                        }
                    }
                }
            }
        }

        if (!bedValid) {
            player.changeGameMode(GameMode.SPECTATOR);
            player.sendMessage(Text.literal("You have no active Enchanted Bed! You are now a spectator.").formatted(Formatting.RED));
            tracker.save_bed$setActiveBedPos(null);
            tracker.save_bed$setActiveBedWorld(null);
        } else {
            player.sendMessage(Text.literal("You respawned at your Enchanted Bed.").formatted(Formatting.GREEN));
        }
    }

    public static void processEnemyBreak(World world, BlockPos pos, ServerPlayerEntity enemy) {
        BlockEntity be = world.getBlockEntity(pos);
        if (be instanceof EnchantedBedBlockEntity) {
            EnchantedBedBlockEntity bedBe = (EnchantedBedBlockEntity) be;
            int lifes = bedBe.getBaseLifes() - 1;
            bedBe.setBaseLifes(lifes);

            // Play the Totem/Anvil sounds at the bed's location
            world.playSound(null, pos, SoundEvents.ITEM_TOTEM_USE, SoundCategory.BLOCKS, 1.0f, 1.0f);
            world.playSound(null, pos, SoundEvents.BLOCK_ANVIL_DESTROY, SoundCategory.BLOCKS, 1.0f, 1.0f);

            if (lifes <= 0) {
                // Destroy block completely (no item drop)
                BlockState state = world.getBlockState(pos);
                if (state.getBlock() instanceof EnchantedBedBlock) {
                    BlockPos otherPos = pos.offset(BedBlock.getOppositePartDirection(state));

                    // Clear the active bed location on the owner player if online
                    ServerPlayerEntity owner = null;
                    if (world.getServer() != null) {
                        for (ServerPlayerEntity onlinePlayer : world.getServer().getPlayerManager().getPlayerList()) {
                            if (bedBe.isOwner(onlinePlayer)) {
                                owner = onlinePlayer;
                                break;
                            }
                        }
                    }
                    if (owner != null) {
                            PlayerBedTracker tracker = (PlayerBedTracker) owner;
                            tracker.save_bed$setActiveBedPos(null);
                            tracker.save_bed$setActiveBedWorld(null);
                            owner.sendMessage(Text.literal("Your Enchanted Bed was destroyed!").formatted(Formatting.RED));
                        }
                    // Remove block entities to prevent item drops in onStateReplaced
                    world.removeBlockEntity(pos);
                    world.removeBlockEntity(otherPos);

                    // Set both to air
                    world.setBlockState(pos, net.minecraft.block.Blocks.AIR.getDefaultState(), net.minecraft.block.Block.NOTIFY_ALL);
                    world.setBlockState(otherPos, net.minecraft.block.Blocks.AIR.getDefaultState(), net.minecraft.block.Block.NOTIFY_ALL);
                }
            } else {
                // Synchronize block state/entity to all clients
                BlockState state = world.getBlockState(pos);
                world.updateListeners(pos, state, state, net.minecraft.block.Block.NOTIFY_ALL);

                // Sync the other part of the bed too
                if (state.getBlock() instanceof EnchantedBedBlock) {
                    BlockPos otherPos = pos.offset(BedBlock.getOppositePartDirection(state));
                    BlockEntity otherBe = world.getBlockEntity(otherPos);
                    if (otherBe instanceof EnchantedBedBlockEntity) {
                        EnchantedBedBlockEntity otherBedBe = (EnchantedBedBlockEntity) otherBe;
                        otherBedBe.setBaseLifes(lifes);
                        BlockState otherState = world.getBlockState(otherPos);
                        world.updateListeners(otherPos, otherState, otherState, net.minecraft.block.Block.NOTIFY_ALL);
                    }
                }

                // Notify the enemy breaking player of remaining lifes
                enemy.sendMessage(Text.literal("Enchanted Bed has " + lifes + " lives remaining!").formatted(Formatting.GOLD));
            }
        }
    }

    public static void openTakeUI(ServerPlayerEntity player) {
        PlayerBedTracker tracker = (PlayerBedTracker) player;
        BlockPos pos = tracker.save_bed$getActiveBedPos();
        if (pos != null) {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeBlockPos(pos);
            ServerPlayNetworking.send(player, new Identifier(SaveBed.MOD_ID, "open_take_ui"), buf);
        }
    }

    public static void pickupBed(ServerPlayerEntity owner, BlockPos pos) {
        World world = owner.getWorld();
        BlockEntity be = world.getBlockEntity(pos);
        if (be instanceof EnchantedBedBlockEntity) {
            EnchantedBedBlockEntity bedBe = (EnchantedBedBlockEntity) be;
            if (bedBe.isOwner(owner)) {
                ItemStack stack = new ItemStack(SaveBed.ENCHANTED_BED);
                NbtCompound nbt = stack.getOrCreateNbt();
                if (bedBe.getOwnerUuid() != null) {
                    nbt.putUuid("OwnerUUID", bedBe.getOwnerUuid());
                }
                if (bedBe.getOwnerName() != null) {
                    nbt.putString("OwnerName", bedBe.getOwnerName());
                }

                BlockState state = world.getBlockState(pos);
                if (state.getBlock() instanceof EnchantedBedBlock) {
                    BlockPos otherPos = pos.offset(BedBlock.getOppositePartDirection(state));
                    // Remove block entities to prevent item drops in onStateReplaced
                    world.removeBlockEntity(pos);
                    world.removeBlockEntity(otherPos);
                    // Set both to air
                    world.setBlockState(pos, net.minecraft.block.Blocks.AIR.getDefaultState(), net.minecraft.block.Block.NOTIFY_ALL);
                    world.setBlockState(otherPos, net.minecraft.block.Blocks.AIR.getDefaultState(), net.minecraft.block.Block.NOTIFY_ALL);
                }

                // Give item back to the owner directly
                owner.getInventory().insertStack(stack);

                // Clear the active bed location on the owner
                PlayerBedTracker tracker = (PlayerBedTracker) owner;
                tracker.save_bed$setActiveBedPos(null);
                tracker.save_bed$setActiveBedWorld(null);
            }
        }
    }

    public static void removeEnchantedBed(ServerPlayerEntity player) {
        UUID ownerUuid = player.getUuid();

        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (isMatchingBed(stack, ownerUuid)) {
                player.getInventory().setStack(i, ItemStack.EMPTY);
            }
        }

        for (int i = 0; i < player.getEnderChestInventory().size(); i++) {
            ItemStack stack = player.getEnderChestInventory().getStack(i);
            if (isMatchingBed(stack, ownerUuid)) {
                player.getEnderChestInventory().setStack(i, ItemStack.EMPTY);
            }
        }

        if (player.currentScreenHandler != null) {
            ItemStack cursorStack = player.currentScreenHandler.getCursorStack();
            if (isMatchingBed(cursorStack, ownerUuid)) {
                player.currentScreenHandler.setCursorStack(ItemStack.EMPTY);
            }
        }

        for (ServerWorld world : player.server.getWorlds()) {
            for (Entity entity : world.iterateEntities()) {
                if (entity instanceof ItemEntity) {
                    ItemEntity itemEntity = (ItemEntity) entity;
                    if (isMatchingBed(itemEntity.getStack(), ownerUuid)) {
                        itemEntity.discard();
                    }
                }
            }
        }

        for (ServerWorld world : player.server.getWorlds()) {
            ServerChunkManager chunkManager = world.getChunkManager();
            for (ChunkHolder holder : chunkManager.threadedAnvilChunkStorage.entryIterator()) {
                WorldChunk chunk = holder.getWorldChunk();
                if (chunk != null) {
                    for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
                        if (blockEntity instanceof Inventory) {
                            Inventory inventory = (Inventory) blockEntity;
                            boolean dirty = false;
                            for (int i = 0; i < inventory.size(); i++) {
                                ItemStack stack = inventory.getStack(i);
                                if (isMatchingBed(stack, ownerUuid)) {
                                    inventory.setStack(i, ItemStack.EMPTY);
                                    dirty = true;
                                }
                            }
                            if (dirty) {
                                blockEntity.markDirty();
                            }
                        }
                    }
                }
            }
        }
    }

    private static boolean isMatchingBed(ItemStack stack, UUID ownerUuid) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        if (stack.getItem() != SaveBed.ENCHANTED_BED) {
            return false;
        }
        if (!stack.hasNbt()) {
            return false;
        }
        NbtCompound nbt = stack.getNbt();
        if (nbt == null) {
            return false;
        }
        if (nbt.contains("OwnerUUID")) {
            return nbt.getUuid("OwnerUUID").equals(ownerUuid);
        }
        return false;
    }
}
