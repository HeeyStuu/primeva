package dev.heeystuuh.primeva.network;

import dev.heeystuuh.primeva.PrimevaMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.List;

/** Pedido de pesquisa com a solução do quebra-cabeça (casa*8 + essência). */
public record ResearchPayload(String id, List<Integer> placements) implements CustomPacketPayload {
    public static final Type<ResearchPayload> TYPE = new Type<>(PrimevaMod.id("research"));
    public static final StreamCodec<ByteBuf, ResearchPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, ResearchPayload::id,
            ByteBufCodecs.VAR_INT.apply(ByteBufCodecs.list(64)), ResearchPayload::placements,
            ResearchPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
