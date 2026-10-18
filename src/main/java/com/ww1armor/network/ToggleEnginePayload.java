package com.ww1armor.network;

import com.ww1armor.ArmoredCarMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Client -> server: driver pressed the engine start/stop key. */
public record ToggleEnginePayload() implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ToggleEnginePayload> TYPE =
            new CustomPacketPayload.Type<>(ArmoredCarMod.id("toggle_engine"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ToggleEnginePayload> CODEC =
            StreamCodec.unit(new ToggleEnginePayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
