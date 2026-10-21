package com.exodusvehicles.artillery;

import com.mojang.brigadier.arguments.BoolArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.GameRules;

/**
 * Per-world switch for artillery craters: the game rule exodusArtilleryCrater (default on), also settable with
 * /exodusartillerycrater true|false (operators). /exodusartillerycrater on its own reports the current setting.
 */
public final class ArtilleryRules {
    public static GameRules.Key<GameRules.BooleanValue> CRATERS;

    private ArtilleryRules() {}

    public static void register() {
        CRATERS = GameRuleRegistry.register("exodusArtilleryCrater", GameRules.Category.MISC, GameRuleFactory.createBooleanRule(true));
        CommandRegistrationCallback.EVENT.register((dispatcher, registry, env) -> dispatcher.register(
                Commands.literal("exodusartillerycrater")
                        .requires(src -> src.hasPermission(2))
                        .executes(ctx -> {
                            boolean on = ctx.getSource().getServer().getGameRules().getBoolean(CRATERS);
                            ctx.getSource().sendSuccess(() -> Component.translatable(
                                    on ? "commands.exodus_vehicles.crater.is_on" : "commands.exodus_vehicles.crater.is_off"), false);
                            return on ? 1 : 0;
                        })
                        .then(Commands.argument("enabled", BoolArgumentType.bool())
                                .executes(ctx -> {
                                    boolean on = BoolArgumentType.getBool(ctx, "enabled");
                                    ctx.getSource().getServer().getGameRules().getRule(CRATERS).set(on, ctx.getSource().getServer());
                                    ctx.getSource().sendSuccess(() -> Component.translatable(
                                            on ? "commands.exodus_vehicles.crater.on" : "commands.exodus_vehicles.crater.off"), true);
                                    return 1;
                                }))));
    }

    public static boolean craters(net.minecraft.server.level.ServerLevel level) {
        return CRATERS == null || level.getGameRules().getBoolean(CRATERS);
    }
}
