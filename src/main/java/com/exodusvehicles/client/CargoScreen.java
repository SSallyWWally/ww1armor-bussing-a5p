package com.exodusvehicles.client;

import com.exodusvehicles.menu.CargoMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/** Double-chest-sized view of the lorry's crates, with pages when more than two crates are loaded. */
public class CargoScreen extends AbstractContainerScreen<CargoMenu> {
    private static final ResourceLocation BG = ResourceLocation.withDefaultNamespace("textures/gui/container/generic_54.png");
    private Button prev, next;

    public CargoScreen(CargoMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageHeight = 114 + 6 * 18;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        this.prev = this.addRenderableWidget(Button.builder(Component.literal("<"), b -> turn(CargoMenu.BUTTON_PREV))
                .bounds(this.leftPos + this.imageWidth - 50, this.topPos + 3, 12, 12).build());
        this.next = this.addRenderableWidget(Button.builder(Component.literal(">"), b -> turn(CargoMenu.BUTTON_NEXT))
                .bounds(this.leftPos + this.imageWidth - 16, this.topPos + 3, 12, 12).build());
    }

    private void turn(int button) {
        if (this.minecraft != null && this.minecraft.gameMode != null && this.menu.clickMenuButton(this.minecraft.player, button)) {
            this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, button);
        }
    }

    @Override
    public boolean mouseScrolled(double x, double y, double scrollX, double scrollY) {
        if (scrollY != 0 && this.menu.pages() > 1) {
            turn(scrollY > 0 ? CargoMenu.BUTTON_PREV : CargoMenu.BUTTON_NEXT);
            return true;
        }
        return super.mouseScrolled(x, y, scrollX, scrollY);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        boolean paged = this.menu.pages() > 1;
        this.prev.visible = paged;
        this.next.visible = paged;
        this.prev.active = this.menu.page() > 0;
        this.next.active = this.menu.page() < this.menu.pages() - 1;
        super.render(g, mouseX, mouseY, partialTick);
        this.renderTooltip(g, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        super.renderLabels(g, mouseX, mouseY);
        String cap = this.menu.capacity() + " slots";
        if (this.menu.pages() > 1) {
            String pg = (this.menu.page() + 1) + "/" + this.menu.pages();
            g.drawString(this.font, pg, this.imageWidth - 33 - this.font.width(pg) / 2 + 3, 5, 0x404040, false);
        }
        g.drawString(this.font, cap, this.imageWidth - 8 - this.font.width(cap), this.inventoryLabelY, 0x404040, false);
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos, y = this.topPos;
        g.blit(BG, x, y, 0, 0, this.imageWidth, 6 * 18 + 17);
        g.blit(BG, x, y + 6 * 18 + 17, 0, 126, this.imageWidth, 96);
        // slots past the loaded crates are boarded over
        int free = this.menu.capacity() - this.menu.page() * CargoMenu.PAGE_SLOTS;
        for (int i = Math.max(0, free); i < CargoMenu.PAGE_SLOTS; i++) {
            int sx = x + 7 + (i % 9) * 18, sy = y + 17 + (i / 9) * 18;
            g.fill(sx, sy, sx + 18, sy + 18, 0xFF6B5A44);
            g.fill(sx + 1, sy + 1, sx + 17, sy + 17, 0xFF8A7458);
        }
    }
}
