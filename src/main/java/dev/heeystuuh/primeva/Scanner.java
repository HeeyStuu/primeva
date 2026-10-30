package dev.heeystuuh.primeva;

import dev.heeystuuh.primeva.network.Network;
import dev.heeystuuh.primeva.network.ScanResultPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.EnumMap;
import java.util.Map;

/** Lógica da Lente da Percepção (chamada quando o foco completa). */
public final class Scanner {

    public static void scan(ServerPlayer player, String key, Component name, Map<Essence, Integer> essences) {
        if (essences.isEmpty()) {
            player.displayClientMessage(Component.translatable("msg.primeva.scan_nothing", name).withStyle(ChatFormatting.GRAY), true);
            return;
        }
        Knowledge k = player.getData(ModAttachments.KNOWLEDGE);
        boolean fresh = k.scanned.add(key);
        if (fresh) {
            k.discoveries.add("do:scan");
            Discovery.announceRevealed(player, k);
        }
        Map<Essence, Integer> shown = essences;
        if (fresh) {
            double mult = 1.0;
            if (player.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.ARCHMAGE_CROWN.get())) mult += 1.0;
            if (CommonEvents.isAscended(player)) mult += 0.5;
            Map<Essence, Integer> gained = new EnumMap<>(Essence.class);
            for (Map.Entry<Essence, Integer> e : essences.entrySet()) gained.put(e.getKey(), (int) Math.ceil(e.getValue() * mult));
            gained.forEach(k::add);
            shown = gained;
            player.level().playSound(null, player.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.4f, 1.8f);
            Network.sync(player);
        }
        CompoundTag tag = new CompoundTag();
        shown.forEach((e, n) -> tag.putInt(e.id, n));
        PacketDistributor.sendToPlayer(player, new ScanResultPayload(name, tag, fresh));
    }

    private Scanner() {}
}
