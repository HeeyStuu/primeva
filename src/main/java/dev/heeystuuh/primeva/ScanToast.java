package dev.heeystuuh.primeva;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

import java.util.EnumMap;
import java.util.Map;

/** Último resultado de escaneamento recebido (lido pelo HUD do cliente). Sem classes de cliente aqui. */
public final class ScanToast {
    public static Component name;
    public static final Map<Essence, Integer> essences = new EnumMap<>(Essence.class);
    public static boolean isNew;
    public static long shownAt = -100000;

    public static void show(Component n, CompoundTag tag, boolean fresh) {
        name = n;
        isNew = fresh;
        essences.clear();
        for (Essence e : Essence.values()) if (tag.contains(e.id)) essences.put(e, tag.getInt(e.id));
        shownAt = System.currentTimeMillis();
    }

    private ScanToast() {}
}
