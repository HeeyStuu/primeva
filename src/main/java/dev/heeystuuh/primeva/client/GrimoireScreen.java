package dev.heeystuuh.primeva.client;

import dev.heeystuuh.primeva.ClientKnowledge;
import dev.heeystuuh.primeva.Essence;
import dev.heeystuuh.primeva.Knowledge;
import dev.heeystuuh.primeva.ModItems;
import dev.heeystuuh.primeva.ModRecipes;
import dev.heeystuuh.primeva.PrimevaMod;
import dev.heeystuuh.primeva.Research;
import dev.heeystuuh.primeva.Warp;
import dev.heeystuuh.primeva.network.ResearchPayload;
import dev.heeystuuh.primeva.recipe.CrucibleRecipe;
import dev.heeystuuh.primeva.item.FocusItem;
import dev.heeystuuh.primeva.recipe.InfusionRecipe;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Grimório Primevo: livro aberto.
 * Página esquerda: percepção acumulada + árvore de pesquisas.
 * Página direita: detalhes da pesquisa, custo, botão de pesquisar e o diagrama do ritual no altar.
 */
public class GrimoireScreen extends Screen {
    private static final ResourceLocation SHEET = PrimevaMod.id("textures/gui/grimoire.png");
    private static final ResourceLocation ESS = PrimevaMod.id("textures/gui/essences.png");
    private static final int TW = 1024, TH = 512;
    private static final int W = 380, H = 240;

    private static final int INK = 0x3B2412, INK_SOFT = 0x6B4A2A, GOLD_INK = 0x8A5A10, PURPLE = 0x5A2A8A,
            GREEN = 0x2E6B2A, RED = 0x9A2A1A;

    /** Posição de cada pesquisa na árvore (relativa à página esquerda) e o item que a representa. */
    private record Node(String id, int x, int y, Supplier<ItemStack> icon) {}

    private static final Map<String, Supplier<ItemStack>> ICONS = new HashMap<>();
    static {
        ICONS.put("infusion", () -> new ItemStack(ModItems.ALTAR.get()));
        ICONS.put("primeval_metal", () -> new ItemStack(ModItems.PRIMEVAL_INGOT.get()));
        ICONS.put("miasma", () -> new ItemStack(ModItems.PURIFIER.get()));
        ICONS.put("purification", () -> new ItemStack(ModItems.PURIFYING_INCENSE.get()));
        ICONS.put("gale_boots", () -> new ItemStack(ModItems.GALE_BOOTS.get()));
        ICONS.put("umbral_blade", () -> new ItemStack(ModItems.UMBRAL_BLADE.get()));
        ICONS.put("elemental_wands", () -> new ItemStack(ModItems.WANDS.get(Essence.EMBER).get()));
        ICONS.put("transmutation", () -> new ItemStack(Items.DIAMOND));
        ICONS.put("salt", () -> new ItemStack(ModItems.PRIMORDIAL_SALT.get()));
        ICONS.put("crucible", () -> new ItemStack(ModItems.CRUCIBLE.get()));
        ICONS.put("essence_logistics", () -> new ItemStack(ModItems.ESSENCE_JAR.get()));
        ICONS.put("arcanite", () -> new ItemStack(ModItems.ARCANITE_INGOT.get()));
        ICONS.put("foci", () -> new ItemStack(ModItems.FOCI.get(FocusItem.Kind.FROST).get()));
        ICONS.put("eternal_flame", () -> new ItemStack(ModItems.ETERNAL_FLAME.get()));
        ICONS.put("goggles", () -> new ItemStack(ModItems.REVEALING_GOGGLES.get()));
        ICONS.put("runic_ring", () -> new ItemStack(ModItems.RUNIC_RING.get()));
        ICONS.put("advanced_foci", () -> new ItemStack(ModItems.FOCI.get(FocusItem.Kind.HOLE).get()));
        ICONS.put("arcane_devices", () -> new ItemStack(ModItems.HUNGRY_CHEST.get()));
        ICONS.put("golemancy", () -> new ItemStack(ModItems.ARCANE_GOLEM.get()));
        ICONS.put("elemental_tools", () -> new ItemStack(ModItems.EMBER_PICKAXE.get()));
        ICONS.put("advanced_seals", () -> new ItemStack(ModItems.SEAL_GUARD.get()));
        ICONS.put("golem_cores", () -> new ItemStack(ModItems.GOLEM_CORE_BLANK.get()));
        ICONS.put("traveler_boots", () -> new ItemStack(ModItems.TRAVELER_BOOTS.get()));
        ICONS.put("trinkets", () -> new ItemStack(ModItems.VERDANT_CHARM.get()));
        ICONS.put("grapple", () -> new ItemStack(ModItems.GRAPPLE.get()));
        ICONS.put("pocket_mirror", () -> new ItemStack(ModItems.POCKET_MIRROR.get()));
        ICONS.put("crimson_cult", () -> new ItemStack(ModItems.CRIMSON_SCYTHE.get()));
        ICONS.put("herald", () -> new ItemStack(ModItems.HERALD_SIGIL.get()));
        ICONS.put("wraiths", () -> new ItemStack(ModItems.MIASMIC_RESIDUE.get()));
        ICONS.put("study", () -> new ItemStack(ModItems.STUDY_TABLE.get()));
        ICONS.put("containment", () -> new ItemStack(ModItems.CONTAINMENT_PILLAR.get()));
        ICONS.put("crystals", () -> new ItemStack(ModItems.CRYSTALS.get(Essence.LUMEN).get()));
        ICONS.put("elixirs", () -> new ItemStack(ModItems.ELIXIR_PRIMEVAL.get()));
        ICONS.put("greater_wands", () -> new ItemStack(ModItems.WANDS.get(Essence.UMBRA).get()));
        ICONS.put("primeval_armor", () -> new ItemStack(ModItems.PRIMEVAL_CHESTPLATE.get()));
        ICONS.put("primeval_tools", () -> new ItemStack(ModItems.PRIMEVAL_PICKAXE.get()));
        ICONS.put("voidsteel", () -> new ItemStack(ModItems.VOIDSTEEL_INGOT.get()));
        ICONS.put("catalyst", () -> new ItemStack(ModItems.CATALYST.get()));
        ICONS.put("eclipse_reaper", () -> new ItemStack(ModItems.ECLIPSE_REAPER.get()));
        ICONS.put("archmage_crown", () -> new ItemStack(ModItems.ARCHMAGE_CROWN.get()));
        ICONS.put("sanity", () -> new ItemStack(ModItems.MIND_MIRROR.get()));
        ICONS.put("warding", () -> new ItemStack(ModItems.WARDING_STONE.get()));
        ICONS.put("astronomy", () -> new ItemStack(ModItems.ASTRAL_SEXTANT.get()));
        ICONS.put("essence_arrows", () -> new ItemStack(ModItems.ARROWS.get(Essence.EMBER).get()));
        ICONS.put("elder_bow", () -> new ItemStack(ModItems.ELDER_BOW.get()));
        ICONS.put("cloud_ring", () -> new ItemStack(ModItems.CLOUD_RING.get()));
        ICONS.put("arcane_lamps", () -> new ItemStack(ModItems.GROWTH_LAMP.get()));
        ICONS.put("levitation", () -> new ItemStack(ModItems.LEVITATOR.get()));
        ICONS.put("voidsteel_armor", () -> new ItemStack(ModItems.VOIDSTEEL_CHESTPLATE.get()));
        ICONS.put("primeval_heart", () -> new ItemStack(ModItems.PRIMEVAL_HEART.get()));
    }

    /** Organiza cada categoria automaticamente em linhas pela profundidade dos pré-requisitos. */
    private static final List<List<Node>> CHAPTERS = new ArrayList<>();
    static {
        for (int ch = 0; ch < Research.CHAPTERS; ch++) {
            Map<String, Integer> depth = new LinkedHashMap<>();
            List<Research> in = new ArrayList<>();
            for (Research r : Research.ALL) if (r.chapter() == ch) in.add(r);
            for (int pass = 0; pass < 8; pass++) {
                for (Research r : in) {
                    int d = 0;
                    for (String p : r.prereqs()) {
                        Research pr = Research.get(p);
                        if (pr != null && pr.chapter() == ch) d = Math.max(d, depth.getOrDefault(p, 0) + 1);
                    }
                    depth.put(r.id(), d);
                }
            }
            int maxD = 0;
            for (int d : depth.values()) maxD = Math.max(maxD, d);
            List<List<String>> rows = new ArrayList<>();
            for (int d = 0; d <= maxD; d++) {
                List<String> row = new ArrayList<>();
                for (Research r : in) if (depth.get(r.id()) == d) row.add(r.id());
                for (int i = 0; i < row.size(); i += 6) rows.add(row.subList(i, Math.min(row.size(), i + 6)));
            }
            int rowH = rows.size() <= 1 ? 0 : Math.min(34, 108 / (rows.size() - 1));
            List<Node> nodes = new ArrayList<>();
            for (int ri = 0; ri < rows.size(); ri++) {
                List<String> row = rows.get(ri);
                int n = row.size();
                int spacing = Math.min(30, 144 / Math.max(1, n));
                int x0 = 72 - spacing * (n - 1) / 2;
                for (int i = 0; i < n; i++) {
                    String id = row.get(i);
                    nodes.add(new Node(id, x0 + i * spacing, 66 + ri * rowH, ICONS.getOrDefault(id, () -> new ItemStack(Items.BOOK))));
                }
            }
            CHAPTERS.add(nodes);
        }
    }
    private static final List<Supplier<ItemStack>> CHAPTER_ICONS = List.of(
            () -> new ItemStack(ModItems.phial(Essence.LUMEN)),
            () -> new ItemStack(ModItems.CRUCIBLE.get()),
            () -> new ItemStack(ModItems.ALTAR.get()),
            () -> new ItemStack(ModItems.CRIMSON_SCYTHE.get()),
            () -> new ItemStack(ModItems.RUNIC_RING.get()),
            () -> new ItemStack(ModItems.ARCANE_GOLEM.get()));

    /** Aba extra: Guia do Primeva (como o mod funciona, por tópicos). */
    private static final int GUIDE = Research.CHAPTERS;
    private static final int GUIDE_LINES = 16, GUIDE_ROW = 13;
    private static final List<String> GUIDE_TOPICS = List.of("start", "essences", "research", "distilling", "crucible", "infusion",
            "miasma", "mind", "magic", "golems", "gear", "world");
    private static final Map<String, Supplier<ItemStack>> GUIDE_ICONS = Map.ofEntries(
            Map.entry("start", () -> new ItemStack(ModItems.GRIMOIRE.get())),
            Map.entry("essences", () -> new ItemStack(ModItems.LENS.get())),
            Map.entry("research", () -> new ItemStack(ModItems.RESEARCH_SCROLL.get())),
            Map.entry("distilling", () -> new ItemStack(ModItems.DISTILLER.get())),
            Map.entry("crucible", () -> new ItemStack(ModItems.CRUCIBLE.get())),
            Map.entry("infusion", () -> new ItemStack(ModItems.ALTAR.get())),
            Map.entry("miasma", () -> new ItemStack(ModItems.PURIFIER.get())),
            Map.entry("mind", () -> new ItemStack(ModItems.MIND_MIRROR.get())),
            Map.entry("magic", () -> new ItemStack(ModItems.WANDS.get(Essence.GALE).get())),
            Map.entry("golems", () -> new ItemStack(ModItems.ARCANE_GOLEM.get())),
            Map.entry("gear", () -> new ItemStack(ModItems.PRIMEVAL_CHESTPLATE.get())),
            Map.entry("world", () -> new ItemStack(ModItems.CRYSTALS.get(Essence.TERRA).get())));
    private int guideTopic;

    private int chapter;
    private List<Node> nodes() {
        return CHAPTERS.get(chapter);
    }

    // posições calculadas no último desenho (usadas nos cliques)
    private int lastButtonY = -1000, lastArrowY = -1000;

    private int left, top;
    private Research selected = Research.get(CHAPTERS.get(0).get(0).id());
    private int recipeIndex;
    private ItemStack hoverStack = ItemStack.EMPTY;
    private List<Component> hoverText = null;

    public GrimoireScreen() {
        super(Component.translatable("item.primeva.grimoire"));
    }

    @Override
    protected void init() {
        left = (width - W) / 2;
        top = (height - H) / 2;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    // ------------------------------------------------------------------ desenho

    private void sheet(GuiGraphics g, int x, int y, int w, int h, int u, int v, int uw, int vh) {
        g.blit(SHEET, x, y, w, h, u, v, uw, vh, TW, TH);
    }

    private void essenceIcon(GuiGraphics g, Essence e, int x, int y, int size) {
        g.blit(ESS, x, y, size, size, e.ordinal() * 32, 0, 32, 32, 192, 32);
    }

    private boolean in(int mx, int my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        renderBackground(g, mx, my, pt);
        hoverStack = ItemStack.EMPTY;
        hoverText = null;
        Knowledge k = ClientKnowledge.get();
        lastButtonY = -1000;
        lastArrowY = -1000;

        sheet(g, left, top, W, H, 0, 0, 760, 480);
        drawTabs(g, k, mx, my);

        int lx = left + 18, rx = left + 196, pw = 168;
        if (chapter == GUIDE) {
            drawGuideLeft(g, lx, top + 14, pw, mx, my);
            drawGuideRight(g, rx, top + 14, pw);
        } else {
            drawLeftPage(g, k, lx, top + 14, pw, mx, my);
            drawRightPage(g, k, rx, top + 14, pw, mx, my);
        }

        if (!hoverStack.isEmpty()) g.renderTooltip(font, hoverStack, mx, my);
        else if (hoverText != null) g.renderComponentTooltip(font, hoverText, mx, my);
    }

    private void centered(GuiGraphics g, Component c, int cx, int y, int color) {
        g.drawString(font, c, cx - font.width(c) / 2, y, color, false);
    }

    private void drawLeftPage(GuiGraphics g, Knowledge k, int x, int y, int w, int mx, int my) {
        g.pose().pushPose();
        g.pose().translate(x + w / 2f, y, 0);
        g.pose().scale(1.25f, 1.25f, 1f);
        Component title = Component.translatable("item.primeva.grimoire");
        g.drawString(font, title, -font.width(title) / 2, 0, GOLD_INK, false);
        g.pose().popPose();
        sheet(g, x + 14, y + 14, 140, 10, 770, 0, 240, 20);

        // percepção acumulada (2 linhas x 3)
        for (Essence e : Essence.values()) {
            int i = e.ordinal();
            int ex = x + 8 + (i % 3) * 54, ey = y + 28 + (i / 3) * 14;
            essenceIcon(g, e, ex, ey, 11);
            g.drawString(font, String.valueOf(k.get(e)), ex + 14, ey + 2, INK, false);
            if (in(mx, my, ex, ey, 46, 12)) hoverText = List.of(e.displayName(), Component.translatable("gui.primeva.insight_hint"));
        }

        if (!Research.chapterOpen(k, chapter)) {
            int ly2 = y + 100;
            for (FormattedCharSequence line : font.split(Component.translatable("gui.primeva.chapter_locked." + chapter), w - 20)) {
                g.drawString(font, line, x + w / 2 - font.width(line) / 2, ly2, RED, false);
                ly2 += 10;
            }
        }

        // conexões da árvore
        for (Node n : nodes()) {
            Research r = Research.get(n.id);
            if (r == null) continue;
            for (String pre : r.prereqs()) {
                Node p = node(pre);
                if (p == null) continue;
                boolean lit = k.research.contains(pre);
                int col = lit ? 0xFFB8862A : 0xFF9C8A6A;
                int x1 = x + p.x + 12, y1 = y + p.y + 24, x2 = x + n.x + 12, y2 = y + n.y;
                int midY = (y1 + y2) / 2;
                g.fill(x1 - 1, y1, x1 + 1, midY + 1, col);
                g.fill(Math.min(x1, x2) - 1, midY - 1, Math.max(x1, x2) + 1, midY + 1, col);
                g.fill(x2 - 1, midY, x2 + 1, y2, col);
            }
        }

        // nós
        for (Node n : nodes()) {
            Research r = Research.get(n.id);
            if (r == null) continue;
            int nx = x + n.x, ny = y + n.y;
            boolean hidden = r.hidden(k);
            int state = r.done(k) ? 2 : r.unlocked(k) ? 1 : 0;
            if (r == selected) sheet(g, nx - 3, ny - 3, 30, 30, 920, 260, 56, 56);
            sheet(g, nx, ny, 24, 24, 770 + state * 50, 260, 48, 48);
            if (hidden) {
                g.fill(nx + 3, ny + 3, nx + 21, ny + 21, 0xCC2A2018);
                centered(g, Component.literal("?"), nx + 12, ny + 8, 0xC8B898);
            } else {
                g.renderItem(n.icon.get(), nx + 4, ny + 4);
                if (state == 0) g.fill(nx + 3, ny + 3, nx + 21, ny + 21, 0x88302820);
                if (state == 1) {
                    // brilho pulsando nas pesquisas prontas para fazer
                    int a = (int) (60 + 50 * Math.sin(System.currentTimeMillis() / 250.0));
                    g.fill(nx + 1, ny + 1, nx + 23, ny + 2, (a << 24) | 0xFFD860);
                    g.fill(nx + 1, ny + 22, nx + 23, ny + 23, (a << 24) | 0xFFD860);
                }
            }
            if (in(mx, my, nx, ny, 24, 24)) {
                String key = hidden ? "gui.primeva.hidden" : state == 2 ? "gui.primeva.done" : state == 1 ? "gui.primeva.available"
                        : !r.cluesDone(k) ? "gui.primeva.discovering" : "gui.primeva.locked";
                hoverText = List.of(hidden ? Component.literal("???") : r.title(), Component.translatable(key));
            }
        }

        Component chap = Component.translatable("gui.primeva.chapter." + chapter);
        int doneN = Research.doneIn(k, chapter), total = Research.countIn(chapter);
        centered(g, chap.copy().append(Component.literal("  " + doneN + "/" + total)), x + w / 2, y + 52, PURPLE);
        int bw = 100, bx = x + w / 2 - bw / 2;
        g.fill(bx, y + 62, bx + bw, y + 64, 0x55302010);
        g.fill(bx, y + 62, bx + bw * doneN / Math.max(1, total), y + 64, 0xFFB8862A);
        Component found = Component.translatable("gui.primeva.perceived", k.scanned.size());
        Component mind = Component.translatable("gui.primeva.mind", k.warp, k.tempWarp);
        int fw = font.width(found) + 10 + font.width(mind);
        int fx = x + w / 2 - fw / 2;
        g.drawString(font, found, fx, y + 204, INK_SOFT, false);
        g.drawString(font, mind, fx + font.width(found) + 10, y + 204, PURPLE, false);
        if (in(mx, my, fx + font.width(found) + 10, y + 204, font.width(mind), 9)) {
            hoverText = List.of(Component.translatable("gui.primeva.mind_title"), Component.translatable("gui.primeva.mind_hint"));
        }
    }

    private Node node(String id) {
        for (Node n : nodes()) if (n.id.equals(id)) return n;
        return null;
    }

    private void drawTabs(GuiGraphics g, Knowledge k, int mx, int my) {
        for (int i = 0; i <= Research.CHAPTERS; i++) {
            int tx = left - 22, ty = top + 22 + i * 30;
            if (i == GUIDE) {
                int state = chapter == GUIDE ? 1 : 2;
                if (chapter == GUIDE) sheet(g, tx - 3, ty - 3, 30, 30, 920, 260, 56, 56);
                sheet(g, tx, ty, 24, 24, 770 + state * 50, 260, 48, 48);
                g.renderItem(new ItemStack(Items.WRITABLE_BOOK), tx + 4, ty + 4);
                if (in(mx, my, tx, ty, 24, 24)) hoverText = List.of(Component.translatable("gui.primeva.guide"), Component.translatable("gui.primeva.guide_hint"));
                continue;
            }
            boolean open = Research.chapterOpen(k, i);
            int state = i == chapter ? 1 : open ? 2 : 0;
            if (i == chapter) sheet(g, tx - 3, ty - 3, 30, 30, 920, 260, 56, 56);
            sheet(g, tx, ty, 24, 24, 770 + state * 50, 260, 48, 48);
            g.renderItem(CHAPTER_ICONS.get(i).get(), tx + 4, ty + 4);
            if (!open) g.fill(tx + 3, ty + 3, tx + 21, ty + 21, 0x99302820);
            if (in(mx, my, tx, ty, 24, 24)) {
                List<Component> tip = new ArrayList<>();
                tip.add(Component.translatable("gui.primeva.chapter." + i));
                if (!open) tip.add(Component.translatable("gui.primeva.chapter_locked." + i));
                hoverText = tip;
            }
        }
    }

    // ------------------------------------------------------------------ guia

    private void drawGuideLeft(GuiGraphics g, int x, int y, int w, int mx, int my) {
        g.pose().pushPose();
        g.pose().translate(x + w / 2f, y, 0);
        g.pose().scale(1.25f, 1.25f, 1f);
        Component title = Component.translatable("gui.primeva.guide");
        g.drawString(font, title, -font.width(title) / 2, 0, GOLD_INK, false);
        g.pose().popPose();
        sheet(g, x + 14, y + 14, 140, 10, 770, 0, 240, 20);
        int ry = y + 30;
        for (int i = 0; i < GUIDE_TOPICS.size(); i++) {
            String id = GUIDE_TOPICS.get(i);
            boolean sel = i == guideTopic, hover = in(mx, my, x + 4, ry - 2, w - 8, GUIDE_ROW);
            if (sel) g.fill(x + 4, ry - 2, x + w - 4, ry + GUIDE_ROW - 2, 0x40B8862A);
            else if (hover) g.fill(x + 4, ry - 2, x + w - 4, ry + GUIDE_ROW - 2, 0x20B8862A);
            g.pose().pushPose();
            g.pose().translate(x + 8, ry - 1, 0);
            g.pose().scale(0.65f, 0.65f, 1f);
            g.renderItem(GUIDE_ICONS.get(id).get(), 0, 0);
            g.pose().popPose();
            g.drawString(font, Component.translatable("guide.primeva." + id + ".title"), x + 22, ry + 1, sel ? PURPLE : INK, false);
            ry += GUIDE_ROW;
        }
        for (FormattedCharSequence line : font.split(Component.translatable("gui.primeva.guide_footer"), w - 8)) {
            g.drawString(font, line, x + w / 2 - font.width(line) / 2, Math.max(ry + 4, y + 194), INK_SOFT, false);
            ry += 10;
        }
    }

    private void drawGuideRight(GuiGraphics g, int x, int y, int w) {
        String id = GUIDE_TOPICS.get(guideTopic);
        g.pose().pushPose();
        g.pose().translate(x + w / 2f, y, 0);
        g.pose().scale(1.2f, 1.2f, 1f);
        Component t = Component.translatable("guide.primeva." + id + ".title");
        g.drawString(font, t, -font.width(t) / 2, 0, PURPLE, false);
        g.pose().popPose();
        sheet(g, x + 14, y + 13, 140, 10, 770, 0, 240, 20);
        List<FormattedCharSequence> lines = font.split(Component.translatable("guide.primeva." + id + ".text"), w - 4);
        pageCount = Math.max(1, (lines.size() + GUIDE_LINES - 1) / GUIDE_LINES);
        page = Math.floorMod(page, pageCount);
        int cy = y + 26;
        for (int i = page * GUIDE_LINES; i < Math.min(lines.size(), (page + 1) * GUIDE_LINES); i++) {
            g.drawString(font, lines.get(i), x + 2, cy, INK, false);
            cy += 10;
        }
        if (pageCount > 1) drawArrows(g, x, w, y + 196, pageCount);
    }

    // ------------------------------------------------------------------ páginas da direita

    /** Itens de bancada que cada pesquisa ensina (as receitas aparecem desenhadas no Grimório). */
    private static final Map<String, List<String>> CRAFTS = Map.ofEntries(
            Map.entry("infusion", List.of("altar", "pedestal", "distiller", "lens", "empty_phial", "research_scroll")),
            Map.entry("salt", List.of("primordial_salt")),
            Map.entry("essence_logistics", List.of("essence_jar", "essence_tube", "essence_filter_tube")),
            Map.entry("arcanite", List.of("arcanite_pickaxe", "arcanite_sword", "arcanite_axe", "arcanite_shovel", "arcanite_hoe",
                    "arcanite_helmet", "arcanite_chestplate", "arcanite_leggings", "arcanite_boots", "arcanite_block")),
            Map.entry("voidsteel", List.of("voidsteel_pickaxe", "voidsteel_sword", "voidsteel_axe", "voidsteel_shovel")),
            Map.entry("crimson_cult", List.of("crimson_helmet", "crimson_chestplate", "crimson_leggings", "crimson_boots")),
            Map.entry("containment", List.of("runic_stabilizer")),
            Map.entry("sanity", List.of("mind_mirror")),
            Map.entry("astronomy", List.of("astral_sextant")),
            Map.entry("voidsteel_armor", List.of("voidsteel_helmet", "voidsteel_chestplate", "voidsteel_leggings", "voidsteel_boots")),
            Map.entry("study", List.of("study_table")));

    private record Page(int kind, Object data) {}
    private static final int P_OVERVIEW = 0, P_TEXT = 1, P_INFUSION = 2, P_CRUCIBLE = 3, P_CRAFT = 4;
    private int page;
    private int pageCount = 1;
    private List<Page> cachedPages;
    private String cachedId;
    private boolean cachedDone;

    private List<Page> pagesFor(Research r, Knowledge k) {
        List<Page> out = new ArrayList<>();
        out.add(new Page(P_OVERVIEW, null));
        for (int i = 1; i <= 4; i++) {
            String key = "research.primeva." + r.id() + ".page" + i;
            if (net.minecraft.client.resources.language.I18n.exists(key)) out.add(new Page(P_TEXT, key));
        }
        if (!r.done(k) || Minecraft.getInstance().level == null) return out;
        var rm = Minecraft.getInstance().level.getRecipeManager();
        for (RecipeHolder<InfusionRecipe> h : rm.getAllRecipesFor(ModRecipes.INFUSION_TYPE.get())) if (h.value().research.equals(r.id())) out.add(new Page(P_INFUSION, h.value()));
        for (RecipeHolder<CrucibleRecipe> h : rm.getAllRecipesFor(ModRecipes.CRUCIBLE_TYPE.get())) if (h.value().research.equals(r.id())) out.add(new Page(P_CRUCIBLE, h.value()));
        List<String> crafts = CRAFTS.get(r.id());
        if (crafts != null) {
            var reg = Minecraft.getInstance().level.registryAccess();
            for (String id : crafts) {
                for (RecipeHolder<net.minecraft.world.item.crafting.CraftingRecipe> h : rm.getAllRecipesFor(net.minecraft.world.item.crafting.RecipeType.CRAFTING)) {
                    ItemStack res = h.value().getResultItem(reg);
                    if (!res.isEmpty() && net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(res.getItem()).equals(PrimevaMod.id(id))) {
                        out.add(new Page(P_CRAFT, h.value()));
                        break;
                    }
                }
            }
        }
        return out;
    }

    private void drawRightPage(GuiGraphics g, Knowledge k, int x, int y, int w, int mx, int my) {
        Research r = selected;
        if (r.hidden(k)) {
            drawHidden(g, k, r, x, y, w);
            return;
        }
        if (cachedPages == null || !r.id().equals(cachedId) || r.done(k) != cachedDone) {
            cachedPages = pagesFor(r, k);
            cachedId = r.id();
            cachedDone = r.done(k);
        }
        List<Page> pages = cachedPages;
        pageCount = pages.size();
        page = Math.floorMod(page, pageCount);
        Page pg = pages.get(page);

        g.pose().pushPose();
        g.pose().translate(x + w / 2f, y, 0);
        g.pose().scale(1.2f, 1.2f, 1f);
        Component t = r.title();
        g.drawString(font, t, -font.width(t) / 2, 0, PURPLE, false);
        g.pose().popPose();
        sheet(g, x + 14, y + 13, 140, 10, 770, 0, 240, 20);
        int cy = y + 26;
        switch (pg.kind()) {
            case P_TEXT -> {
                for (FormattedCharSequence line : font.split(Component.translatable((String) pg.data()), w - 4)) {
                    g.drawString(font, line, x + 2, cy, INK, false);
                    cy += 10;
                }
            }
            case P_INFUSION -> drawInfusion(g, (InfusionRecipe) pg.data(), x, cy, w, mx, my);
            case P_CRUCIBLE -> drawCrucible(g, (CrucibleRecipe) pg.data(), x, cy, w, mx, my);
            case P_CRAFT -> drawCrafting(g, (net.minecraft.world.item.crafting.CraftingRecipe) pg.data(), x, cy, w, mx, my);
            default -> drawOverview(g, k, r, x, cy, w, mx, my);
        }
        if (pageCount > 1) drawArrows(g, x, w, y + 196, pageCount);
    }

    private void drawHidden(GuiGraphics g, Knowledge k, Research r, int x, int y, int w) {
        g.pose().pushPose();
        g.pose().translate(x + w / 2f, y, 0);
        g.pose().scale(1.2f, 1.2f, 1f);
        g.drawString(font, "???", -font.width("???") / 2, 0, PURPLE, false);
        g.pose().popPose();
        sheet(g, x + 14, y + 13, 140, 10, 770, 0, 240, 20);
        int hy = y + 30;
        for (FormattedCharSequence line : font.split(Component.translatable("gui.primeva.hidden_hint"), w - 4)) {
            g.drawString(font, line, x + 2, hy, INK_SOFT, false);
            hy += 10;
        }
        hy += 4;
        for (String p : r.prereqs()) {
            Research pr = Research.get(p);
            if (pr == null) continue;
            boolean ok = k.research.contains(p);
            Component c = Component.literal(ok ? "✔ " : "✖ ").append(pr.hidden(k) ? Component.literal("???") : pr.title());
            g.drawString(font, c, x + 4, hy, ok ? GREEN : RED, false);
            hy += 11;
        }
    }

    private void drawOverview(GuiGraphics g, Knowledge k, Research r, int x, int cy, int w, int mx, int my) {
        boolean done = r.done(k), unlocked = r.unlocked(k);
        Component status = Component.translatable(done ? "gui.primeva.done" : unlocked ? "gui.primeva.available"
                : !r.cluesDone(k) ? "gui.primeva.discovering" : "gui.primeva.locked");
        centered(g, status, x + w / 2, cy, done ? GREEN : unlocked ? GOLD_INK : RED);
        cy += 12;
        for (FormattedCharSequence line : font.split(Component.translatable("research.primeva." + r.id() + ".desc"), w - 4)) {
            g.drawString(font, line, x + 2, cy, INK, false);
            cy += 10;
        }
        cy += 3;
        int warp = Warp.RESEARCH_WARP.getOrDefault(r.id(), 0);
        if (warp > 0) {
            g.drawString(font, Component.translatable("gui.primeva.forbidden", warp), x + 2, cy, PURPLE, false);
            cy += 11;
        }
        if (done) {
            if (pageCount > 1) {
                for (FormattedCharSequence line : font.split(Component.translatable("gui.primeva.turn_pages"), w - 4)) {
                    g.drawString(font, line, x + 2, cy + 4, INK_SOFT, false);
                    cy += 10;
                }
            }
            return;
        }
        if (!r.clues().isEmpty() && !r.cluesDone(k)) {
            g.drawString(font, Component.translatable("gui.primeva.steps"), x + 2, cy, GOLD_INK, false);
            cy += 11;
            for (String c : r.clues()) {
                boolean ok = Research.clueDone(k, c);
                Component line = Component.literal(ok ? "✔ " : "○ ").append(Research.clueText(c));
                for (FormattedCharSequence l : font.split(line, w - 6)) {
                    g.drawString(font, l, x + 4, cy, ok ? GREEN : INK, false);
                    cy += 10;
                }
            }
            cy += 3;
        }
        if (Research.REWARDS.containsKey(r.id())) {
            ItemStack rw = Research.REWARDS.get(r.id()).get();
            Component lbl = Component.translatable("gui.primeva.reward");
            g.drawString(font, lbl, x + 2, cy + 4, INK_SOFT, false);
            int ix = x + 4 + font.width(lbl);
            g.renderItem(rw, ix, cy);
            g.renderItemDecorations(font, rw, ix, cy);
            if (in(mx, my, ix, cy, 16, 16)) hoverStack = rw;
            cy += 17;
        }
        g.drawString(font, Component.translatable("gui.primeva.cost"), x + 2, cy + 2, INK_SOFT, false);
        int startX = x + 2 + font.width(Component.translatable("gui.primeva.cost")) + 2;
        int ex = startX;
        for (Map.Entry<Essence, Integer> e : r.cost().entrySet()) {
            boolean ok = k.get(e.getKey()) >= r.need(e.getKey());
            String txt = k.get(e.getKey()) + "/" + r.need(e.getKey());
            int ew = 13 + font.width(txt) + 5;
            if (ex + ew > x + w && ex > startX) {
                ex = startX;
                cy += 12;
            }
            essenceIcon(g, e.getKey(), ex, cy, 11);
            g.drawString(font, txt, ex + 13, cy + 2, ok ? GREEN : RED, false);
            if (in(mx, my, ex, cy, ew, 11)) hoverText = List.of(e.getKey().displayName());
            ex += ew;
        }
        cy += 13;
        if (r.advancement() != null || r.era() > 0) {
            boolean gate = k.gates.contains(r.id());
            if (r.advancement() != null) {
                String key = "advancements." + r.advancement().substring(r.advancement().indexOf(':') + 1).replace('/', '.') + ".title";
                for (FormattedCharSequence line : font.split(Component.translatable("gui.primeva.req_advancement", Component.translatable(key)), w - 4)) {
                    g.drawString(font, line, x + 2, cy, gate ? GREEN : RED, false);
                    cy += 10;
                }
            }
            if (r.era() > 0) {
                g.drawString(font, Component.translatable("gui.primeva.req_era", Component.translatable("tooltip.techgrid.era" + r.era())), x + 2, cy, gate ? GREEN : RED, false);
                cy += 10;
            }
        }
        // botão: abre o quebra-cabeça
        int bx = x + w / 2 - 45, by = Math.max(cy + 2, 0);
        by = Math.min(by, top + 14 + 172);
        lastButtonY = by;
        boolean hover = unlocked && in(mx, my, bx, by, 90, 20);
        sheet(g, bx, by, 90, 20, 770, unlocked ? (hover ? 75 : 30) : 120, 180, 40);
        Component label = Component.translatable("gui.primeva.start_research");
        g.drawString(font, label, bx + 45 - font.width(label) / 2, by + 6, unlocked ? 0xFFF4D8 : 0xBDB4A8, true);
    }

    private void drawInfusion(GuiGraphics g, InfusionRecipe rec, int x, int top, int w, int mx, int my) {
        int ccx = x + 52, ccy = top + 50;
        int radius = 34;
        int n = rec.pedestals.size();
        for (int i = 0; i < n; i++) {
            double a = -Math.PI / 2 + i * 2 * Math.PI / n;
            int px = (int) Math.round(ccx + Math.cos(a) * radius), py = (int) Math.round(ccy + Math.sin(a) * radius);
            for (int s = 1; s < 7; s++) {
                int lx = px + (ccx - px) * s / 7, ly = py + (ccy - py) * s / 7;
                g.fill(lx - 1, ly - 1, lx + 1, ly + 1, 0xFFA060E0);
            }
            sheet(g, px - 10, py - 10, 20, 20, 770, 200, 40, 40);
            drawIngredient(g, rec.pedestals.get(i), px - 8, py - 8, true, mx, my);
        }
        sheet(g, ccx - 13, ccy - 13, 26, 26, 815, 200, 52, 52);
        drawIngredient(g, rec.center, ccx - 8, ccy - 8, true, mx, my);
        int rx2 = x + w - 26, ry2 = ccy - 8;
        sheet(g, rx2 - 18, ry2 + 2, 12, 12, 770 + 28, 170, 24, 24);
        sheet(g, rx2 - 5, ry2 - 5, 26, 26, 815, 200, 52, 52);
        g.renderItem(rec.result, rx2, ry2);
        g.renderItemDecorations(font, rec.result, rx2, ry2);
        if (in(mx, my, rx2, ry2, 16, 16)) hoverStack = rec.result;
        Component tierTxt = Component.translatable("gui.primeva.tier." + rec.tier);
        g.drawString(font, tierTxt, x + w - 4 - font.width(tierTxt), top, rec.tier >= 3 ? RED : rec.tier == 2 ? GOLD_INK : INK_SOFT, false);
        int iy = ccy + radius + 14;
        g.drawString(font, Component.translatable("gui.primeva.instability_label"), x + 2, iy + 2, INK_SOFT, false);
        int fx = x + 2 + font.width(Component.translatable("gui.primeva.instability_label")) + 2;
        for (int i = 0; i < 6; i++) sheet(g, fx + i * 10, iy, 10, 10, i < rec.instability ? 875 : 897, 200, 20, 20);
        iy += 13;
        g.drawString(font, Component.translatable("gui.primeva.drain_label"), x + 2, iy + 2, INK_SOFT, false);
        int ex = x + 2 + font.width(Component.translatable("gui.primeva.drain_label")) + 2;
        for (Map.Entry<Essence, Integer> e : rec.essenceDrain().entrySet()) {
            String txt = String.valueOf(e.getValue());
            int ew = 13 + font.width(txt) + 5;
            if (ex + ew > x + w) {
                ex = x + 4;
                iy += 12;
            }
            essenceIcon(g, e.getKey(), ex, iy, 11);
            g.drawString(font, txt, ex + 13, iy + 2, INK, false);
            ex += ew;
        }
    }

    /** Diagrama do cadinho: catalisador + essências -> resultado. */
    private void drawCrucible(GuiGraphics g, CrucibleRecipe rec, int x, int top, int w, int mx, int my) {
        g.drawString(font, Component.translatable("gui.primeva.crucible_recipe"), x + 2, top + 2, INK_SOFT, false);
        int cy = top + 18;
        int cx = x + 16;
        sheet(g, cx - 5, cy - 5, 26, 26, 815, 200, 52, 52);
        g.renderItem(new ItemStack(ModItems.CRUCIBLE.get()), cx, cy);
        if (in(mx, my, cx, cy, 16, 16)) hoverStack = new ItemStack(ModItems.CRUCIBLE.get());
        int kx = cx + 30;
        sheet(g, kx - 2, cy - 2, 20, 20, 770, 200, 40, 40);
        drawIngredient(g, rec.catalyst, kx, cy, true, mx, my);
        int rx2 = x + w - 26;
        sheet(g, rx2 - 18, cy + 2, 12, 12, 770 + 28, 170, 24, 24);
        sheet(g, rx2 - 5, cy - 5, 26, 26, 815, 200, 52, 52);
        g.renderItem(rec.result, rx2, cy);
        g.renderItemDecorations(font, rec.result, rx2, cy);
        if (in(mx, my, rx2, cy, 16, 16)) hoverStack = rec.result;
        int ey = cy + 26, ex = x + 4;
        for (Map.Entry<Essence, Integer> e : rec.essences.entrySet()) {
            String txt = "x" + e.getValue();
            int ew = 13 + font.width(txt) + 6;
            if (ex + ew > x + w) {
                ex = x + 4;
                ey += 13;
            }
            essenceIcon(g, e.getKey(), ex, ey, 11);
            g.drawString(font, txt, ex + 13, ey + 2, INK, false);
            if (in(mx, my, ex, ey, ew, 11)) hoverText = List.of(e.getKey().displayName());
            ex += ew;
        }
    }

    /** Receita de bancada desenhada numa grade 3x3. */
    private void drawCrafting(GuiGraphics g, net.minecraft.world.item.crafting.CraftingRecipe rec, int x, int top, int w, int mx, int my) {
        g.drawString(font, Component.translatable("gui.primeva.crafting_recipe"), x + 2, top + 2, INK_SOFT, false);
        int gx = x + 14, gy = top + 18;
        int cols = 3;
        List<Ingredient> ings = rec.getIngredients();
        if (rec instanceof net.minecraft.world.item.crafting.ShapedRecipe sr) cols = sr.getWidth();
        for (int i = 0; i < 9; i++) sheet(g, gx + (i % 3) * 20 - 2, gy + (i / 3) * 20 - 2, 20, 20, 770, 200, 40, 40);
        for (int i = 0; i < ings.size(); i++) {
            int col = i % cols, row = i / cols;
            if (row > 2 || col > 2) continue;
            Ingredient ing = ings.get(i);
            if (ing.isEmpty()) continue;
            drawIngredient(g, ing, gx + col * 20, gy + row * 20, true, mx, my);
        }
        var reg = Minecraft.getInstance().level.registryAccess();
        ItemStack res = rec.getResultItem(reg);
        int rx2 = x + w - 30, ry2 = gy + 20;
        sheet(g, rx2 - 22, ry2 + 2, 12, 12, 770 + 28, 170, 24, 24);
        sheet(g, rx2 - 5, ry2 - 5, 26, 26, 815, 200, 52, 52);
        g.renderItem(res, rx2, ry2);
        g.renderItemDecorations(font, res, rx2, ry2);
        if (in(mx, my, rx2, ry2, 16, 16)) hoverStack = res;
    }

    private void drawArrows(GuiGraphics g, int x, int w, int ay, int count) {
        lastArrowY = ay;
        sheet(g, x + w / 2 - 34, ay, 12, 12, 770, 170, 24, 24);
        sheet(g, x + w / 2 + 22, ay, 12, 12, 798, 170, 24, 24);
        String pageTxt = (page + 1) + "/" + count;
        g.drawString(font, pageTxt, x + w / 2 - font.width(pageTxt) / 2, ay + 2, INK, false);
    }

    private void drawIngredient(GuiGraphics g, Ingredient ing, int x, int y, boolean reveal, int mx, int my) {
        ItemStack[] items = ing.getItems();
        if (items.length == 0) return;
        ItemStack s = items[(int) ((System.currentTimeMillis() / 1000) % items.length)];
        if (reveal) {
            g.renderItem(s, x, y);
            if (in(mx, my, x, y, 16, 16)) hoverStack = s;
        } else {
            g.drawString(font, "?", x + 5, y + 4, 0xE8D8B8, false);
        }
    }

    // ------------------------------------------------------------------ cliques

    @Override
    public boolean mouseClicked(double mxd, double myd, int button) {
        int mx = (int) mxd, my = (int) myd;
        Knowledge k = ClientKnowledge.get();
        int lx = left + 18, ly = top + 14, rx = left + 196, w = 168;
        for (int i = 0; i <= Research.CHAPTERS; i++) {
            if (in(mx, my, left - 22, top + 22 + i * 30, 24, 24)) {
                if (i != chapter) {
                    chapter = i;
                    if (i != GUIDE) selected = Research.get(nodes().get(0).id());
                    page = 0;
                    pageCount = 1;
                    cachedPages = null;
                    pageSound();
                }
                return true;
            }
        }
        if (chapter == GUIDE) {
            int ry = ly + 30;
            for (int i = 0; i < GUIDE_TOPICS.size(); i++) {
                if (in(mx, my, lx + 4, ry - 2, w - 8, GUIDE_ROW)) {
                    if (i != guideTopic) {
                        guideTopic = i;
                        page = 0;
                        pageSound();
                    }
                    return true;
                }
                ry += GUIDE_ROW;
            }
            if (pageCount > 1 && lastArrowY > -1000) {
                if (in(mx, my, rx + w / 2 - 34, lastArrowY, 12, 12)) { page--; pageSound(); return true; }
                if (in(mx, my, rx + w / 2 + 22, lastArrowY, 12, 12)) { page++; pageSound(); return true; }
            }
            return super.mouseClicked(mxd, myd, button);
        }
        for (Node n : nodes()) {
            if (in(mx, my, lx + n.x(), ly + n.y(), 24, 24)) {
                Research r = Research.get(n.id());
                if (r != null && r != selected) {
                    selected = r;
                    page = 0;
                    pageSound();
                }
                return true;
            }
        }
        // páginas
        if (pageCount > 1 && lastArrowY > -1000) {
            if (in(mx, my, rx + w / 2 - 34, lastArrowY, 12, 12)) { page--; pageSound(); return true; }
            if (in(mx, my, rx + w / 2 + 22, lastArrowY, 12, 12)) { page++; pageSound(); return true; }
        }
        // iniciar pesquisa: abre o quebra-cabeça
        if (page == 0 && !selected.hidden(k) && !selected.done(k) && selected.unlocked(k) && lastButtonY > -1000
                && in(mx, my, rx + w / 2 - 45, lastButtonY, 90, 20)) {
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 0.8f));
            Minecraft.getInstance().setScreen(new ResearchPuzzleScreen(this, selected));
            return true;
        }
        return super.mouseClicked(mxd, myd, button);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double sx, double sy) {
        if (pageCount > 1) {
            page += sy < 0 ? 1 : -1;
            pageSound();
            return true;
        }
        return super.mouseScrolled(mx, my, sx, sy);
    }

    private void pageSound() {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0f));
    }
}
