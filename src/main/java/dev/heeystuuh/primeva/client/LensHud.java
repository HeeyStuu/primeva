package dev.heeystuuh.primeva.client;

import dev.heeystuuh.primeva.ClientKnowledge;
import dev.heeystuuh.primeva.Essence;
import dev.heeystuuh.primeva.PrimevaMod;
import dev.heeystuuh.primeva.ScanTarget;
import dev.heeystuuh.primeva.ScanToast;
import dev.heeystuuh.primeva.item.LensItem;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ComputeFovModifierEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** HUD da Lente: moldura mística, alvo, progresso, essências conhecidas e o painel de absorção. */
public final class LensHud {
    private static final ResourceLocation OVERLAY = PrimevaMod.id("textures/gui/lens_overlay.png");
    private static final ResourceLocation ESS = PrimevaMod.id("textures/gui/essences.png");
    private static final Map<UUID, Map.Entry<String, Integer>> FOCUS = new HashMap<>();

    private static boolean scanning(Player p) {
        return p != null && p.isUsingItem() && p.getUseItem().getItem() instanceof LensItem;
    }

    @EventBusSubscriber(modid = PrimevaMod.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static final class Layers {
        @SubscribeEvent
        public static void register(RegisterGuiLayersEvent event) {
            event.registerAbove(VanillaGuiLayers.CROSSHAIR, PrimevaMod.id("lens"), LensHud::render);
        }
    }

    @EventBusSubscriber(modid = PrimevaMod.MODID, value = Dist.CLIENT)
    public static final class Fov {
        @SubscribeEvent
        public static void fov(ComputeFovModifierEvent event) {
            if (scanning(event.getPlayer())) {
                float t = Math.min(1f, event.getPlayer().getTicksUsingItem() / 10f);
                event.setNewFovModifier(event.getNewFovModifier() * Mth.lerp(t, 1f, 0.55f));
            }
        }
    }

    private static void icon(GuiGraphics g, Essence e, int x, int y, int size) {
        g.blit(ESS, x, y, size, size, e.ordinal() * 32, 0, 32, 32, 192, 32);
    }

    public static void render(GuiGraphics g, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        Player p = mc.player;
        if (p == null || mc.options.hideGui) return;
        int w = g.guiWidth(), h = g.guiHeight();
        Font font = mc.font;

        if (scanning(p)) {
            float fade = Math.min(1f, p.getTicksUsingItem() / 8f);
            com.mojang.blaze3d.systems.RenderSystem.enableBlend();
            g.setColor(1f, 1f, 1f, fade);
            int size = Math.max(w, h);
            g.blit(OVERLAY, (w - size) / 2, (h - size) / 2, size, size, 0, 0, 256, 256, 256, 256);
            g.setColor(1f, 1f, 1f, 1f);

            ScanTarget t = ScanTarget.find(p);
            int cx = w / 2, cy = h / 2;
            if (t != null) {
                int prog = LensItem.progress(FOCUS, p, t.key(), p.getTicksUsingItem());
                float f = Math.min(1f, prog / (float) LensItem.SCAN_TIME);
                // anel de progresso (pontos ao redor da mira)
                int dots = 32;
                for (int i = 0; i < dots; i++) {
                    double a = -Math.PI / 2 + i * Math.PI * 2 / dots;
                    int x = cx + (int) Math.round(Math.cos(a) * 14), y = cy + (int) Math.round(Math.sin(a) * 14);
                    int col = i < f * dots ? 0xFFD8A0FF : 0x55FFFFFF;
                    g.fill(x - 1, y - 1, x + 1, y + 1, col);
                }
                Component name = t.name();
                g.drawString(font, name, cx - font.width(name) / 2, cy + 22, 0xFFF0E0FF, true);
                boolean known = ClientKnowledge.get().scanned.contains(t.key());
                if (t.essences().isEmpty()) {
                    Component nothing = Component.translatable("gui.primeva.lens_nothing");
                    g.drawString(font, nothing, cx - font.width(nothing) / 2, cy + 33, 0xFF9A90A8, true);
                } else if (known) {
                    int total = 0;
                    for (Map.Entry<Essence, Integer> e : t.essences().entrySet()) total += 14 + font.width(String.valueOf(e.getValue())) + 4;
                    int x = cx - total / 2;
                    for (Map.Entry<Essence, Integer> e : t.essences().entrySet()) {
                        icon(g, e.getKey(), x, cy + 33, 11);
                        g.drawString(font, String.valueOf(e.getValue()), x + 13, cy + 35, 0xFFFFFFFF, true);
                        x += 14 + font.width(String.valueOf(e.getValue())) + 4;
                    }
                } else {
                    Component unk = Component.translatable("gui.primeva.lens_unknown");
                    g.drawString(font, unk, cx - font.width(unk) / 2, cy + 33, 0xFFC890FF, true);
                }
            } else {
                FOCUS.remove(p.getUUID());
            }
        } else {
            FOCUS.remove(p.getUUID());
        }

        // painel de absorção (3,5 s, com entrada deslizando)
        long age = System.currentTimeMillis() - ScanToast.shownAt;
        if (age < 3500 && ScanToast.name != null) {
            float in = Math.min(1f, age / 250f), out = Math.min(1f, (3500 - age) / 400f);
            float a = Math.min(in, out);
            int pw = 150, ph = 40;
            int x = w / 2 - pw / 2, y = (int) (8 - (1 - in) * 30);
            int alpha = (int) (a * 220) << 24;
            g.fill(x, y, x + pw, y + ph, alpha | 0x1A0A28);
            g.fill(x, y, x + pw, y + 1, ((int) (a * 255) << 24) | 0xE0B850);
            g.fill(x, y + ph - 1, x + pw, y + ph, ((int) (a * 255) << 24) | 0xE0B850);
            int textA = Math.max(4, (int) (a * 255)) << 24;
            Component title = ScanToast.isNew ? Component.translatable("gui.primeva.new_perception") : Component.translatable("gui.primeva.known_perception");
            g.drawString(font, title, x + pw / 2 - font.width(title) / 2, y + 4, textA | (ScanToast.isNew ? 0xFFD860 : 0xB0A8C0), false);
            g.drawString(font, ScanToast.name, x + pw / 2 - font.width(ScanToast.name) / 2, y + 14, textA | 0xFFFFFF, false);
            int total = 0;
            for (Map.Entry<Essence, Integer> e : ScanToast.essences.entrySet()) total += 14 + font.width((ScanToast.isNew ? "+" : "") + e.getValue()) + 5;
            int ex = x + pw / 2 - total / 2;
            for (Map.Entry<Essence, Integer> e : ScanToast.essences.entrySet()) {
                String txt = (ScanToast.isNew ? "+" : "") + e.getValue();
                g.setColor(1f, 1f, 1f, a);
                icon(g, e.getKey(), ex, y + 25, 11);
                g.setColor(1f, 1f, 1f, 1f);
                g.drawString(font, txt, ex + 13, y + 27, textA | (e.getKey().rgb & 0xFFFFFF), false);
                ex += 14 + font.width(txt) + 5;
            }
        }
    }

    private LensHud() {}
}
