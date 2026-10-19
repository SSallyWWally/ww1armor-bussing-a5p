package com.exodusvehicles.network;

import com.exodusvehicles.ArmoredCarMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Server -> client: your shell hit someone (kill = it killed / wrecked them). */
public record HitMarkerPayload(boolean kill) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<HitMarkerPayload> TYPE =
            new CustomPacketPayload.Type<>(ArmoredCarMod.id("hit_marker"));
    public static final StreamCodec<RegistryFriendlyByteBuf, HitMarkerPayload> CODEC = StreamCodec.of(
            (buf, p) -> buf.writeBoolean(p.kill),
            buf -> new HitMarkerPayload(buf.readBoolean()));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
