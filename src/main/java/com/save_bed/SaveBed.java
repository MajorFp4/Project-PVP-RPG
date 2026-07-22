package com.save_bed;

import com.save_bed.block.EnchantedBedBlock;
import com.save_bed.block.entity.EnchantedBedBlockEntity;
import com.save_bed.item.EnchantedBedItem;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SaveBed implements ModInitializer {
	public static final String MOD_ID = "save_bed";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final Block ENCHANTED_BED_BLOCK = new EnchantedBedBlock(
			AbstractBlock.Settings.create()
					.mapColor(MapColor.RED)
					.sounds(BlockSoundGroup.WOOD)
					.strength(0.2F)
					.nonOpaque()
					.burnable()
	);

	public static final BlockEntityType<EnchantedBedBlockEntity> ENCHANTED_BED_BLOCK_ENTITY = FabricBlockEntityTypeBuilder.create(
			EnchantedBedBlockEntity::new, ENCHANTED_BED_BLOCK
	).build();

	public static final Item ENCHANTED_BED = new EnchantedBedItem(new Item.Settings().maxCount(1));

	@Override
	public void onInitialize() {
		Registry.register(Registries.BLOCK, new Identifier(MOD_ID, "enchanted_bed"), ENCHANTED_BED_BLOCK);
		Registry.register(Registries.BLOCK_ENTITY_TYPE, new Identifier(MOD_ID, "enchanted_bed"), ENCHANTED_BED_BLOCK_ENTITY);
		Registry.register(Registries.ITEM, new Identifier(MOD_ID, "enchanted_bed"), ENCHANTED_BED);
		
		// Register play events and logic
		com.save_bed.logic.BedLogic.register();
	}

	public static Identifier id(String path) {
		return new Identifier(MOD_ID, path);
	}
}
