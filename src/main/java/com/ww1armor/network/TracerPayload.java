package com.ww1armor.network;

import com.ww1armor.ArmoredCarMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Server -> client: a tracer round left the car's MG from (sx,sy,sz) and stopped at (ex,ey,ez). */
public record TracerPayload(int carId, double sx, double sy, double sz, double ex, double ey, double ez)
        implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<TracerPayload> TYPE =
            new CustomPacketPayload.Type<>(ArmoredCarMod.id("tracer"));
    public static final StreamCodec<RegistryFriendlyByteBuf, TracerPayload> CODEC = StreamCodec.of(
            (buf, p) -> {
                buf.writeVarInt(p.carId);
                buf.writeDouble(p.sx); buf.writeDouble(p.sy); buf.writeDouble(p.sz);
                buf.writeDouble(p.ex); buf.writeDouble(p.ey); buf.writeDouble(p.ez);
            },
            buf -> new TracerPayload(buf.readVarInt(), buf.readDouble(), buf.readDouble(), buf.readDouble(),
                    buf.readDouble(), buf.readDouble(), buf.readDouble()));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
