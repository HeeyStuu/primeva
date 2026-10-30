package dev.heeystuuh.primeva;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.Map;

/** As seis essências primordiais. */
public enum Essence {
    EMBER("ember", ChatFormatting.RED, 0xFF6A1E),
    TIDE("tide", ChatFormatting.BLUE, 0x3AA0FF),
    TERRA("terra", ChatFormatting.GREEN, 0x6CC040),
    GALE("gale", ChatFormatting.AQUA, 0xC8F6F2),
    LUMEN("lumen", ChatFormatting.YELLOW, 0xFFE060),
    UMBRA("umbra", ChatFormatting.DARK_PURPLE, 0xB060FF);

    public final String id;
    public final ChatFormatting fmt;
    public final int rgb;

    Essence(String id, ChatFormatting fmt, int rgb) {
        this.id = id;
        this.fmt = fmt;
        this.rgb = rgb;
    }

    public org.joml.Vector3f color() {
        return new org.joml.Vector3f(((rgb >> 16) & 255) / 255f, ((rgb >> 8) & 255) / 255f, (rgb & 255) / 255f);
    }

    public static Essence byId(String id) {
        for (Essence e : values()) if (e.id.equals(id)) return e;
        return null;
    }

    public MutableComponent displayName() {
        return Component.translatable("essence.primeva." + id).withStyle(fmt);
    }

    /** "Terra 3  Brasa 1" colorido, na ordem do enum. */
    public static MutableComponent line(Map<Essence, Integer> map, String prefix) {
        MutableComponent out = Component.empty();
        boolean first = true;
        for (Essence e : values()) {
            Integer n = map.get(e);
            if (n == null || n <= 0) continue;
            if (!first) out.append(Component.literal("  "));
            out.append(Component.literal(prefix + n + " ").withStyle(e.fmt)).append(e.displayName());
            first = false;
        }
        return out;
    }
}
