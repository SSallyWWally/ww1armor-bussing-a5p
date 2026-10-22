package com.exodusvehicles.artillery;

import com.exodusvehicles.network.SuppressPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Who is being shot at. Rounds passing close add to a player's suppression (0..1); it holds for a second after the
 * last round and then wears off over a couple of seconds. The client draws the effects; here it shakes a suppressed
 * gunner's own aim.
 */
public final class Suppression {
    private static final Map<UUID, float[]> LEVELS = new HashMap<>();     // {level, last game tick}
    private static final int HOLD = 20;
    private static final float DECAY = 0.025F;

    private Suppression() {}

    public static float level(Player p) {
        float[] s = LEVELS.get(p.getUUID());
        if (s == null) {
            return 0.0F;
        }
        long since = p.level().getGameTime() - (long) s[1];
        return Math.max(0.0F, s[0] - Math.max(0, since - HOLD) * DECAY);
    }

    public static void add(ServerPlayer p, float amount) {
        float now = Math.min(1.0F, level(p) + amount);
        LEVELS.put(p.getUUID(), new float[]{now, p.level().getGameTime()});
        if (LEVELS.size() > 512) {
            LEVELS.entrySet().removeIf(e -> e.getValue()[0] <= 0.0F);
        }
        ServerPlayNetworking.send(p, new SuppressPayload(amount));
    }
}
