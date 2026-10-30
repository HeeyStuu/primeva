package dev.heeystuuh.primeva.client;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.resources.IoSupplier;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Pacote de recursos gerado no próprio cliente: pega as texturas do Minecraft instalado
 * (ferramentas, lingotes, poções, minérios, troncos, armaduras...) e só troca a cor.
 * Assim os itens do Primeva ficam com o formato idêntico ao vanilla, sem copiar a arte para dentro do mod.
 * Se alguma textura base não existir, o mod usa a própria textura de reserva.
 */
public final class VanillaTintPack implements PackResources {
    public static final String ID = "primeva_vanilla_colors";

    enum Mode { NONE, GRAY, HUE, HUE_KEEP_BROWN }

    record Layer(String base, Mode mode, int color, float sat) {}

    private static final Map<String, List<Layer>> RECIPES = new LinkedHashMap<>();
    private static final Map<String, byte[]> CACHE = new ConcurrentHashMap<>();
    private static final byte[] MISSING = new byte[0];

    // ---------------- cores ----------------
    private static final int ARC = 0x9A7AE8, VOID = 0x7A5AB0, PRI = 0x3AD8C0, CRIM = 0xC81E32, TRAV = 0x7A9A50;
    private static final Map<String, Integer> ESS = Map.of("ember", 0xFF7A2A, "tide", 0x4AA8FF, "terra", 0x7AD050,
            "gale", 0xD0FFF6, "lumen", 0xFFE070, "umbra", 0xB070FF);

    private static Layer L(String base, Mode m, int color) {
        return new Layer(base, m, color, 1f);
    }

    private static Layer L(String base, Mode m, int color, float sat) {
        return new Layer(base, m, color, sat);
    }

    private static void put(String path, Layer... layers) {
        RECIPES.put("textures/" + path + ".png", List.of(layers));
    }

    private static void item(String name, String base, Mode m, int color) {
        put("item/" + name, L(base, m, color));
    }

    private static void block(String name, String base, Mode m, int color) {
        put("block/" + name, L(base, m, color));
    }

    static {
        String[] tools = {"sword", "pickaxe", "axe", "shovel", "hoe"};
        for (String t : tools) {
            item("arcanite_" + t, "item/iron_" + t, Mode.GRAY, ARC);
            if (!t.equals("hoe")) item("voidsteel_" + t, "item/netherite_" + t, Mode.GRAY, VOID);
        }
        item("ember_pickaxe", "item/iron_pickaxe", Mode.GRAY, ESS.get("ember"));
        item("tide_axe", "item/iron_axe", Mode.GRAY, ESS.get("tide"));
        item("terra_shovel", "item/iron_shovel", Mode.GRAY, ESS.get("terra"));
        item("gale_sword", "item/iron_sword", Mode.GRAY, ESS.get("gale"));
        item("lumen_hoe", "item/iron_hoe", Mode.GRAY, ESS.get("lumen"));
        item("primeval_pickaxe", "item/iron_pickaxe", Mode.GRAY, PRI);
        item("umbral_blade", "item/iron_sword", Mode.GRAY, 0x8A50D0);
        item("eclipse_reaper", "item/netherite_hoe", Mode.GRAY, 0xB070FF);
        item("crimson_blade", "item/iron_sword", Mode.GRAY, CRIM);

        item("primeval_ingot", "item/iron_ingot", Mode.GRAY, PRI);
        item("arcanite_ingot", "item/iron_ingot", Mode.GRAY, ARC);
        item("arcanite_nugget", "item/iron_nugget", Mode.GRAY, ARC);
        item("voidsteel_ingot", "item/netherite_ingot", Mode.GRAY, VOID);

        String[] armor = {"helmet", "chestplate", "leggings", "boots"};
        for (String a : armor) {
            item("primeval_" + a, "item/iron_" + a, Mode.GRAY, PRI);
            item("arcanite_" + a, "item/iron_" + a, Mode.GRAY, ARC);
            put("item/crimson_" + a, L("item/leather_" + a, Mode.GRAY, CRIM), L("item/leather_" + a + "_overlay", Mode.NONE, 0));
        }
        put("item/traveler_boots", L("item/leather_boots", Mode.GRAY, TRAV), L("item/leather_boots_overlay", Mode.NONE, 0));
        item("gale_boots", "item/iron_boots", Mode.GRAY, 0xC8F0FF);
        item("archmage_crown", "item/golden_helmet", Mode.NONE, 0);

        for (Map.Entry<String, Integer> e : ESS.entrySet()) {
            String id = e.getKey();
            int c = e.getValue();
            put("item/phial_" + id, L("item/potion_overlay", Mode.GRAY, c), L("item/potion", Mode.NONE, 0));
            put("item/crystal_" + id, L("block/amethyst_cluster", Mode.HUE, c, id.equals("gale") ? 0.35f : 1f));
            put("item/shard_" + id, L("item/amethyst_shard", Mode.HUE, c, id.equals("gale") ? 0.35f : 1f));
            put("block/crystal_cluster_" + id, L("block/amethyst_cluster", Mode.HUE, c, id.equals("gale") ? 0.35f : 1f));
            put("item/wand_" + id, L("item/blaze_rod", Mode.HUE, c, id.equals("gale") ? 0.35f : 1f));
        }
        put("item/elixir_vigor", L("item/potion_overlay", Mode.GRAY, 0xFF4A6A), L("item/potion", Mode.NONE, 0));
        put("item/elixir_abyss", L("item/potion_overlay", Mode.GRAY, 0x2A70E0), L("item/splash_potion", Mode.NONE, 0));
        put("item/elixir_primeval", L("item/potion_overlay", Mode.GRAY, 0xE05AE0), L("item/lingering_potion", Mode.NONE, 0));
        item("empty_phial", "item/glass_bottle", Mode.NONE, 0);
        put("item/lucidity_tonic", L("item/potion_overlay", Mode.GRAY, 0x7AE8E8), L("item/potion", Mode.NONE, 0));

        item("quicksilver", "item/ghast_tear", Mode.GRAY, 0xD8E4F8);
        item("amber", "item/emerald", Mode.HUE, 0xFFA020);
        item("arcane_dust", "item/glowstone_dust", Mode.HUE, 0xA060FF);
        item("primordial_salt", "item/sugar", Mode.GRAY, 0xFFF0C0);
        item("void_seed", "item/ender_pearl", Mode.HUE, 0x8040D0);
        item("eternal_flame", "item/blaze_powder", Mode.HUE, 0xFFD040);
        item("arcane_fuel", "item/coal", Mode.GRAY, 0xFF9040);
        item("grimoire", "item/book", Mode.HUE, 0x7A3AC0);
        item("crimson_rites", "item/book", Mode.HUE, CRIM);
        item("crimson_cloth", "item/leather", Mode.HUE, CRIM);
        item("lens", "item/spyglass", Mode.HUE, 0xF0C040);
        item("primeval_heart", "item/heart_of_the_sea", Mode.HUE, 0xFF3A6A);
        item("focus_frost", "item/ender_eye", Mode.HUE, 0x7AD8FF);
        item("focus_shock", "item/ender_eye", Mode.HUE, 0xFFF060);
        item("focus_excavation", "item/ender_eye", Mode.HUE, 0x7AD050);
        item("focus_exchange", "item/ender_eye", Mode.HUE, 0xFFB040);
        item("focus_hole", "item/ender_eye", Mode.HUE, 0x6A2AB0);
        item("loot_bag_common", "item/bundle", Mode.NONE, 0);
        item("loot_bag_uncommon", "item/bundle", Mode.HUE, 0x3A80E0);
        item("loot_bag_rare", "item/bundle", Mode.HUE, 0xF0C030);

        // blocos
        block("vermilion_ore", "block/iron_ore", Mode.HUE, 0xE02020);
        block("deepslate_vermilion_ore", "block/deepslate_iron_ore", Mode.HUE, 0xE02020);
        block("amber_ore", "block/gold_ore", Mode.HUE, 0xFF8A10);
        block("argent_log", "block/birch_log", Mode.GRAY, 0xD8E8F8);
        put("block/argent_log_top", L("block/birch_log_top", Mode.HUE, 0xB8D0E8, 0.45f));
        put("block/argent_planks", L("block/birch_planks", Mode.HUE, 0xB8C8D8, 0.4f));
        block("argent_leaves", "block/birch_leaves", Mode.GRAY, 0x9ADCE0);
        block("argent_sapling", "block/birch_sapling", Mode.HUE, 0x8AD8E0);
        block("elder_log", "block/dark_oak_log", Mode.HUE, 0x8A4A2A);
        block("elder_log_top", "block/dark_oak_log_top", Mode.HUE, 0x8A4A2A);
        block("elder_planks", "block/dark_oak_planks", Mode.HUE, 0x9A4A28);
        block("elder_leaves", "block/dark_oak_leaves", Mode.GRAY, 0x2E7A28);
        block("elder_sapling", "block/dark_oak_sapling", Mode.HUE, 0x3A9A30);
        block("arcanite_block", "block/iron_block", Mode.GRAY, ARC);
        block("crucible_side", "block/cauldron_side", Mode.GRAY, 0x8A7AB0);
        block("crucible_top", "block/cauldron_top", Mode.GRAY, 0x8A7AB0);
        block("crucible_inner", "block/cauldron_inner", Mode.GRAY, 0x8A7AB0);
        block("crucible_bottom", "block/cauldron_bottom", Mode.GRAY, 0x8A7AB0);
        block("arcane_lamp", "block/redstone_lamp_on", Mode.HUE, 0xB070FF);
        block("corrupted_soil_side", "block/mycelium_side", Mode.HUE_KEEP_BROWN, 0xB040D0);
        block("corrupted_soil_top", "block/mycelium_top", Mode.HUE_KEEP_BROWN, 0xB040D0);
        block("pedestal_side", "block/stone_bricks", Mode.GRAY, 0xD8D0E8);
        block("pedestal_top", "block/chiseled_stone_bricks", Mode.GRAY, 0xD8D0E8);
        block("altar_side", "block/enchanting_table_side", Mode.HUE, 0x9040E0);
        block("altar_top", "block/enchanting_table_top", Mode.HUE, 0x9040E0);
        block("catalyst_side", "block/crying_obsidian", Mode.HUE, 0xFF40D0);
        block("catalyst_top", "block/crying_obsidian", Mode.HUE, 0xFF40D0);
        block("distiller_copper", "block/copper_block", Mode.NONE, 0);
        block("distiller_iron", "block/iron_block", Mode.NONE, 0);
        block("distiller_top", "block/cauldron_top", Mode.NONE, 0);
        block("pillar_cap", "block/polished_blackstone", Mode.GRAY, 0x8A7AA8);
        block("pillar_side", "block/polished_blackstone_bricks", Mode.GRAY, 0x8A7AA8);
        block("pillar_core", "block/crying_obsidian", Mode.NONE, 0);
        block("purifier_cap", "block/gold_block", Mode.NONE, 0);
        block("purifier_metal", "block/gold_block", Mode.NONE, 0);
        block("purifier_glass", "block/glowstone", Mode.NONE, 0);
        block("study_wood", "block/dark_oak_planks", Mode.NONE, 0);
        block("study_top", "block/green_wool", Mode.NONE, 0);
        block("hungry_chest_side", "block/barrel_side", Mode.NONE, 0);
        block("hungry_chest_top", "block/barrel_top", Mode.NONE, 0);

        // ---- novidades
        for (String id : new String[]{"ember", "tide", "terra", "gale", "lumen", "umbra"}) {
            int c = ESS.get(id);
            put("item/arrow_" + id, L("item/arrow", Mode.GRAY, c));
        }
        put("item/elder_bow", L("item/bow", Mode.HUE, 0x9A4A28));
        for (int i = 0; i < 3; i++) put("item/elder_bow_pulling_" + i, L("item/bow_pulling_" + i, Mode.HUE, 0x9A4A28));
        for (String a : armor) item("voidsteel_" + a, "item/netherite_" + a, Mode.GRAY, VOID);
        item("focus_flame", "item/ender_eye", Mode.HUE, 0xFF6020);
        item("focus_radiance", "item/ender_eye", Mode.HUE, 0xFFF0A0);
        put("item/cleansing_soap", L("item/slime_ball", Mode.HUE, 0xA8F0F0, 0.55f));
        put("item/decaying_brain", L("item/rotten_flesh", Mode.HUE, 0xD88AA8, 0.8f));
        block("growth_lamp", "block/redstone_lamp_on", Mode.HUE, 0x60E040);
        block("fertility_lamp", "block/redstone_lamp_on", Mode.HUE, 0xFF70B0);
        block("levitator_side", "block/cut_copper", Mode.HUE, 0x80D0FF);
        block("levitator_top", "block/copper_grate", Mode.HUE, 0x80D0FF);
        block("warding_stone_side", "block/chiseled_stone_bricks", Mode.GRAY, 0xF0E0B0);
        block("arcane_stone", "block/smooth_stone", Mode.GRAY, 0x9A88C0);
        block("arcane_bricks", "block/stone_bricks", Mode.GRAY, 0x8474AC);

        // armaduras vestidas
        for (String n : new String[]{"1", "2"}) {
            put("models/armor/primeval_layer_" + n, L("models/armor/iron_layer_" + n, Mode.GRAY, PRI));
            put("models/armor/arcanite_layer_" + n, L("models/armor/iron_layer_" + n, Mode.GRAY, ARC));
            put("models/armor/gale_layer_" + n, L("models/armor/iron_layer_" + n, Mode.GRAY, 0xC8F0FF));
            put("models/armor/archmage_layer_" + n, L("models/armor/gold_layer_" + n, Mode.NONE, 0));
            put("models/armor/crimson_layer_" + n, L("models/armor/leather_layer_" + n, Mode.GRAY, CRIM));
            put("models/armor/voidsteel_layer_" + n, L("models/armor/netherite_layer_" + n, Mode.GRAY, VOID));
        }
        put("models/armor/traveler_layer_1", L("models/armor/leather_layer_1", Mode.GRAY, TRAV));
    }

    // ---------------- cor ----------------
    private static float[] hsv(int r, int g, int b) {
        float rf = r / 255f, gf = g / 255f, bf = b / 255f;
        float max = Math.max(rf, Math.max(gf, bf)), min = Math.min(rf, Math.min(gf, bf)), d = max - min;
        float h = 0;
        if (d > 1e-5f) {
            if (max == rf) h = ((gf - bf) / d) % 6f;
            else if (max == gf) h = (bf - rf) / d + 2f;
            else h = (rf - gf) / d + 4f;
            h *= 60f;
            if (h < 0) h += 360f;
        }
        return new float[]{h, max <= 0 ? 0 : d / max, max};
    }

    private static int rgbFromHsv(float h, float s, float v) {
        float c = v * s, x = c * (1 - Math.abs((h / 60f) % 2 - 1)), m = v - c;
        float r, g, b;
        if (h < 60) { r = c; g = x; b = 0; } else if (h < 120) { r = x; g = c; b = 0; }
        else if (h < 180) { r = 0; g = c; b = x; } else if (h < 240) { r = 0; g = x; b = c; }
        else if (h < 300) { r = x; g = 0; b = c; } else { r = c; g = 0; b = x; }
        return (clamp((r + m) * 255) << 16) | (clamp((g + m) * 255) << 8) | clamp((b + m) * 255);
    }

    private static int clamp(float v) {
        return Math.max(0, Math.min(255, Math.round(v)));
    }

    /** px em ABGR (formato do NativeImage) -> ABGR recolorido. */
    private static int recolor(int px, Layer l) {
        int a = (px >>> 24) & 255;
        if (a == 0 || l.mode == Mode.NONE) return px;
        int r = px & 255, g = (px >> 8) & 255, b = (px >> 16) & 255;
        float[] p = hsv(r, g, b);
        int tr = (l.color >> 16) & 255, tg = (l.color >> 8) & 255, tb = l.color & 255;
        int out;
        if (l.mode == Mode.GRAY) {
            if (p[1] > 0.22f) return px; // só pinta os tons de cinza (cabos de madeira ficam iguais)
            float lum = (0.299f * r + 0.587f * g + 0.114f * b) / 255f;
            float k = lum * 1.18f;
            float w = Math.max(0, lum - 0.82f) * 2.2f; // brilhos puxam para o branco
            out = (clamp(tr * k * (1 - w) + 255 * w) << 16) | (clamp(tg * k * (1 - w) + 255 * w) << 8) | clamp(tb * k * (1 - w) + 255 * w);
        } else {
            if (p[1] < 0.12f) return px;
            if (l.mode == Mode.HUE_KEEP_BROWN && p[0] >= 12 && p[0] <= 50) return px;
            float[] t = hsv(tr, tg, tb);
            float s = Math.min(1f, Math.max(p[1], t[1] * 0.6f) * l.sat);
            out = rgbFromHsv(t[0], s, p[2]);
        }
        return (a << 24) | ((out & 255) << 16) | (out & 0xFF00) | ((out >> 16) & 255);
    }

    private static byte[] generate(String path) {
        List<Layer> layers = RECIPES.get(path);
        if (layers == null) return MISSING;
        NativeImage canvas = null;
        try {
            for (Layer l : layers) {
                IoSupplier<InputStream> src = Minecraft.getInstance().getVanillaPackResources()
                        .getResource(PackType.CLIENT_RESOURCES, ResourceLocation.withDefaultNamespace("textures/" + l.base + ".png"));
                if (src == null) {
                    if (canvas == null) return MISSING;
                    continue;
                }
                try (InputStream in = src.get(); NativeImage img = NativeImage.read(in)) {
                    int w = img.getWidth(), h = Math.min(img.getHeight(), img.getWidth() * (path.contains("models/armor") ? 1 : 1));
                    if (path.contains("models/armor")) h = img.getHeight();
                    if (canvas == null) canvas = new NativeImage(w, h, true);
                    for (int y = 0; y < Math.min(h, canvas.getHeight()); y++) {
                        for (int x = 0; x < Math.min(w, canvas.getWidth()); x++) {
                            int px = recolor(img.getPixelRGBA(x, y), l);
                            int sa = (px >>> 24) & 255;
                            if (sa == 0) continue;
                            if (sa == 255) canvas.setPixelRGBA(x, y, px);
                            else canvas.blendPixel(x, y, px);
                        }
                    }
                }
            }
            return canvas == null ? MISSING : canvas.asByteArray();
        } catch (Exception e) {
            return MISSING;
        } finally {
            if (canvas != null) canvas.close();
        }
    }

    // ---------------- pacote ----------------
    private final PackLocationInfo info;

    private VanillaTintPack(PackLocationInfo info) {
        this.info = info;
    }

    public static Pack create() {
        PackLocationInfo info = new PackLocationInfo(ID, Component.literal("Primeva: cores vanilla"), PackSource.BUILT_IN, Optional.empty());
        Pack.ResourcesSupplier sup = new Pack.ResourcesSupplier() {
            @Override
            public PackResources openPrimary(PackLocationInfo i) {
                return new VanillaTintPack(i);
            }

            @Override
            public PackResources openFull(PackLocationInfo i, Pack.Metadata m) {
                return new VanillaTintPack(i);
            }
        };
        return Pack.readMetaAndCreate(info, sup, PackType.CLIENT_RESOURCES, new PackSelectionConfig(true, Pack.Position.TOP, false));
    }

    @Nullable
    @Override
    public IoSupplier<InputStream> getRootResource(String... elements) {
        return null;
    }

    @Nullable
    @Override
    public IoSupplier<InputStream> getResource(PackType type, ResourceLocation loc) {
        if (type != PackType.CLIENT_RESOURCES || !loc.getNamespace().equals("primeva") || !RECIPES.containsKey(loc.getPath())) return null;
        byte[] data = CACHE.computeIfAbsent(loc.getPath(), VanillaTintPack::generate);
        if (data.length == 0) return null;
        return () -> new ByteArrayInputStream(data);
    }

    @Override
    public void listResources(PackType type, String namespace, String path, ResourceOutput out) {
        if (type != PackType.CLIENT_RESOURCES || !namespace.equals("primeva")) return;
        for (String p : RECIPES.keySet()) {
            if (!p.startsWith(path + "/") && !p.equals(path)) continue;
            byte[] data = CACHE.computeIfAbsent(p, VanillaTintPack::generate);
            if (data.length == 0) continue;
            out.accept(ResourceLocation.fromNamespaceAndPath("primeva", p), () -> new ByteArrayInputStream(data));
        }
    }

    @Override
    public Set<String> getNamespaces(PackType type) {
        return type == PackType.CLIENT_RESOURCES ? Set.of("primeva") : Set.of();
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <T> T getMetadataSection(MetadataSectionSerializer<T> serializer) {
        if (serializer == PackMetadataSection.TYPE) {
            return (T) new PackMetadataSection(Component.literal("Texturas do Primeva com o formato do vanilla"),
                    SharedConstants.getCurrentVersion().getPackVersion(PackType.CLIENT_RESOURCES), Optional.empty());
        }
        return null;
    }

    @Override
    public PackLocationInfo location() {
        return info;
    }

    @Override
    public void close() {
    }

    /** Limpa o cache quando os recursos recarregam (F3+T). */
    public static void clearCache() {
        CACHE.clear();
    }
}
