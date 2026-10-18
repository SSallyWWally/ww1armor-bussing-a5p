package com.ww1armor.network;

import com.ww1armor.ArmoredCarMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Client -> server: gunner asks to feed a new belt into the MG 08. */
public record ReloadPayload() implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ReloadPayload> TYPE =
            new CustomPacketPayload.Type<>(ArmoredCarMod.id("reload_gun"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ReloadPayload> CODEC =
            StreamCodec.unit(new ReloadPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
