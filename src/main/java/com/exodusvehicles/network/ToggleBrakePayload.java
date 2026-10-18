package com.exodusvehicles.network;

import com.exodusvehicles.ArmoredCarMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Client -> server: driver toggled the parking brake. */
public record ToggleBrakePayload() implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ToggleBrakePayload> TYPE =
            new CustomPacketPayload.Type<>(ArmoredCarMod.id("toggle_brake"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ToggleBrakePayload> CODEC =
            StreamCodec.unit(new ToggleBrakePayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
