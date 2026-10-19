package com.exodusvehicles.network;

import com.exodusvehicles.ArmoredCarMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Server -> client: shell shellId burst at x,y,z (ends the visual shell / shell camera; shake and shell shock nearby). */
public record ShellImpactPayload(int shellId, double x, double y, double z, float radius) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ShellImpactPayload> TYPE =
            new CustomPacketPayload.Type<>(ArmoredCarMod.id("shell_impact"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ShellImpactPayload> CODEC = StreamCodec.of(
            (buf, p) -> {
                buf.writeVarInt(p.shellId);
                buf.writeDouble(p.x); buf.writeDouble(p.y); buf.writeDouble(p.z);
                buf.writeFloat(p.radius);
            },
            buf -> new ShellImpactPayload(buf.readVarInt(), buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readFloat()));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
