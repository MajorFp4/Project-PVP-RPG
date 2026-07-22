package com.save_bed.client.mixin;

import com.save_bed.SaveBed;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.BuiltinModelItemRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(BuiltinModelItemRenderer.class)
public class BuiltinModelItemRendererMixin {

    @ModifyVariable(
        method = "render",
        at = @At("HEAD"),
        argsOnly = true,
        ordinal = 0
    )
    private VertexConsumerProvider wrapVertexConsumers(VertexConsumerProvider vertexConsumers, ItemStack stack, ModelTransformationMode mode, MatrixStack matrices) {
        if (stack != null && stack.getItem() == SaveBed.ENCHANTED_BED) {
            return layer -> ItemRenderer.getDirectItemGlintConsumer(vertexConsumers, layer, true, true);
        }
        return vertexConsumers;
    }
}
