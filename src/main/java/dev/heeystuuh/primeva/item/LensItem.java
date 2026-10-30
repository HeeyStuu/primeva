package dev.heeystuuh.primeva.item;

import dev.heeystuuh.primeva.Essence;
import dev.heeystuuh.primeva.Miasma;
import dev.heeystuuh.primeva.ScanTarget;
import dev.heeystuuh.primeva.Scanner;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Lente da Percepção: segure o botão direito mirando em algo (ou com um item na outra mão).
 * Runas fluem do alvo até você; ao completar, as essências são absorvidas.
 * Agachado, sem alvo: mostra o miasma do lugar.
 */
public class LensItem extends Item {
    public static final int SCAN_TIME = 30;
    /** Alvo atual e o tick em que começou a ser focado, por jogador (servidor). */
    private static final Map<UUID, Map.Entry<String, Integer>> FOCUS = new HashMap<>();

    public LensItem(Properties props) {
        super(props);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.SPYGLASS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack lens = player.getItemInHand(hand);
        if (player.isShiftKeyDown() && player.getOffhandItem().isEmpty()) {
            if (player instanceof ServerPlayer sp) sp.displayClientMessage(Miasma.describe(Miasma.get(level, sp.blockPosition())), true);
            return InteractionResultHolder.success(lens);
        }
        player.startUsingItem(hand);
        if (!level.isClientSide) FOCUS.remove(player.getUUID());
        level.playSound(null, player.blockPosition(), SoundEvents.SPYGLASS_USE, SoundSource.PLAYERS, 1f, 1.3f);
        return InteractionResultHolder.consume(lens);
    }

    /** Progresso do foco (0..SCAN_TIME), calculado igual no cliente e no servidor. */
    public static int progress(Map<UUID, Map.Entry<String, Integer>> focus, Player p, String key, int used) {
        Map.Entry<String, Integer> f = focus.get(p.getUUID());
        if (f == null || !f.getKey().equals(key)) {
            focus.put(p.getUUID(), Map.entry(key, used));
            return 0;
        }
        return used - f.getValue();
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remaining) {
        if (!(entity instanceof ServerPlayer player) || !(level instanceof ServerLevel sl)) return;
        int used = getUseDuration(stack, entity) - remaining;
        ScanTarget t = ScanTarget.find(player);
        if (t == null) {
            FOCUS.remove(player.getUUID());
            return;
        }
        int prog = progress(FOCUS, player, t.key(), used);
        Vec3 eye = player.getEyePosition();
        Essence dom = t.dominant();

        // runas fluindo do alvo para os olhos
        Vec3 d = t.pos().subtract(eye);
        for (int i = 0; i < 3; i++) {
            sl.sendParticles(player, ParticleTypes.ENCHANT, true, eye.x, eye.y - 0.2, eye.z, 0,
                    d.x + (sl.random.nextDouble() - 0.5) * 0.8, d.y + (sl.random.nextDouble() - 0.5) * 0.8, d.z + (sl.random.nextDouble() - 0.5) * 0.8, 1.0);
        }
        // anel de essência girando em volta do alvo
        if (dom != null) {
            double a = used * 0.5;
            for (int k = 0; k < 3; k++) {
                double ang = a + k * Math.PI * 2 / 3;
                Essence e = t.essences().keySet().stream().skip(k % Math.max(1, t.essences().size())).findFirst().orElse(dom);
                sl.sendParticles(new DustParticleOptions(e.color(), 0.9f), t.pos().x + Math.cos(ang) * 0.6, t.pos().y + Math.sin(used * 0.2) * 0.2,
                        t.pos().z + Math.sin(ang) * 0.6, 1, 0, 0, 0, 0);
            }
        }
        if (prog % 5 == 0) {
            sl.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.5f, 0.6f + prog / (float) SCAN_TIME);
        }
        if (prog >= SCAN_TIME) {
            FOCUS.remove(player.getUUID());
            Scanner.scan(player, t.key(), t.name(), t.essences());
            if (!t.essences().isEmpty()) burst(sl, player, t);
            player.stopUsingItem();
            player.getCooldowns().addCooldown(this, 10);
        }
    }

    private static void burst(ServerLevel sl, ServerPlayer player, ScanTarget t) {
        Vec3 c = player.position().add(0, 1.0, 0);
        int k = 0;
        for (Essence e : t.essences().keySet()) {
            for (int i = 0; i < 16; i++) {
                double ang = (i / 16.0) * Math.PI * 2 + k;
                sl.sendParticles(new DustParticleOptions(e.color(), 1.3f), c.x + Math.cos(ang) * 1.1, c.y + (i % 4) * 0.25 - 0.3, c.z + Math.sin(ang) * 1.1, 1, 0, 0, 0, 0);
            }
            k++;
        }
        sl.sendParticles(ParticleTypes.END_ROD, t.pos().x, t.pos().y, t.pos().z, 14, 0.2, 0.2, 0.2, 0.08);
        sl.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_CLUSTER_BREAK, SoundSource.PLAYERS, 0.8f, 1.4f);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (!level.isClientSide) FOCUS.remove(entity.getUUID());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.primeva.lens.1").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.primeva.lens.2").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.primeva.lens.3").withStyle(ChatFormatting.GRAY));
    }
}
