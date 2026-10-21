package com.exodusvehicles.network;

import com.exodusvehicles.ArmoredCarMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Server -> client: your shell hit someone (kill = it killed / wrecked them; line = which kill call-out, -1 = default). */
public record HitMarkerPayload(boolean kill, int line) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<HitMarkerPayload> TYPE =
            new CustomPacketPayload.Type<>(ArmoredCarMod.id("hit_marker"));
    public static final StreamCodec<RegistryFriendlyByteBuf, HitMarkerPayload> CODEC = StreamCodec.of(
            (buf, p) -> {
                buf.writeBoolean(p.kill);
                buf.writeVarInt(p.line + 1);
            },
            buf -> new HitMarkerPayload(buf.readBoolean(), buf.readVarInt() - 1));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
