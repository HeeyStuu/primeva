package dev.heeystuuh.primeva.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/** Ceifadora do Eclipse: corte em área que drena vida e definha todos ao redor do alvo. */
public class EclipseReaperItem extends SwordItem {
    public EclipseReaperItem(Tier tier, Properties props) {
        super(tier, props);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker.level() instanceof ServerLevel sl) {
            float healed = 3.0f;
            for (LivingEntity e : sl.getEntitiesOfClass(LivingEntity.class, target.getBoundingBox().inflate(3.5),
                    e -> e != attacker && e != target && e.isAlive() && !(e instanceof Player))) {
                e.hurt(sl.damageSources().indirectMagic(attacker, attacker), 8.0f);
                e.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 1), attacker);
                healed += 1.0f;
            }
            target.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 2), attacker);
            attacker.heal(Math.min(healed, 8.0f));
            sl.sendParticles(ParticleTypes.SCULK_SOUL, target.getX(), target.getY() + 1, target.getZ(), 20, 1.5, 0.5, 1.5, 0.02);
            sl.sendParticles(ParticleTypes.SWEEP_ATTACK, target.getX(), target.getY() + 1, target.getZ(), 3, 1, 0.2, 1, 0);
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.primeva.eclipse_reaper.1").withStyle(ChatFormatting.DARK_PURPLE));
        tooltip.add(Component.translatable("tooltip.primeva.eclipse_reaper.2").withStyle(ChatFormatting.GRAY));
    }
}
