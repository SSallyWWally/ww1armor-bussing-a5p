package com.ww1armor.item;

import com.ww1armor.ArmoredCarMod;
import com.ww1armor.entity.ArmoredCarEntity;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class ArmoredCarItem extends Item {
    public ArmoredCarItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        Player player = ctx.getPlayer();
        Vec3 at = ctx.getClickLocation();
        if (ctx.getClickedFace() != Direction.UP) {
            at = Vec3.atBottomCenterOf(ctx.getClickedPos().relative(ctx.getClickedFace()));
        }
        if (level instanceof ServerLevel server) {
            ArmoredCarEntity car = ArmoredCarMod.BUSSING_A5P.create(server);
            if (car == null) {
                return InteractionResult.FAIL;
            }
            float yaw = player != null ? player.getYRot() : 0.0F;
            car.moveTo(at.x, at.y, at.z, yaw, 0.0F);
            if (!level.noCollision(car, car.getBoundingBox().deflate(0.05))) {
                if (player != null) {
                    player.displayClientMessage(Component.translatable("msg.ww1armor.blocked"), true);
                }
                return InteractionResult.FAIL;
            }
            server.addFreshEntity(car);
            if (player == null || !player.getAbilities().instabuild) {
                ctx.getItemInHand().shrink(1);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
