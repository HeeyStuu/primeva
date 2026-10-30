package dev.heeystuuh.primeva;

import dev.heeystuuh.primeva.network.Network;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;

import java.util.*;
import java.util.function.Supplier;

/**
 * Pesquisas do Grimório, em 6 categorias.
 * Para revelar uma pesquisa: concluir os pré-requisitos e cumprir as pistas de descoberta
 * (estudar algo com a Lente, obter um item, derrotar uma criatura ou usar uma máquina).
 * Algumas também exigem uma conquista ou uma Era do Nexus das Eras.
 */
public record Research(String id, int chapter, Map<Essence, Integer> cost, List<String> prereqs, List<String> clues, String advancement, int era) {

    public static final int CHAPTERS = 6;
    public static final int ESSENCE = 0, ALCHEMY = 1, INFUSION = 2, CREATURES = 3, ARTIFACTS = 4, GOLEMS = 5;

    public static final List<Research> ALL = List.of(
            // ---------------- Essência ----------------
            r("miasma", ESSENCE, cost(Essence.UMBRA, 12, Essence.LUMEN, 12), List.of("infusion"), List.of("do:distill")),
            r("salt", ESSENCE, cost(Essence.LUMEN, 10, Essence.TERRA, 10, Essence.GALE, 6), List.of("infusion"), List.of("has:#primeva:shards")),
            r("purification", ESSENCE, cost(Essence.LUMEN, 24, Essence.TIDE, 16), List.of("miasma"), List.of("do:miasma")),
            r("eternal_flame", ESSENCE, cost(Essence.EMBER, 16, Essence.LUMEN, 16), List.of("crucible"), List.of("scan:item:minecraft:glowstone_dust")),
            r("transmutation", ESSENCE, cost(Essence.TERRA, 30, Essence.LUMEN, 20, Essence.EMBER, 20), List.of("primeval_metal", "miasma"), List.of("scan:item:minecraft:diamond")),
            gated("study", ESSENCE, cost(Essence.LUMEN, 30, Essence.GALE, 20, Essence.TIDE, 20), List.of("transmutation"), List.of(), null, 4),
            r("sanity", ESSENCE, cost(Essence.LUMEN, 20, Essence.TIDE, 15), List.of("purification"), List.of("do:warp")),
            r("warding", ESSENCE, cost(Essence.LUMEN, 30, Essence.TERRA, 20, Essence.UMBRA, 10), List.of("purification"), List.of("kill:minecraft:zombie")),
            r("astronomy", ESSENCE, cost(Essence.LUMEN, 14, Essence.UMBRA, 14, Essence.GALE, 10), List.of("salt"), List.of("has:minecraft:spyglass")),
            gated("crystals", ESSENCE, cost(Essence.EMBER, 40, Essence.TIDE, 40, Essence.TERRA, 40, Essence.GALE, 40, Essence.LUMEN, 40, Essence.UMBRA, 40), List.of("containment"), List.of(), null, 4),
            // ---------------- Alquimia ----------------
            r("primeval_metal", ALCHEMY, cost(Essence.TERRA, 16, Essence.EMBER, 12), List.of("infusion"), List.of("scan:item:minecraft:iron_ingot")),
            r("crucible", ALCHEMY, cost(Essence.TIDE, 14, Essence.EMBER, 12), List.of("salt"), List.of("do:distill")),
            r("essence_logistics", ALCHEMY, cost(Essence.TIDE, 16, Essence.GALE, 12), List.of("crucible"), List.of("has:primeva:quicksilver")),
            r("arcanite", ALCHEMY, cost(Essence.TERRA, 18, Essence.UMBRA, 8, Essence.LUMEN, 10), List.of("crucible"), List.of("do:crucible")),
            r("essence_arrows", ALCHEMY, cost(Essence.GALE, 12, Essence.EMBER, 10), List.of("crucible"), List.of("has:minecraft:arrow")),
            gated("elixirs", ALCHEMY, cost(Essence.TIDE, 50, Essence.LUMEN, 40, Essence.TERRA, 30), List.of("crystals"), List.of(), null, 4),
            gated("voidsteel", ALCHEMY, cost(Essence.UMBRA, 50, Essence.GALE, 30, Essence.TERRA, 30), List.of("containment", "arcanite"), List.of("scan:item:minecraft:ender_pearl"), null, 4),
            // ---------------- Infusão ----------------
            r("infusion", INFUSION, cost(Essence.LUMEN, 8, Essence.TERRA, 8), List.of(), List.of("do:scan")),
            r("elemental_wands", INFUSION, cost(Essence.EMBER, 20, Essence.TIDE, 20, Essence.GALE, 20), List.of("primeval_metal"), List.of("scan:item:minecraft:blaze_rod")),
            r("foci", INFUSION, cost(Essence.GALE, 20, Essence.TIDE, 20, Essence.TERRA, 20), List.of("crucible", "elemental_wands"), List.of("has:primeva:amber")),
            r("advanced_foci", INFUSION, cost(Essence.UMBRA, 30, Essence.TERRA, 25, Essence.LUMEN, 25), List.of("foci"), List.of("do:wand")),
            gated("containment", INFUSION, cost(Essence.TERRA, 45, Essence.UMBRA, 30, Essence.LUMEN, 30), List.of("study"), List.of("do:infusion"), "minecraft:nether/obtain_crying_obsidian", 4),
            gated("greater_wands", INFUSION, cost(Essence.TERRA, 50, Essence.LUMEN, 50, Essence.UMBRA, 50), List.of("crystals", "elemental_wands"), List.of(), null, 4),
            gated("catalyst", INFUSION, cost(Essence.EMBER, 90, Essence.TIDE, 90, Essence.TERRA, 90, Essence.GALE, 90, Essence.LUMEN, 90, Essence.UMBRA, 90), List.of("primeval_armor"), List.of(), "minecraft:nether/summon_wither", 5),
            gated("primeval_heart", INFUSION, cost(Essence.EMBER, 200, Essence.TIDE, 200, Essence.TERRA, 200, Essence.GALE, 200, Essence.LUMEN, 200, Essence.UMBRA, 200), List.of("eclipse_reaper", "archmage_crown"), List.of(), "minecraft:end/kill_dragon", 6),
            // ---------------- Criaturas ----------------
            r("wraiths", CREATURES, cost(Essence.UMBRA, 18, Essence.GALE, 12), List.of("miasma"), List.of("kill:primeva:miasma_wraith")),
            r("crimson_cult", CREATURES, cost(Essence.UMBRA, 30, Essence.EMBER, 20), List.of("miasma"), List.of("kill:primeva:crimson_cultist")),
            r("herald", CREATURES, cost(Essence.UMBRA, 60, Essence.EMBER, 40, Essence.LUMEN, 20), List.of("crimson_cult"), List.of("has:primeva:crimson_rites")),
            // ---------------- Artefatos ----------------
            r("gale_boots", ARTIFACTS, cost(Essence.GALE, 24, Essence.TIDE, 10), List.of("primeval_metal"), List.of("scan:item:minecraft:feather")),
            r("umbral_blade", ARTIFACTS, cost(Essence.UMBRA, 24, Essence.EMBER, 14), List.of("primeval_metal"), List.of("scan:entity:minecraft:zombie")),
            r("goggles", ARTIFACTS, cost(Essence.LUMEN, 20, Essence.GALE, 14), List.of("arcanite"), List.of()),
            r("runic_ring", ARTIFACTS, cost(Essence.TERRA, 24, Essence.LUMEN, 20, Essence.TIDE, 16), List.of("arcanite"), List.of()),
            r("traveler_boots", ARTIFACTS, cost(Essence.GALE, 20, Essence.TERRA, 12), List.of("arcanite"), List.of()),
            r("arcane_devices", ARTIFACTS, cost(Essence.UMBRA, 14, Essence.LUMEN, 18, Essence.TERRA, 14), List.of("eternal_flame", "arcanite"), List.of()),
            r("elemental_tools", ARTIFACTS, cost(Essence.EMBER, 25, Essence.TIDE, 25, Essence.TERRA, 25, Essence.GALE, 25), List.of("arcanite", "foci"), List.of("do:wand")),
            r("trinkets", ARTIFACTS, cost(Essence.LUMEN, 25, Essence.TERRA, 20, Essence.TIDE, 20), List.of("runic_ring"), List.of()),
            r("grapple", ARTIFACTS, cost(Essence.GALE, 25, Essence.TERRA, 15), List.of("traveler_boots"), List.of()),
            r("pocket_mirror", ARTIFACTS, cost(Essence.UMBRA, 20, Essence.GALE, 20, Essence.LUMEN, 15), List.of("trinkets"), List.of("scan:entity:minecraft:enderman")),
            r("elder_bow", ARTIFACTS, cost(Essence.GALE, 20, Essence.TERRA, 14, Essence.UMBRA, 8), List.of("arcanite", "essence_arrows"), List.of("scan:entity:minecraft:skeleton")),
            r("cloud_ring", ARTIFACTS, cost(Essence.GALE, 30, Essence.LUMEN, 15), List.of("trinkets"), List.of("scan:entity:minecraft:bat")),
            r("arcane_lamps", ARTIFACTS, cost(Essence.TERRA, 25, Essence.LUMEN, 20, Essence.TIDE, 15), List.of("arcane_devices"), List.of("has:minecraft:bone_meal")),
            r("levitation", ARTIFACTS, cost(Essence.GALE, 28, Essence.TIDE, 12), List.of("gale_boots"), List.of("has:minecraft:phantom_membrane")),
            r("voidsteel_armor", ARTIFACTS, cost(Essence.UMBRA, 60, Essence.TERRA, 40, Essence.GALE, 20), List.of("voidsteel"), List.of()),
            gated("primeval_armor", ARTIFACTS, cost(Essence.TERRA, 70, Essence.EMBER, 50, Essence.LUMEN, 50), List.of("crystals", "gale_boots"), List.of(), null, 4),
            gated("primeval_tools", ARTIFACTS, cost(Essence.TERRA, 80, Essence.EMBER, 60), List.of("primeval_armor"), List.of(), null, 4),
            gated("eclipse_reaper", ARTIFACTS, cost(Essence.UMBRA, 180, Essence.EMBER, 120, Essence.TERRA, 80), List.of("catalyst", "umbral_blade"), List.of(), null, 5),
            gated("archmage_crown", ARTIFACTS, cost(Essence.GALE, 180, Essence.LUMEN, 160, Essence.TIDE, 80), List.of("catalyst"), List.of(), "minecraft:end/kill_dragon", 5),
            // ---------------- Golens ----------------
            r("golemancy", GOLEMS, cost(Essence.TERRA, 30, Essence.LUMEN, 20, Essence.GALE, 15), List.of("arcane_devices"), List.of("has:minecraft:clay_ball")),
            r("advanced_seals", GOLEMS, cost(Essence.UMBRA, 25, Essence.EMBER, 25), List.of("golemancy"), List.of("do:golem")),
            r("golem_cores", GOLEMS, cost(Essence.EMBER, 20, Essence.TIDE, 20, Essence.TERRA, 20, Essence.GALE, 20), List.of("advanced_seals"), List.of("do:golem_work"))
    );

    /** Recompensas entregues ao concluir a pesquisa. */
    public static final Map<String, Supplier<ItemStack>> REWARDS = Map.of(
            "infusion", () -> new ItemStack(ModItems.ARCANE_DUST.get(), 4),
            "salt", () -> new ItemStack(ModItems.PRIMORDIAL_SALT.get(), 1),
            "essence_logistics", () -> new ItemStack(ModItems.ESSENCE_JAR.get(), 1),
            "purification", () -> new ItemStack(ModItems.PURIFYING_INCENSE.get(), 1),
            "golemancy", () -> new ItemStack(ModItems.SEAL_PICKUP.get(), 1),
            "golem_cores", () -> new ItemStack(ModItems.GOLEM_CORE_BLANK.get(), 1),
            "elemental_wands", () -> new ItemStack(ModItems.phial(Essence.EMBER), 2),
            "wraiths", () -> new ItemStack(ModItems.MIASMIC_RESIDUE.get(), 2)
    );

    private static Research r(String id, int ch, Map<Essence, Integer> cost, List<String> pre, List<String> clues) {
        return new Research(id, ch, cost, pre, clues, null, 0);
    }

    private static Research gated(String id, int ch, Map<Essence, Integer> cost, List<String> pre, List<String> clues, String adv, int era) {
        return new Research(id, ch, cost, pre, clues, adv, era);
    }

    private static Map<Essence, Integer> cost(Object... kv) {
        Map<Essence, Integer> m = new EnumMap<>(Essence.class);
        for (int i = 0; i < kv.length; i += 2) m.put((Essence) kv[i], (Integer) kv[i + 1]);
        return m;
    }

    public static Research get(String id) {
        for (Research r : ALL) if (r.id.equals(id)) return r;
        return null;
    }

    public MutableComponent title() {
        return Component.translatable("research.primeva." + id);
    }

    public boolean done(Knowledge k) {
        return k.research.contains(id);
    }

    public boolean prereqsDone(Knowledge k) {
        return k.research.containsAll(prereqs);
    }

    /** Uma pista foi cumprida? */
    public static boolean clueDone(Knowledge k, String clue) {
        if (clue.startsWith("scan:")) return k.scanned.contains(clue.substring(5)) || k.discoveries.contains(clue);
        return k.discoveries.contains(clue);
    }

    public boolean cluesDone(Knowledge k) {
        for (String c : clues) if (!clueDone(k, c)) return false;
        return true;
    }

    /** Pré-requisitos + pistas + portões (conquista/era). No cliente os portões vêm sincronizados do servidor. */
    public boolean unlocked(Knowledge k) {
        return prereqsDone(k) && cluesDone(k) && k.gates.contains(id);
    }

    /** Escondida ("???") enquanto os pré-requisitos não forem concluídos. */
    public boolean hidden(Knowledge k) {
        return !done(k) && !prereqsDone(k);
    }

    /** Custo real (com o multiplicador da configuração). */
    public int need(Essence e) {
        Integer base = cost.get(e);
        if (base == null) return 0;
        return Math.max(1, (int) Math.ceil(base * Config.get(Config.RESEARCH_COST)));
    }

    public boolean affordable(Knowledge k) {
        for (Essence e : cost.keySet()) if (k.get(e) < need(e)) return false;
        return true;
    }

    // ---------------- portões ----------------

    public boolean advancementOk(ServerPlayer player) {
        if (advancement == null) return true;
        AdvancementHolder holder = player.server.getAdvancements().get(ResourceLocation.parse(advancement));
        return holder == null || player.getAdvancements().getOrStartProgress(holder).isDone();
    }

    /** Era do Nexus das Eras (se o mod não estiver instalado, não exige). */
    public static int nexusEra(Player player) {
        CompoundTag root = player.getPersistentData();
        if (!root.contains(Player.PERSISTED_NBT_TAG)) return 0;
        int mask = root.getCompound(Player.PERSISTED_NBT_TAG).getInt("nexus_era");
        for (int i = 7; i >= 1; i--) if ((mask & (1 << i)) != 0) return i;
        return 0;
    }

    public boolean eraOk(ServerPlayer player) {
        return era <= 0 || !ModList.get().isLoaded("nexus") || nexusEra(player) >= era;
    }

    public boolean gatesOk(ServerPlayer player) {
        return advancementOk(player) && eraOk(player);
    }

    public static void refreshGates(ServerPlayer player) {
        Knowledge k = player.getData(ModAttachments.KNOWLEDGE);
        k.gates.clear();
        for (Research r : ALL) if (r.gatesOk(player)) k.gates.add(r.id);
    }

    public static int countIn(int chapter) {
        int n = 0;
        for (Research r : ALL) if (r.chapter == chapter) n++;
        return n;
    }

    public static int doneIn(Knowledge k, int chapter) {
        int n = 0;
        for (Research r : ALL) if (r.chapter == chapter && r.done(k)) n++;
        return n;
    }

    /** Todas as categorias ficam abertas; o que trava é cada pesquisa. */
    public static boolean chapterOpen(Knowledge k, int chapter) {
        return true;
    }

    /** Tabuleiro do quebra-cabeça desta pesquisa para este jogador. */
    public ResearchPuzzle.Board board(java.util.UUID player) {
        return ResearchPuzzle.generate(id, player, cost.keySet());
    }

    /** Custo total: pesquisa + essências colocadas no tabuleiro. */
    public Map<Essence, Integer> totalCost(Map<Integer, Essence> placed) {
        Map<Essence, Integer> out = new EnumMap<>(Essence.class);
        for (Essence e : cost.keySet()) out.put(e, need(e));
        ResearchPuzzle.placementCost(placed).forEach((e, n) -> out.merge(e, n, Integer::sum));
        return out;
    }

    public static boolean hasScroll(Player p) {
        return p.getAbilities().instabuild || p.getInventory().contains(new ItemStack(ModItems.RESEARCH_SCROLL.get()));
    }

    /** Chamado pelo servidor quando o jogador termina o quebra-cabeça da pesquisa. */
    public static void tryResearch(ServerPlayer player, String id, List<Integer> encoded) {
        Research r = get(id);
        if (r == null) return;
        refreshGates(player);
        Knowledge k = player.getData(ModAttachments.KNOWLEDGE);
        if (r.done(k) || !r.unlocked(k)) return;
        Map<Integer, Essence> placed = ResearchPuzzle.decode(encoded);
        ResearchPuzzle.Board board = r.board(player.getUUID());
        for (Integer cell : placed.keySet()) if (board.anchors().containsKey(cell) || board.voids().contains(cell)) return;
        if (!ResearchPuzzle.solved(board, placed)) {
            player.displayClientMessage(Component.translatable("msg.primeva.puzzle_unsolved").withStyle(ChatFormatting.RED), true);
            return;
        }
        Map<Essence, Integer> total = r.totalCost(placed);
        for (Map.Entry<Essence, Integer> e : total.entrySet()) if (k.get(e.getKey()) < e.getValue()) return;
        if (!hasScroll(player)) {
            player.displayClientMessage(Component.translatable("msg.primeva.need_scroll").withStyle(ChatFormatting.RED), true);
            return;
        }
        if (!player.getAbilities().instabuild) {
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                if (player.getInventory().getItem(i).is(ModItems.RESEARCH_SCROLL.get())) {
                    player.getInventory().getItem(i).shrink(1);
                    break;
                }
            }
        }
        total.forEach((e, n) -> k.add(e, -n));
        k.research.add(r.id);
        Warp.addPerm(player, Warp.RESEARCH_WARP.getOrDefault(r.id, 0));
        player.sendSystemMessage(Component.translatable("msg.primeva.researched", r.title().withStyle(ChatFormatting.GOLD))
                .withStyle(ChatFormatting.LIGHT_PURPLE));
        player.level().playSound(null, player.blockPosition(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1f, 1f);
        if (r.era >= 5) player.level().playSound(null, player.blockPosition(), SoundEvents.WITHER_SPAWN, SoundSource.PLAYERS, 0.4f, 1.6f);
        Supplier<ItemStack> reward = REWARDS.get(r.id);
        if (reward != null) {
            ItemStack s = reward.get();
            player.sendSystemMessage(Component.translatable("msg.primeva.research_reward", s.getHoverName().copy().withStyle(ChatFormatting.AQUA), s.getCount())
                    .withStyle(ChatFormatting.GRAY));
            if (!player.getInventory().add(s)) player.drop(s, false);
        }
        Discovery.announceRevealed(player, k);
        Network.sync(player);
    }

    /** Texto da pista para mostrar no Grimório. */
    public static Component clueText(String clue) {
        String[] p = clue.split(":", 2);
        String rest = p.length > 1 ? p[1] : "";
        return switch (p[0]) {
            case "scan" -> {
                String[] q = rest.split(":", 2);
                ResourceLocation rl = ResourceLocation.tryParse(q.length > 1 ? q[1] : rest);
                Component name = rl == null ? Component.literal(rest)
                        : q[0].equals("entity") ? BuiltInRegistries.ENTITY_TYPE.get(rl).getDescription() : BuiltInRegistries.ITEM.get(rl).getDescription();
                yield Component.translatable("clue.primeva.scan", name);
            }
            case "has" -> {
                if (rest.startsWith("#")) yield Component.translatable("clue.primeva.has", Component.translatable("tag.item." + rest.substring(1).replace(':', '.')));
                ResourceLocation rl = ResourceLocation.tryParse(rest);
                yield Component.translatable("clue.primeva.has", rl == null ? Component.literal(rest) : BuiltInRegistries.ITEM.get(rl).getDescription());
            }
            case "kill" -> {
                ResourceLocation rl = ResourceLocation.tryParse(rest);
                yield Component.translatable("clue.primeva.kill", rl == null ? Component.literal(rest) : BuiltInRegistries.ENTITY_TYPE.get(rl).getDescription());
            }
            default -> Component.translatable("clue.primeva.do." + rest);
        };
    }
}
