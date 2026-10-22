package com.exodusvehicles.item;

import com.exodusvehicles.entity.ArmoredCarEntity;
import com.exodusvehicles.entity.HullSegmentEntity;
import com.exodusvehicles.entity.MorserEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * An item that is held on a vehicle (hold right-click while looking at it) and works on it a little every tick:
 * the repair wrench and the jerry can. Stops as soon as you look away, walk off or let go.
 */
public abstract class VehicleToolItem extends Item {
    public static final double REACH = 4.5;

    protected VehicleToolItem(Properties properties) {
        super(properties);
    }

    /** The vehicle (car, lorry, howitzer) the player is looking at, within reach. */
    @Nullable
    public static Entity targetVehicle(Player player) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getViewVector(1.0F);
        Vec3 end = eye.add(look.scale(REACH));
        EntityHitResult hit = ProjectileUtil.getEntityHitResult(player.level(), player, eye, end,
                player.getBoundingBox().expandTowards(look.scale(REACH)).inflate(1.0),
                e -> (e instanceof ArmoredCarEntity || e instanceof MorserEntity || e instanceof HullSegmentEntity
                        || e instanceof com.exodusvehicles.entity.FieldMgEntity || e instanceof com.exodusvehicles.entity.PlaneEntity)
                        && !e.isRemoved() && !player.isPassengerOfSameVehicle(e), 0.0F);
        if (hit == null) {
            return null;
        }
        Entity e = hit.getEntity();
        return e instanceof HullSegmentEntity seg ? seg.getCar() : e;
    }

    /** Can this item work on that vehicle right now? (Also decides whether holding right-click starts at all.) */
    protected abstract boolean canWorkOn(Player player, ItemStack stack, Entity vehicle);

    /** One tick of work (server side). useTicks counts up from 1. */
    protected abstract void workTick(Player player, ItemStack stack, Entity vehicle, int useTicks);

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        Entity target = targetVehicle(player);
        if (target == null || !canWorkOn(player, stack, target)) {
            return InteractionResultHolder.pass(stack);
        }
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    @Override
    public void onUseTick(Level level, LivingEntity user, ItemStack stack, int remaining) {
        if (!(user instanceof Player player)) {
            return;
        }
        Entity target = targetVehicle(player);
        if (target == null || !canWorkOn(player, stack, target)) {
            player.stopUsingItem();
            return;
        }
        if (!level.isClientSide) {
            workTick(player, stack, target, getUseDuration(stack, user) - remaining);
        }
    }
}
