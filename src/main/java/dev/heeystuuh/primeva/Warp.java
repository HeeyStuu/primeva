package dev.heeystuuh.primeva;

import dev.heeystuuh.primeva.network.Network;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Mente Corrompida. Conhecimento proibido deixa marcas:
 * - permanente: pesquisas sombrias (só o Tônico da Lucidez tira um pouco);
 * - temporária: colapsos de ritual, Ritos Carmesins, ficar em miasma pesado (some sozinha com o tempo).
 * Quanto maior, mais frequentes e piores os eventos: sussurros, visões, espectros, fome, emboscadas e pesadelos.
 */
public final class Warp {
    public static final Map<String, Integer> RESEARCH_WARP = Map.ofEntries(
            Map.entry("miasma", 1), Map.entry("transmutation", 1), Map.entry("umbral_blade", 2), Map.entry("wraiths", 2),
            Map.entry("advanced_foci", 2), Map.entry("voidsteel", 3), Map.entry("crimson_cult", 3), Map.entry("catalyst", 3),
            Map.entry("herald", 4), Map.entry("voidsteel_armor", 2), Map.entry("eclipse_reaper", 5), Map.entry("primeval_heart", 6));

    public static int total(Knowledge k) {
        return k.warp + k.tempWarp;
    }

    public static void addPerm(ServerPlayer p, int n) {
        if (n <= 0) return;
        Knowledge k = p.getData(ModAttachments.KNOWLEDGE);
        k.warp += n;
        p.displayClientMessage(Component.translatable("msg.primeva.warp_perm", n).withStyle(ChatFormatting.DARK_PURPLE), false);
        p.level().playSound(null, p.blockPosition(), SoundEvents.SCULK_CLICKING, SoundSource.PLAYERS, 1f, 0.5f);
        Network.sync(p);
    }

    public static void addTemp(ServerPlayer p, int n) {
        if (n <= 0) return;
        Knowledge k = p.getData(ModAttachments.KNOWLEDGE);
        k.tempWarp = Math.min(100, k.tempWarp + n);
        p.displayClientMessage(Component.translatable("msg.primeva.warp_temp", n).withStyle(ChatFormatting.LIGHT_PURPLE), true);
        Network.sync(p);
    }

    /** Sabão da Clareza: limpa só a marca temporária. */
    public static void cleanseTemp(ServerPlayer p) {
        Knowledge k = p.getData(ModAttachments.KNOWLEDGE);
        k.tempWarp = 0;
        p.displayClientMessage(Component.translatable("msg.primeva.warp_cleansed").withStyle(ChatFormatting.AQUA), true);
        Network.sync(p);
    }

    /** Tônico da Lucidez: limpa a marca temporária e 1 da permanente. */
    public static void cleanse(ServerPlayer p) {
        Knowledge k = p.getData(ModAttachments.KNOWLEDGE);
        k.tempWarp = 0;
        k.warp = Math.max(0, k.warp - 1);
        p.removeEffect(MobEffects.CONFUSION);
        p.removeEffect(MobEffects.DARKNESS);
        p.displayClientMessage(Component.translatable("msg.primeva.warp_cleansed").withStyle(ChatFormatting.AQUA), false);
        Network.sync(p);
    }

    @SubscribeEvent
    public static void tick(PlayerTickEvent.Post e) {
        if (!(e.getEntity() instanceof ServerPlayer p) || p.tickCount % 20 != 11 || p.isSpectator() || p.isCreative()) return;
        Knowledge k = p.getData(ModAttachments.KNOWLEDGE);
        var pd = p.getPersistentData();
        // a marca temporária some devagar (1 a cada 5 min)
        int t = pd.getInt("primeva_warp_decay") + 20;
        if (t >= 6000) {
            t = 0;
            if (k.tempWarp > 0) {
                k.tempWarp--;
                Network.sync(p);
            }
        }
        pd.putInt("primeva_warp_decay", t);
        // miasma pesado contamina a mente (1 por minuto)
        if (Miasma.get(p.level(), p.blockPosition()) >= 60 && !CommonEvents.miasmaImmune(p)) {
            int mt = pd.getInt("primeva_warp_miasma") + 20;
            if (mt >= 1200) {
                mt = 0;
                addTemp(p, 1);
            }
            pd.putInt("primeva_warp_miasma", mt);
        }
        int w = total(k);
        if (w < 3) return;
        int interval = Math.max(2400, 14000 - w * 250);
        if (p.getRandom().nextInt(interval / 20) != 0) return;
        event(p, w);
    }

    private static void event(ServerPlayer p, int w) {
        ServerLevel sl = p.serverLevel();
        RandomSource r = p.getRandom();
        List<Integer> options = new ArrayList<>();
        options.add(0);
        options.add(0);
        if (w >= 8) options.add(1);
        if (w >= 12) options.add(2);
        if (w >= 18) options.add(3);
        if (w >= 25) options.add(4);
        if (w >= 35) options.add(5);
        int ev = options.get(r.nextInt(options.size()));
        BlockPos pos = p.blockPosition();
        Discovery.grant(p, "do:warp");
        switch (ev) {
            case 0 -> { // sussurros
                sl.playSound(null, pos, SoundEvents.AMBIENT_CAVE.value(), SoundSource.AMBIENT, 1f, 0.6f + r.nextFloat() * 0.3f);
                p.displayClientMessage(Component.translatable("warp.primeva.whisper." + r.nextInt(6)).withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC), false);
            }
            case 1 -> { // visão: dói, mas ensina
                p.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 160, 0));
                p.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 60, 0));
                Essence e = Essence.values()[r.nextInt(Essence.values().length)];
                p.getData(ModAttachments.KNOWLEDGE).add(e, 3 + w / 10);
                Network.sync(p);
                p.displayClientMessage(Component.translatable("warp.primeva.vision", e.displayName()).withStyle(ChatFormatting.LIGHT_PURPLE), false);
            }
            case 2 -> { // espectro
                if (sl.getDifficulty() != Difficulty.PEACEFUL) Miasma.spawnWraith(sl, pos, r);
                p.displayClientMessage(Component.translatable("warp.primeva.phantom").withStyle(ChatFormatting.DARK_PURPLE), false);
            }
            case 3 -> { // fome que não passa
                p.addEffect(new MobEffectInstance(MobEffects.HUNGER, 600, 1));
                p.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 600, 0));
                p.displayClientMessage(Component.translatable("warp.primeva.hunger").withStyle(ChatFormatting.DARK_PURPLE), false);
            }
            case 4 -> { // emboscada da seita
                if (sl.getDifficulty() != Difficulty.PEACEFUL) {
                    for (int i = 0; i < 2; i++) {
                        int x = pos.getX() + r.nextInt(17) - 8, z = pos.getZ() + r.nextInt(17) - 8;
                        BlockPos at = sl.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, new BlockPos(x, 0, z));
                        if (Math.abs(at.getY() - pos.getY()) > 6) at = pos.offset(r.nextInt(5) - 2, 0, r.nextInt(5) - 2);
                        var c = ModEntities.CRIMSON_CULTIST.get().create(sl, null, at, MobSpawnType.EVENT, true, false);
                        if (c != null) {
                            c.setTarget(p);
                            sl.addFreshEntity(c);
                        }
                    }
                }
                p.displayClientMessage(Component.translatable("warp.primeva.ambush").withStyle(ChatFormatting.DARK_RED), false);
            }
            default -> { // pesadelo
                p.addEffect(new MobEffectInstance(MobEffects.WITHER, 120, 0));
                p.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 0));
                sl.playSound(null, pos, SoundEvents.WARDEN_HEARTBEAT, SoundSource.HOSTILE, 1.5f, 0.7f);
                p.displayClientMessage(Component.translatable("warp.primeva.nightmare").withStyle(ChatFormatting.DARK_RED), false);
            }
        }
    }

    private Warp() {}
}
