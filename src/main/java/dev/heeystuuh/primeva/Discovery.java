package dev.heeystuuh.primeva;

import dev.heeystuuh.primeva.network.Network;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashSet;
import java.util.Set;

/**
 * Descobertas: pistas que revelam pesquisas. Vêm de estudar com a Lente, obter itens,
 * derrotar criaturas e usar as máquinas do Primeva.
 */
public final class Discovery {

    /** Concede uma pista a um jogador. */
    public static void grant(ServerPlayer p, String clue) {
        Knowledge k = p.getData(ModAttachments.KNOWLEDGE);
        if (!k.discoveries.add(clue)) return;
        if (isUsefulClue(clue)) {
            p.displayClientMessage(Component.translatable("msg.primeva.discovery", Research.clueText(clue)).withStyle(ChatFormatting.AQUA), true);
            p.level().playSound(null, p.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.8f, 1.4f);
        }
        announceRevealed(p, k);
        Network.sync(p);
    }

    /** Concede a pista a todos os jogadores perto de uma máquina. */
    public static void near(Level level, BlockPos pos, String clue) {
        if (!(level instanceof ServerLevel sl)) return;
        for (Player p : sl.getEntitiesOfClass(Player.class, new AABB(pos).inflate(8))) {
            if (p instanceof ServerPlayer sp) grant(sp, clue);
        }
    }

    private static boolean isUsefulClue(String clue) {
        for (Research r : Research.ALL) if (r.clues().contains(clue)) return true;
        return false;
    }

    /** Avisa sobre pesquisas que acabaram de ficar disponíveis. */
    public static void announceRevealed(ServerPlayer p, Knowledge k) {
        Research.refreshGates(p);
        for (Research r : Research.ALL) {
            if (r.done(k) || k.announced.contains(r.id()) || !r.unlocked(k)) continue;
            k.announced.add(r.id());
            p.sendSystemMessage(Component.translatable("msg.primeva.research_revealed", r.title().withStyle(ChatFormatting.GOLD))
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
            p.level().playSound(null, p.blockPosition(), SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 1f, 0.8f);
        }
    }

    // ---------------- ganchos ----------------

    @SubscribeEvent
    public static void onKill(LivingDeathEvent e) {
        if (e.getSource().getEntity() instanceof ServerPlayer sp) {
            ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(e.getEntity().getType());
            grant(sp, "kill:" + id);
        }
    }

    /** A cada 5 s: pistas "has:" (ter um item no inventário) e "do:miasma". */
    @SubscribeEvent
    public static void onTick(PlayerTickEvent.Post e) {
        if (!(e.getEntity() instanceof ServerPlayer sp) || sp.tickCount % 100 != 37) return;
        Knowledge k = sp.getData(ModAttachments.KNOWLEDGE);
        Set<String> pending = new HashSet<>();
        for (Research r : Research.ALL) {
            if (r.done(k)) continue;
            for (String c : r.clues()) if (c.startsWith("has:") && !k.discoveries.contains(c)) pending.add(c);
        }
        for (String c : pending) {
            String what = c.substring(4);
            boolean found = false;
            if (what.startsWith("#")) {
                TagKey<Item> tag = TagKey.create(Registries.ITEM, ResourceLocation.parse(what.substring(1)));
                for (ItemStack s : sp.getInventory().items) if (s.is(tag)) { found = true; break; }
            } else {
                Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(what));
                found = sp.getInventory().contains(new ItemStack(item));
            }
            if (found) grant(sp, c);
        }
        if (Miasma.get(sp.level(), sp.blockPosition()) >= 40) grant(sp, "do:miasma");
    }

    private Discovery() {}
}
