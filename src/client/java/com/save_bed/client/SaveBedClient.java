package com.save_bed.client;

import com.save_bed.SaveBed;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.render.block.entity.BedBlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class SaveBedClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// Register the vanilla BedBlockEntityRenderer for our custom block entity type.
		// Without this, the bed block is invisible because beds are rendered entirely
		// by the block entity renderer, not by the block model.
		BlockEntityRendererFactories.register(SaveBed.ENCHANTED_BED_BLOCK_ENTITY, BedBlockEntityRenderer::new);

		// Register client packet receiver for opening the TakeBedScreen
		ClientPlayNetworking.registerGlobalReceiver(new Identifier(SaveBed.MOD_ID, "open_take_ui"), (client, handler, buf, responseSender) -> {
			BlockPos pos = buf.readBlockPos();
			client.execute(() -> {
				client.setScreen(new com.save_bed.client.screen.TakeBedScreen(pos));
			});
		});
	}
}