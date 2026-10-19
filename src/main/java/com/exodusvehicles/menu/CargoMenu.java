package com.exodusvehicles.menu;

import com.exodusvehicles.ArmoredCarMod;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * The lorry's supply crates as one inventory: every crate is a full chest (27 slots), shown 54 at a time in pages.
 * data[0] = crates loaded, data[1] = page shown (both synced to the client).
 */
public class CargoMenu extends AbstractContainerMenu {
    public static final int SLOTS_PER_CRATE = 27;
    public static final int PAGE_SLOTS = 54;
    public static final int BUTTON_PREV = 0, BUTTON_NEXT = 1;

    private final Container cargo;
    private final ContainerData data;

    /** Client side. */
    public CargoMenu(int id, Inventory inventory) {
        this(id, inventory, new SimpleContainer(6 * SLOTS_PER_CRATE), new SimpleContainerData(2));
    }

    public CargoMenu(int id, Inventory inventory, Container cargo, ContainerData data) {
        super(ArmoredCarMod.CARGO_MENU, id);
        this.cargo = cargo;
        this.data = data;
        cargo.startOpen(inventory.player);
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new PagedSlot(row * 9 + col, 8 + col * 18, 18 + row * 18));
            }
        }
        int y0 = 18 + 6 * 18 + 13;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, y0 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(inventory, col, 8 + col * 18, y0 + 58));
        }
        this.addDataSlots(data);
    }

    public int crates() {
        return this.data.get(0);
    }

    public int capacity() {
        return Math.min(this.cargo.getContainerSize(), crates() * SLOTS_PER_CRATE);
    }

    public int page() {
        return this.data.get(1);
    }

    public int pages() {
        return Math.max(1, (capacity() + PAGE_SLOTS - 1) / PAGE_SLOTS);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        int p = page() + (id == BUTTON_NEXT ? 1 : id == BUTTON_PREV ? -1 : 0);
        p = Math.max(0, Math.min(pages() - 1, p));
        if (p != page()) {
            this.data.set(1, p);
            // every screen slot now shows a different cargo slot: resend the whole page
            this.sendAllDataToRemote();
        }
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        if (index < PAGE_SLOTS) {
            if (!this.moveItemStackTo(stack, PAGE_SLOTS, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!this.moveItemStackTo(stack, 0, PAGE_SLOTS, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return copy;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.cargo.stillValid(player);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.cargo.stopOpen(player);
    }

    /** A screen slot that shows whichever cargo slot the current page puts under it. */
    private class PagedSlot extends Slot {
        private final int onPage;

        PagedSlot(int onPage, int x, int y) {
            super(CargoMenu.this.cargo, onPage, x, y);
            this.onPage = onPage;
        }

        private int index() {
            return page() * PAGE_SLOTS + this.onPage;
        }

        @Override
        public boolean isActive() {
            return index() < capacity();
        }

        @Override
        public int getContainerSlot() {
            return index();
        }

        @Override
        public ItemStack getItem() {
            return isActive() ? this.container.getItem(index()) : ItemStack.EMPTY;
        }

        @Override
        public boolean hasItem() {
            return !getItem().isEmpty();
        }

        @Override
        public void set(ItemStack stack) {
            if (isActive()) {
                this.container.setItem(index(), stack);
                this.setChanged();
            }
        }

        @Override
        public ItemStack remove(int amount) {
            return isActive() ? this.container.removeItem(index(), amount) : ItemStack.EMPTY;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return isActive() && super.mayPlace(stack);
        }

        @Override
        public boolean mayPickup(Player player) {
            return isActive();
        }
    }
}
