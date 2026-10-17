package com.ww1armor.network;

import com.ww1armor.ArmoredCarMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Client -> server: move me to the next free seat. */
public record SwitchSeatPayload() implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SwitchSeatPayload> TYPE =
            new CustomPacketPayload.Type<>(ArmoredCarMod.id("switch_seat"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SwitchSeatPayload> CODEC =
            StreamCodec.unit(new SwitchSeatPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
