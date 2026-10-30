package dev.heeystuuh.primeva.client;

import dev.heeystuuh.primeva.*;
import dev.heeystuuh.primeva.network.ResearchPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.*;

/**
 * Mesa de Pesquisa: ligue todas as essências-âncora colocando essências compatíveis nas casas vizinhas.
 * Clique esquerdo coloca a essência escolhida, clique direito remove. Cada essência colocada custa percepção.
 */
public class ResearchPuzzleScreen extends Screen {
    private static final ResourceLocation SHEET = PrimevaMod.id("textures/gui/grimoire.png");
    private static final ResourceLocation ESS = PrimevaMod.id("textures/gui/essences.png");
    private static final int W = 380, H = 240, SIZE = 12;
    private static final int INK = 0x3B2412, INK_SOFT = 0x6B4A2A, PURPLE = 0x5A2A8A, GREEN = 0x2E6B2A, RED = 0x9A2A1A;

    private final Screen parent;
    private final Research research;
    private final ResearchPuzzle.Board board;
    private final Map<Integer, Essence> placed = new HashMap<>();
    private Essence selected = Essence.EMBER;
    private int left, top;
    private List<Component> hoverText;

    public ResearchPuzzleScreen(Screen parent, Research research) {
        super(research.title());
        this.parent = parent;
        this.research = research;
        this.board = research.board(Minecraft.getInstance().player.getUUID());
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

    private void sheet(GuiGraphics g, int x, int y, int w, int h, int u, int v, int uw, int vh) {
        g.blit(SHEET, x, y, w, h, u, v, uw, vh, 1024, 512);
    }

    private int[] cellPos(int idx) {
        int[] c = ResearchPuzzle.CELLS.get(idx);
        double x = SIZE * (Math.sqrt(3) * c[0] + Math.sqrt(3) / 2 * c[1]);
        double y = SIZE * 1.5 * c[1];
        return new int[]{left + 18 + 84 + (int) Math.round(x), top + 14 + 112 + (int) Math.round(y)};
    }

    private Map<Integer, Essence> all() {
        Map<Integer, Essence> m = new HashMap<>(placed);
        m.putAll(board.anchors());
        return m;
    }

    private Map<Essence, Integer> cost() {
        return research.totalCost(placed);
    }

    private boolean affordable() {
        Knowledge k = ClientKnowledge.get();
        for (Map.Entry<Essence, Integer> e : cost().entrySet()) if (k.get(e.getKey()) < e.getValue()) return false;
        return true;
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        renderBackground(g, mx, my, pt);
        hoverText = null;
        sheet(g, left, top, W, H, 0, 0, 760, 480);
        Knowledge k = ClientKnowledge.get();
        Map<Integer, Essence> all = all();
        Set<Integer> linked = ResearchPuzzle.connectedFrom(all, board.anchors().keySet().iterator().next());
        boolean solved = ResearchPuzzle.solved(board, placed);

        // título
        Component t = Component.translatable("gui.primeva.puzzle_title", research.title());
        g.drawString(font, t, left + 18 + 84 - font.width(t) / 2, top + 16, PURPLE, false);

        // ligações
        for (Map.Entry<Integer, Essence> a : all.entrySet()) {
            int[] pa = cellPos(a.getKey());
            for (int n : ResearchPuzzle.neighbors(a.getKey())) {
                if (n < a.getKey() || !all.containsKey(n) || !ResearchPuzzle.compatible(a.getValue(), all.get(n))) continue;
                int[] pb = cellPos(n);
                int col = linked.contains(n) && linked.contains(a.getKey()) ? 0xFFB8862A : 0xFF9C8A6A;
                for (int s = 1; s < 5; s++) {
                    int lx = pa[0] + (pb[0] - pa[0]) * s / 5, ly = pa[1] + (pb[1] - pa[1]) * s / 5;
                    g.fill(lx - 1, ly - 1, lx + 1, ly + 1, col);
                }
            }
        }
        // casas
        for (int i = 0; i < ResearchPuzzle.CELLS.size(); i++) {
            int[] p = cellPos(i);
            boolean isVoid = board.voids().contains(i);
            if (isVoid) {
                g.fill(p[0] - 6, p[1] - 6, p[0] + 6, p[1] + 6, 0x66302010);
                continue;
            }
            sheet(g, p[0] - 9, p[1] - 9, 18, 18, 770, 200, 40, 40);
            Essence e = all.get(i);
            if (e != null) {
                boolean anchor = board.anchors().containsKey(i);
                if (anchor) g.fill(p[0] - 8, p[1] - 8, p[0] + 8, p[1] + 8, linked.contains(i) ? 0x88FFD860 : 0x88A02020);
                g.blit(ESS, p[0] - 6, p[1] - 6, 12, 12, e.ordinal() * 32, 0, 32, 32, 192, 32);
            }
            if (Math.abs(mx - p[0]) <= 8 && Math.abs(my - p[1]) <= 8) {
                g.fill(p[0] - 8, p[1] - 8, p[0] + 8, p[1] + 8, 0x30FFFFFF);
                if (e != null) hoverText = List.of(e.displayName());
            }
        }

        // página direita
        int rx = left + 196, ry = top + 14, w = 168;
        int cy = ry;
        for (FormattedCharSequence line : font.split(Component.translatable("gui.primeva.puzzle_help"), w - 4)) {
            g.drawString(font, line, rx + 2, cy, INK, false);
            cy += 10;
        }
        cy += 4;
        // ciclo
        g.drawString(font, Component.translatable("gui.primeva.puzzle_cycle"), rx + 2, cy, INK_SOFT, false);
        cy += 11;
        int cx = rx + 4;
        for (int i = 0; i < ResearchPuzzle.CYCLE.length; i++) {
            g.blit(ESS, cx, cy, 11, 11, ResearchPuzzle.CYCLE[i].ordinal() * 32, 0, 32, 32, 192, 32);
            if (i < ResearchPuzzle.CYCLE.length - 1) g.drawString(font, "-", cx + 13, cy + 2, INK_SOFT, false);
            cx += 22;
        }
        g.drawString(font, "↺", cx - 6, cy + 2, INK_SOFT, false);
        cy += 18;
        // paleta
        g.drawString(font, Component.translatable("gui.primeva.puzzle_palette"), rx + 2, cy, INK_SOFT, false);
        cy += 11;
        for (Essence e : Essence.values()) {
            int px = rx + 4 + (e.ordinal() % 3) * 54, py = cy + (e.ordinal() / 3) * 16;
            if (e == selected) g.fill(px - 2, py - 2, px + 50, py + 13, 0x55B8862A);
            g.blit(ESS, px, py, 12, 12, e.ordinal() * 32, 0, 32, 32, 192, 32);
            g.drawString(font, String.valueOf(k.get(e)), px + 15, py + 2, INK, false);
            if (mx >= px - 2 && mx < px + 50 && my >= py - 2 && my < py + 13) hoverText = List.of(e.displayName(), Component.translatable("gui.primeva.puzzle_pick"));
        }
        cy += 36;
        // custo
        g.drawString(font, Component.translatable("gui.primeva.cost"), rx + 2, cy + 2, INK_SOFT, false);
        int ex = rx + 2 + font.width(Component.translatable("gui.primeva.cost")) + 2;
        for (Map.Entry<Essence, Integer> e : cost().entrySet()) {
            String txt = String.valueOf(e.getValue());
            int ew = 13 + font.width(txt) + 5;
            if (ex + ew > rx + w) {
                ex = rx + 4;
                cy += 12;
            }
            g.blit(ESS, ex, cy, 11, 11, e.getKey().ordinal() * 32, 0, 32, 32, 192, 32);
            g.drawString(font, txt, ex + 13, cy + 2, k.get(e.getKey()) >= e.getValue() ? GREEN : RED, false);
            ex += ew;
        }
        cy += 16;
        boolean scroll = Research.hasScroll(Minecraft.getInstance().player);
        Component state = solved ? Component.translatable("gui.primeva.puzzle_solved") : Component.translatable("gui.primeva.puzzle_unsolved");
        g.drawString(font, state, rx + 2, cy, solved ? GREEN : RED, false);
        cy += 11;
        if (!scroll) {
            g.drawString(font, Component.translatable("msg.primeva.need_scroll"), rx + 2, cy, RED, false);
            cy += 11;
        }
        // botões
        boolean can = solved && affordable() && scroll;
        int bx = rx + w / 2 - 45, by = top + H - 40;
        boolean hover = can && mx >= bx && mx < bx + 90 && my >= by && my < by + 20;
        sheet(g, bx, by, 90, 20, 770, can ? (hover ? 75 : 30) : 120, 180, 40);
        Component label = Component.translatable("gui.primeva.research");
        g.drawString(font, label, bx + 45 - font.width(label) / 2, by + 6, can ? 0xFFF4D8 : 0xBDB4A8, true);
        Component back = Component.translatable("gui.primeva.back");
        int backX = left + 22, backY = top + H - 22;
        g.drawString(font, back, backX, backY, mx >= backX && mx < backX + font.width(back) && my >= backY && my < backY + 9 ? PURPLE : INK_SOFT, false);
        Component clear = Component.translatable("gui.primeva.puzzle_clear");
        int clrX = left + 18 + 168 - font.width(clear) - 6;
        g.drawString(font, clear, clrX, backY, mx >= clrX && mx < clrX + font.width(clear) && my >= backY && my < backY + 9 ? PURPLE : INK_SOFT, false);

        if (hoverText != null) g.renderComponentTooltip(font, hoverText, mx, my);
    }

    private void click(float pitch) {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.AMETHYST_BLOCK_HIT, pitch));
    }

    @Override
    public boolean mouseClicked(double mxd, double myd, int button) {
        int mx = (int) mxd, my = (int) myd;
        // casas
        for (int i = 0; i < ResearchPuzzle.CELLS.size(); i++) {
            int[] p = cellPos(i);
            if (Math.abs(mx - p[0]) > 8 || Math.abs(my - p[1]) > 8) continue;
            if (board.voids().contains(i) || board.anchors().containsKey(i)) return true;
            if (button == 1) {
                if (placed.remove(i) != null) click(0.7f);
            } else {
                placed.put(i, selected);
                click(1.3f);
            }
            return true;
        }
        // paleta
        int rx = left + 196;
        for (Essence e : Essence.values()) {
            int px = rx + 4 + (e.ordinal() % 3) * 54;
            int py = paletteY() + (e.ordinal() / 3) * 16;
            if (mx >= px - 2 && mx < px + 50 && my >= py - 2 && my < py + 13) {
                selected = e;
                click(1.0f);
                return true;
            }
        }
        // concluir
        int bx = rx + 84 - 45, by = top + H - 40;
        if (mx >= bx && mx < bx + 90 && my >= by && my < by + 20 && ResearchPuzzle.solved(board, placed) && affordable()
                && Research.hasScroll(Minecraft.getInstance().player)) {
            PacketDistributor.sendToServer(new ResearchPayload(research.id(), ResearchPuzzle.encode(placed)));
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.ENCHANTMENT_TABLE_USE, 1.0f));
            Minecraft.getInstance().setScreen(parent);
            return true;
        }
        int backY = top + H - 22;
        if (my >= backY && my < backY + 9) {
            if (mx >= left + 22 && mx < left + 22 + font.width(Component.translatable("gui.primeva.back"))) {
                Minecraft.getInstance().setScreen(parent);
                return true;
            }
            Component clear = Component.translatable("gui.primeva.puzzle_clear");
            int clrX = left + 18 + 168 - font.width(clear) - 6;
            if (mx >= clrX && mx < clrX + font.width(clear)) {
                placed.clear();
                click(0.5f);
                return true;
            }
        }
        return super.mouseClicked(mxd, myd, button);
    }

    /** Mesma conta do desenho para achar a altura da paleta. */
    private int paletteY() {
        int cy = top + 14;
        cy += font.split(Component.translatable("gui.primeva.puzzle_help"), 164).size() * 10;
        cy += 4 + 11 + 18 + 11;
        return cy;
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(parent);
    }
}
