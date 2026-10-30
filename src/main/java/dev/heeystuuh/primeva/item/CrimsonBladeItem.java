package dev.heeystuuh.primeva.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/** Lâmina Carmesim: faz o alvo definhar e sangrar fraqueza. */
public class CrimsonBladeItem extends SwordItem {
    public CrimsonBladeItem(Tier tier, Properties props) {
        super(tier, props);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        target.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, 0), attacker);
        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 0), attacker);
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public void appendHoverText(ItemStack s, TooltipContext c, List<Component> t, TooltipFlag f) {
        t.add(Component.translatable("tooltip.primeva.crimson_blade").withStyle(ChatFormatting.DARK_RED));
    }
}
