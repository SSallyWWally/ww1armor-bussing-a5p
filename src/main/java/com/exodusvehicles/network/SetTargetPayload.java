package com.exodusvehicles.network;

import com.exodusvehicles.ArmoredCarMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Client -> server: fire-control entry for the howitzer (target X/Z, range and lateral correction in blocks). */
public record SetTargetPayload(int x, int z, int addRange, int right) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SetTargetPayload> TYPE =
            new CustomPacketPayload.Type<>(ArmoredCarMod.id("set_target"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SetTargetPayload> CODEC = StreamCodec.of(
            (buf, p) -> {
                buf.writeInt(p.x);
                buf.writeInt(p.z);
                buf.writeInt(p.addRange);
                buf.writeInt(p.right);
            },
            buf -> new SetTargetPayload(buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt()));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
