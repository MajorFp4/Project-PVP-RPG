package com.save_bed.client.mixin;

import com.save_bed.block.entity.EnchantedBedBlockEntity;
import net.minecraft.block.entity.BedBlockEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BedBlockEntityRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(BedBlockEntityRenderer.class)
public class BedBlockEntityRendererMixin {

    @ModifyVariable(
        method = "render(Lnet/minecraft/block/entity/BedBlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;II)V",
        at = @At("HEAD"),
        argsOnly = true,
        ordinal = 0
    )
    private VertexConsumerProvider wrapVertexConsumers(VertexConsumerProvider vertexConsumers, BedBlockEntity blockEntity) {
        if (blockEntity instanceof EnchantedBedBlockEntity) {
            return layer -> ItemRenderer.getDirectItemGlintConsumer(vertexConsumers, layer, true, true);
        }
        return vertexConsumers;
    }
}
