package com.exodusvehicles.network;

import com.exodusvehicles.ArmoredCarMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Server -> client: a round just went close past you (amount of suppression it adds). */
public record SuppressPayload(float amount) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SuppressPayload> TYPE = new CustomPacketPayload.Type<>(ArmoredCarMod.id("suppress"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SuppressPayload> CODEC = StreamCodec.of(
            (buf, p) -> buf.writeFloat(p.amount), buf -> new SuppressPayload(buf.readFloat()));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
