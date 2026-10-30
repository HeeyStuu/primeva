package dev.heeystuuh.primeva;

import com.mojang.serialization.Codec;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public final class ModAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, PrimevaMod.MODID);

    /** Conhecimento do jogador (mantido ao morrer). */
    public static final Supplier<AttachmentType<Knowledge>> KNOWLEDGE = ATTACHMENTS.register("knowledge",
            () -> AttachmentType.serializable(() -> new Knowledge()).copyOnDeath().build());

    /** Miasma acumulado em cada chunk. */
    public static final Supplier<AttachmentType<Integer>> MIASMA = ATTACHMENTS.register("miasma",
            () -> AttachmentType.builder(() -> 0).serialize(Codec.INT).build());

    private ModAttachments() {}
}
