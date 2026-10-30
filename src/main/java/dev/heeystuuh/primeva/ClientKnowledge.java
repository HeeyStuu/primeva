package dev.heeystuuh.primeva;

import net.minecraft.nbt.CompoundTag;

/** Cópia do conhecimento do jogador no cliente (atualizada pelo servidor). Sem classes de cliente aqui. */
public final class ClientKnowledge {
    private static Knowledge current = new Knowledge();

    public static Knowledge get() {
        return current;
    }

    public static void update(CompoundTag tag) {
        Knowledge k = new Knowledge();
        k.deserializeNBT(null, tag);
        current = k;
    }

    private ClientKnowledge() {}
}
