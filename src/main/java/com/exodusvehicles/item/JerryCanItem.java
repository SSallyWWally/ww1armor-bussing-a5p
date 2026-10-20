package com.exodusvehicles.item;

import com.exodusvehicles.ArmoredCarMod;
import com.exodusvehicles.ModConfig;
import com.exodusvehicles.entity.ArmoredCarEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;

import java.util.List;

/**
 * 20 litre jerry can of petrol. Hold right-click on a vehicle to pour it in (slowly). The fuel left is kept as the
 * item's wear (one point per decilitre); an emptied can turns into an empty can, which can be refilled by crafting.
 */
public class JerryCanItem extends VehicleToolItem {
    public static final int DECILITRES = 200;

    public JerryCanItem(Properties properties) {
        super(properties);
    }

    public static float litres(ItemStack stack) {
        return (DECILITRES - stack.getDamageValue()) / 10.0F;
    }

    @Override
    protected boolean canWorkOn(Player player, ItemStack stack, Entity vehicle) {
        return vehicle instanceof ArmoredCarEntity car && !car.isWrecked() && car.getFuel() < car.fuelCapacity() - 0.05F;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.NONE;
    }

    @Override
    protected void workTick(Player player, ItemStack stack, Entity vehicle, int useTicks) {
        ArmoredCarEntity car = (ArmoredCarEntity) vehicle;
        float perTick = ModConfig.get().refuelLitresPerSecond / 20.0F;
        float room = car.fuelCapacity() - car.getFuel();
        float pour = Math.min(perTick, room);
        boolean creative = player.getAbilities().instabuild;
        if (!creative) {
            // pour whole decilitres from the can as the vehicle's tank fills
            float owed = stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA,
                    net.minecraft.world.item.component.CustomData.EMPTY).copyTag().getFloat("Pour") + pour * 10.0F;
            int whole = (int) owed;
            whole = Math.min(whole, DECILITRES - stack.getDamageValue());
            stack.setDamageValue(stack.getDamageValue() + whole);
            net.minecraft.nbt.CompoundTag t = new net.minecraft.nbt.CompoundTag();
            t.putFloat("Pour", owed - whole);
            stack.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.of(t));
            if (stack.getDamageValue() >= DECILITRES) {
                car.addFuel(pour);
                player.setItemInHand(player.getUsedItemHand(), new ItemStack(ArmoredCarMod.JERRY_CAN_EMPTY));
                player.stopUsingItem();
                player.displayClientMessage(Component.translatable("msg.exodus_vehicles.can_empty"), true);
                return;
            }
        }
        car.addFuel(pour);
        if (useTicks % 30 == 1) {
            vehicle.level().playSound(null, player.getX(), player.getY() + 1.0, player.getZ(), ArmoredCarMod.REFUEL,
                    SoundSource.PLAYERS, 0.9F, 0.95F + player.getRandom().nextFloat() * 0.1F);
        }
        if (useTicks % 20 == 0 || car.getFuel() >= car.fuelCapacity() - 0.05F) {
            player.displayClientMessage(Component.translatable("msg.exodus_vehicles.refuelling",
                    String.format("%.1f", car.getFuel()), Math.round(car.fuelCapacity())), true);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        lines.add(Component.translatable("item.exodus_vehicles.jerry_can.litres", String.format("%.1f", litres(stack)))
                .withStyle(ChatFormatting.GRAY));
        lines.add(Component.translatable("item.exodus_vehicles.jerry_can.hint").withStyle(ChatFormatting.DARK_GRAY));
    }
}
