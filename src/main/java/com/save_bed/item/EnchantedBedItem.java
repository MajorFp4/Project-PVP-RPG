package com.save_bed.item;

import com.save_bed.SaveBed;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.BedItem;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.nbt.NbtCompound;

import java.util.List;

public class EnchantedBedItem extends BedItem {
    public EnchantedBedItem(Item.Settings settings) {
        super(SaveBed.ENCHANTED_BED_BLOCK, settings);
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return true;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        NbtCompound nbt = stack.getNbt();
        if (nbt != null && nbt.contains("OwnerName")) {
            String ownerName = nbt.getString("OwnerName");
            tooltip.add(Text.literal(ownerName + " owns this bed").formatted(Formatting.GRAY));
        } else {
            tooltip.add(Text.literal("Unowned bed").formatted(Formatting.GRAY));
        }
        super.appendTooltip(stack, world, tooltip, context);
    }
}
