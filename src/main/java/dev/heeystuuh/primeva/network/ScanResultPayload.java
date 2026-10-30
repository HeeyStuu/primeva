package dev.heeystuuh.primeva.network;

import dev.heeystuuh.primeva.PrimevaMod;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Resultado de um escaneamento, para o painel animado no cliente. */
public record ScanResultPayload(Component name, CompoundTag essences, boolean isNew) implements CustomPacketPayload {
    public static final Type<ScanResultPayload> TYPE = new Type<>(PrimevaMod.id("scan_result"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ScanResultPayload> CODEC = StreamCodec.composite(
            ComponentSerialization.TRUSTED_STREAM_CODEC, ScanResultPayload::name,
            ByteBufCodecs.COMPOUND_TAG, ScanResultPayload::essences,
            ByteBufCodecs.BOOL, ScanResultPayload::isNew,
            ScanResultPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
