package com.exodusvehicles.item;

import com.exodusvehicles.ArmoredCarMod;
import com.exodusvehicles.ModConfig;
import com.exodusvehicles.entity.Repairable;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;

/** Repair wrench: hold right-click on a damaged vehicle or gun; slow work (1 hp a second by default). */
public class WrenchItem extends VehicleToolItem {
    public WrenchItem(Properties properties) {
        super(properties);
    }

    @Override
    protected boolean canWorkOn(Player player, ItemStack stack, Entity vehicle) {
        return vehicle instanceof Repairable r && r.getHealth() < r.getMaxHealth();
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BRUSH;
    }

    @Override
    protected void workTick(Player player, ItemStack stack, Entity vehicle, int useTicks) {
        Repairable r = (Repairable) vehicle;
        if (!r.repairBy(ModConfig.get().repairPerSecond / 20.0F)) {
            player.stopUsingItem();
            return;
        }
        if (useTicks % 16 == 1) {
            vehicle.level().playSound(null, player.getX(), player.getY() + 1.0, player.getZ(), ArmoredCarMod.REPAIR,
                    SoundSource.PLAYERS, 0.8F, 0.95F + player.getRandom().nextFloat() * 0.1F);
        }
        if (useTicks % 20 == 0) {
            if (!player.getAbilities().instabuild) {
                stack.hurtAndBreak(1, player, player.getUsedItemHand() == net.minecraft.world.InteractionHand.MAIN_HAND
                        ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
            }
            player.displayClientMessage(Component.translatable("msg.exodus_vehicles.repairing",
                    (int) Math.floor(r.getHealth()), (int) r.getMaxHealth()), true);
        }
    }
}
