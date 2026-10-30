package dev.heeystuuh.primeva.item;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;

/** Espada de Aço do Vazio: enfraquece e cega por um instante quem ela corta. */
public class VoidsteelSwordItem extends SwordItem {
    public VoidsteelSwordItem(Tier tier, Properties props) {
        super(tier, props);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 80, 1), attacker);
        target.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 40, 0), attacker);
        return super.hurtEnemy(stack, target, attacker);
    }
}
