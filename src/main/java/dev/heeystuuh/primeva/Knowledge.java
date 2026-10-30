package dev.heeystuuh.primeva;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.neoforged.neoforge.common.util.INBTSerializable;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Set;

/** O que o jogador já percebeu, quanta percepção acumulou e o que pesquisou. */
public class Knowledge implements INBTSerializable<CompoundTag> {
    public final Set<String> scanned = new HashSet<>();
    public final EnumMap<Essence, Integer> insight = new EnumMap<>(Essence.class);
    public final Set<String> research = new HashSet<>();
    /** Pesquisas cujos portões (conquista/era) estão abertos. Calculado no servidor, só viaja pela rede. */
    public final Set<String> gates = new HashSet<>();
    /** Pistas de descoberta cumpridas (máquinas usadas, itens obtidos, criaturas derrotadas...). */
    public final Set<String> discoveries = new HashSet<>();
    /** Pesquisas já anunciadas como disponíveis (para não repetir a mensagem). */
    public final Set<String> announced = new HashSet<>();
    /** Mente corrompida: marca permanente (pesquisas proibidas) e temporária (rituais, miasma, ritos). */
    public int warp;
    public int tempWarp;

    public int get(Essence e) {
        return insight.getOrDefault(e, 0);
    }

    public void add(Essence e, int n) {
        insight.put(e, Math.max(0, get(e) + n));
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        ListTag sc = new ListTag();
        scanned.forEach(s -> sc.add(StringTag.valueOf(s)));
        tag.put("scanned", sc);
        ListTag rs = new ListTag();
        research.forEach(s -> rs.add(StringTag.valueOf(s)));
        tag.put("research", rs);
        CompoundTag ins = new CompoundTag();
        insight.forEach((e, n) -> ins.putInt(e.id, n));
        tag.put("insight", ins);
        ListTag gt = new ListTag();
        gates.forEach(s -> gt.add(StringTag.valueOf(s)));
        tag.put("gates", gt);
        ListTag ds = new ListTag();
        discoveries.forEach(x -> ds.add(StringTag.valueOf(x)));
        tag.put("discoveries", ds);
        ListTag an = new ListTag();
        announced.forEach(x -> an.add(StringTag.valueOf(x)));
        tag.put("announced", an);
        tag.putInt("warp", warp);
        tag.putInt("temp_warp", tempWarp);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        scanned.clear();
        research.clear();
        insight.clear();
        gates.clear();
        discoveries.clear();
        announced.clear();
        warp = tag.getInt("warp");
        tempWarp = tag.getInt("temp_warp");
        ListTag ds = tag.getList("discoveries", Tag.TAG_STRING);
        for (int i = 0; i < ds.size(); i++) discoveries.add(ds.getString(i));
        ListTag an = tag.getList("announced", Tag.TAG_STRING);
        for (int i = 0; i < an.size(); i++) announced.add(an.getString(i));
        ListTag gt = tag.getList("gates", Tag.TAG_STRING);
        for (int i = 0; i < gt.size(); i++) gates.add(gt.getString(i));
        ListTag sc = tag.getList("scanned", Tag.TAG_STRING);
        for (int i = 0; i < sc.size(); i++) scanned.add(sc.getString(i));
        ListTag rs = tag.getList("research", Tag.TAG_STRING);
        for (int i = 0; i < rs.size(); i++) research.add(rs.getString(i));
        CompoundTag ins = tag.getCompound("insight");
        for (Essence e : Essence.values()) if (ins.contains(e.id)) insight.put(e, ins.getInt(e.id));
    }
}
