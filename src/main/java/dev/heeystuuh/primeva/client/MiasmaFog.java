package dev.heeystuuh.primeva.client;

import dev.heeystuuh.primeva.ClientMiasma;
import dev.heeystuuh.primeva.PrimevaMod;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;

/** Névoa arroxeada que fica mais densa conforme o miasma do lugar, e some aos poucos com a purificação. */
@EventBusSubscriber(modid = PrimevaMod.MODID, value = Dist.CLIENT)
public final class MiasmaFog {
    private static float intensity;

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post e) {
        float target = Mth.clamp((ClientMiasma.level - 20) / 60f, 0f, 1f);
        intensity += (target - intensity) * 0.02f;
    }

    @SubscribeEvent
    public static void color(ViewportEvent.ComputeFogColor e) {
        if (intensity < 0.01f) return;
        float k = intensity * 0.6f;
        e.setRed(Mth.lerp(k, e.getRed(), 0.28f));
        e.setGreen(Mth.lerp(k, e.getGreen(), 0.12f));
        e.setBlue(Mth.lerp(k, e.getBlue(), 0.36f));
    }

    @SubscribeEvent
    public static void fog(ViewportEvent.RenderFog e) {
        if (intensity < 0.01f) return;
        float far = Mth.lerp(intensity, e.getFarPlaneDistance(), Math.min(e.getFarPlaneDistance(), 48f));
        e.setFarPlaneDistance(far);
        e.setNearPlaneDistance(Math.min(e.getNearPlaneDistance(), far * 0.3f));
        e.setCanceled(true);
    }

    private MiasmaFog() {}
}
