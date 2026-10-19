package com.exodusvehicles.client;

import com.exodusvehicles.entity.MorserEntity;
import com.exodusvehicles.network.SetTargetPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * The battery's plotting board, simplified: enter the target's X / Z (from an observer's spyglass) and any
 * corrections the observer calls ("add 30", "right 10"); the gun works out bearing, charge and elevation itself.
 */
public class FireControlScreen extends Screen {
    private final MorserEntity gun;
    private EditBox x, z, add, right;

    public FireControlScreen(MorserEntity gun) {
        super(Component.translatable("screen.exodus_vehicles.fire_control"));
        this.gun = gun;
    }

    @Override
    protected void init() {
        int cx = this.width / 2, y = this.height / 2 - 50;
        this.x = field(cx - 105, y, this.gun.hasTarget() ? this.gun.targetX() : (int) Math.floor(this.gun.getX()));
        this.z = field(cx + 5, y, this.gun.hasTarget() ? this.gun.targetZ() : (int) Math.floor(this.gun.getZ()));
        this.add = field(cx - 105, y + 44, this.gun.corrAdd());
        this.right = field(cx + 5, y + 44, this.gun.corrRight());
        this.addRenderableWidget(Button.builder(Component.translatable("screen.exodus_vehicles.lay"), b -> send(false))
                .bounds(cx - 105, y + 78, 100, 20).build());
        this.addRenderableWidget(Button.builder(Component.translatable("screen.exodus_vehicles.clear_corr"), b -> send(true))
                .bounds(cx + 5, y + 78, 100, 20).build());
        this.setInitialFocus(this.x);
    }

    private EditBox field(int px, int py, int value) {
        EditBox b = new EditBox(this.font, px, py, 100, 18, Component.empty());
        b.setMaxLength(8);
        b.setValue(String.valueOf(value));
        b.setFilter(s -> s.isEmpty() || s.equals("-") || s.equals("+") || s.matches("[+-]?\\d{0,7}"));
        return this.addRenderableWidget(b);
    }

    private static int num(EditBox b) {
        try {
            return Integer.parseInt(b.getValue().replace("+", ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void send(boolean clearCorrections) {
        if (clearCorrections) {
            this.add.setValue("0");
            this.right.setValue("0");
        }
        ClientPlayNetworking.send(new SetTargetPayload(num(this.x), num(this.z), num(this.add), num(this.right)));
        this.onClose();
    }

    @Override
    public boolean keyPressed(int key, int scan, int mods) {
        if (key == org.lwjgl.glfw.GLFW.GLFW_KEY_ENTER || key == org.lwjgl.glfw.GLFW.GLFW_KEY_KP_ENTER) {
            send(false);
            return true;
        }
        return super.keyPressed(key, scan, mods);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        int cx = this.width / 2, y = this.height / 2 - 50;
        g.drawCenteredString(this.font, this.title, cx, y - 34, 0xFFE8E4D0);
        g.drawCenteredString(this.font, Component.translatable("screen.exodus_vehicles.gun_at",
                (int) Math.floor(this.gun.getX()), (int) Math.floor(this.gun.getZ())), cx, y - 22, 0xFFB9B6A3);
        g.drawString(this.font, Component.translatable("screen.exodus_vehicles.target_x"), cx - 105, y - 10, 0xFFE8E4D0);
        g.drawString(this.font, Component.translatable("screen.exodus_vehicles.target_z"), cx + 5, y - 10, 0xFFE8E4D0);
        g.drawString(this.font, Component.translatable("screen.exodus_vehicles.add_drop"), cx - 105, y + 34, 0xFFE8E4D0);
        g.drawString(this.font, Component.translatable("screen.exodus_vehicles.right_left"), cx + 5, y + 34, 0xFFE8E4D0);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
