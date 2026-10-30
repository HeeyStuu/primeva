package dev.heeystuuh.primeva.network;

import dev.heeystuuh.primeva.PrimevaMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Nível de miasma onde o jogador está (para a névoa do cliente). */
public record MiasmaPayload(int level) implements CustomPacketPayload {
    public static final Type<MiasmaPayload> TYPE = new Type<>(PrimevaMod.id("miasma"));
    public static final StreamCodec<ByteBuf, MiasmaPayload> CODEC =
            StreamCodec.composite(ByteBufCodecs.VAR_INT, MiasmaPayload::level, MiasmaPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
