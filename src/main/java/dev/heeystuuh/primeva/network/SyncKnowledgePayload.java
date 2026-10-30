package dev.heeystuuh.primeva.network;

import dev.heeystuuh.primeva.PrimevaMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record SyncKnowledgePayload(CompoundTag tag) implements CustomPacketPayload {
    public static final Type<SyncKnowledgePayload> TYPE = new Type<>(PrimevaMod.id("sync_knowledge"));
    public static final StreamCodec<ByteBuf, SyncKnowledgePayload> CODEC =
            StreamCodec.composite(ByteBufCodecs.COMPOUND_TAG, SyncKnowledgePayload::tag, SyncKnowledgePayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
