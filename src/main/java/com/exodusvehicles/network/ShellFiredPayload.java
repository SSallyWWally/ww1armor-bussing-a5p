package com.exodusvehicles.network;

import com.exodusvehicles.ArmoredCarMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Server -> client: an artillery shell left gun gunId; clients fly a visual copy (trail, shell camera). */
public record ShellFiredPayload(int shellId, int gunId, double x, double y, double z, double vx, double vy, double vz)
        implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ShellFiredPayload> TYPE =
            new CustomPacketPayload.Type<>(ArmoredCarMod.id("shell_fired"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ShellFiredPayload> CODEC = StreamCodec.of(
            (buf, p) -> {
                buf.writeVarInt(p.shellId);
                buf.writeVarInt(p.gunId);
                buf.writeDouble(p.x); buf.writeDouble(p.y); buf.writeDouble(p.z);
                buf.writeDouble(p.vx); buf.writeDouble(p.vy); buf.writeDouble(p.vz);
            },
            buf -> new ShellFiredPayload(buf.readVarInt(), buf.readVarInt(), buf.readDouble(), buf.readDouble(), buf.readDouble(),
                    buf.readDouble(), buf.readDouble(), buf.readDouble()));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
