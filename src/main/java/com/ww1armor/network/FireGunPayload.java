package com.ww1armor.network;

import com.ww1armor.ArmoredCarMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Client -> server: gunner is holding the trigger this tick (server enforces fire rate). */
public record FireGunPayload() implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<FireGunPayload> TYPE =
            new CustomPacketPayload.Type<>(ArmoredCarMod.id("fire_gun"));
    public static final StreamCodec<RegistryFriendlyByteBuf, FireGunPayload> CODEC =
            StreamCodec.unit(new FireGunPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
