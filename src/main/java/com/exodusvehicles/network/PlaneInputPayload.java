package com.exodusvehicles.network;

import com.exodusvehicles.ArmoredCarMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Pilot -> server, every tick: throttle and bank (so others see them), and whether the aircraft just crashed. */
public record PlaneInputPayload(float throttle, float roll, boolean crashed) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<PlaneInputPayload> TYPE = new CustomPacketPayload.Type<>(ArmoredCarMod.id("plane_input"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PlaneInputPayload> CODEC = StreamCodec.of(
            (buf, p) -> {
                buf.writeFloat(p.throttle);
                buf.writeFloat(p.roll);
                buf.writeBoolean(p.crashed);
            },
            buf -> new PlaneInputPayload(buf.readFloat(), buf.readFloat(), buf.readBoolean()));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
