package com.save_bed.client.screen;

import com.save_bed.SaveBed;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

@Environment(EnvType.CLIENT)
public class TakeBedScreen extends Screen {
    private final BlockPos pos;

    public TakeBedScreen(BlockPos pos) {
        super(Text.literal("Enchanted Bed Options"));
        this.pos = pos;
    }

    @Override
    protected void init() {
        int x = this.width / 2 - 50;
        int y = this.height / 2 - 10;

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Take"), button -> {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeBlockPos(this.pos);
            ClientPlayNetworking.send(new Identifier(SaveBed.MOD_ID, "take_bed"), buf);
            this.close();
        }).dimensions(x, y, 100, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);
        super.render(context, mouseX, mouseY, delta);
    }
}
