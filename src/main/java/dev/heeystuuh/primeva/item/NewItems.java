package dev.heeystuuh.primeva.item;

import dev.heeystuuh.primeva.*;
import dev.heeystuuh.primeva.network.Network;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;

import javax.annotation.Nullable;
import java.util.List;

/** Itens do pacote de novidades: flechas, arco, espelho da mente, sabão, sextante e cérebro. */
public final class NewItems {

    /** Flecha de Essência: cada uma carrega um efeito da sua essência. */
    public static class EssenceArrow extends ArrowItem {
        public final Essence essence;

        public EssenceArrow(Essence e, Properties p) {
            super(p);
            this.essence = e;
        }

        @Override
        public AbstractArrow createArrow(Level level, ItemStack ammo, LivingEntity shooter, @Nullable ItemStack weapon) {
            Arrow a = new Arrow(level, shooter, ammo.copyWithCount(1), weapon);
            switch (essence) {
                case EMBER -> a.igniteForSeconds(100);
                case TIDE -> a.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 120, 2));
                case GALE -> a.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 40, 1));
                case TERRA -> {
                    a.setBaseDamage(a.getBaseDamage() + 2.0);
                    a.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 160, 0));
                }
                case LUMEN -> a.addEffect(new MobEffectInstance(MobEffects.GLOWING, 300, 0));
                case UMBRA -> a.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 1));
            }
            return a;
        }

        @Override
        public void appendHoverText(ItemStack s, TooltipContext c, List<Component> t, TooltipFlag f) {
            t.add(Component.translatable("tooltip.primeva.arrow_" + essence.id).withStyle(essence.fmt));
        }
    }

    /** Arco Ancião: flechas 50% mais fortes e 30% mais rápidas. */
    public static class ElderBow extends BowItem {
        public ElderBow(Properties p) {
            super(p.durability(768));
        }

        @Override
        protected Projectile createProjectile(Level level, LivingEntity shooter, ItemStack weapon, ItemStack ammo, boolean crit) {
            Projectile pr = super.createProjectile(level, shooter, weapon, ammo, crit);
            if (pr instanceof AbstractArrow a) {
                a.setBaseDamage(a.getBaseDamage() * 1.5 + 1.0);
                a.setDeltaMovement(a.getDeltaMovement().scale(1.3));
            }
            return pr;
        }

        @Override
        public int getEnchantmentValue() {
            return 18;
        }

        @Override
        public void appendHoverText(ItemStack s, TooltipContext c, List<Component> t, TooltipFlag f) {
            t.add(Component.translatable("tooltip.primeva.elder_bow").withStyle(ChatFormatting.GRAY));
        }
    }

    /** Espelho da Mente: mostra quanta corrupção mental você carrega e o que ela pode causar. */
    public static class MindMirror extends Item {
        public MindMirror(Properties p) {
            super(p.stacksTo(1));
        }

        @Override
        public InteractionResultHolder<ItemStack> use(Level level, Player p, InteractionHand hand) {
            ItemStack st = p.getItemInHand(hand);
            if (p instanceof ServerPlayer sp) {
                Knowledge k = sp.getData(ModAttachments.KNOWLEDGE);
                int w = Warp.total(k);
                sp.sendSystemMessage(Component.translatable("msg.primeva.mirror_mind", k.warp, k.tempWarp, w).withStyle(ChatFormatting.LIGHT_PURPLE));
                String stage = w >= 35 ? "5" : w >= 25 ? "4" : w >= 18 ? "3" : w >= 12 ? "2" : w >= 8 ? "1" : w >= 3 ? "0" : "none";
                sp.sendSystemMessage(Component.translatable("msg.primeva.mirror_stage." + stage).withStyle(ChatFormatting.GRAY));
                level.playSound(null, p.blockPosition(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 0.8f, 0.7f);
                sp.getCooldowns().addCooldown(this, 40);
            }
            return InteractionResultHolder.sidedSuccess(st, level.isClientSide);
        }

        @Override
        public void appendHoverText(ItemStack s, TooltipContext c, List<Component> t, TooltipFlag f) {
            t.add(Component.translatable("tooltip.primeva.mind_mirror").withStyle(ChatFormatting.GRAY));
        }
    }

    /** Sabão da Clareza: lava a marca temporária da mente (mais barato que o tônico). */
    public static class CleansingSoap extends Item {
        public CleansingSoap(Properties p) {
            super(p.stacksTo(16));
        }

        @Override
        public UseAnim getUseAnimation(ItemStack stack) {
            return UseAnim.BRUSH;
        }

        @Override
        public int getUseDuration(ItemStack stack, LivingEntity entity) {
            return 32;
        }

        @Override
        public InteractionResultHolder<ItemStack> use(Level level, Player p, InteractionHand hand) {
            return ItemUtils.startUsingInstantly(level, p, hand);
        }

        @Override
        public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity e) {
            if (e instanceof ServerPlayer sp) {
                Warp.cleanseTemp(sp);
                ((ServerLevel) level).sendParticles(ParticleTypes.BUBBLE_POP, sp.getX(), sp.getY() + 1, sp.getZ(), 30, 0.4, 0.6, 0.4, 0.02);
                level.playSound(null, sp.blockPosition(), SoundEvents.BUBBLE_COLUMN_UPWARDS_AMBIENT, SoundSource.PLAYERS, 1f, 1.2f);
                if (!sp.getAbilities().instabuild) stack.shrink(1);
            }
            return stack;
        }

        @Override
        public void appendHoverText(ItemStack s, TooltipContext c, List<Component> t, TooltipFlag f) {
            t.add(Component.translatable("tooltip.primeva.cleansing_soap").withStyle(ChatFormatting.GRAY));
        }
    }

    /** Sextante Astral: observe o céu aberto para ganhar percepção (dia: Lume/Brasa; noite: Umbra/Vendaval/Maré). */
    public static class AstralSextant extends Item {
        public AstralSextant(Properties p) {
            super(p.durability(64));
        }

        @Override
        public InteractionResultHolder<ItemStack> use(Level level, Player p, InteractionHand hand) {
            ItemStack st = p.getItemInHand(hand);
            if (!(p instanceof ServerPlayer sp)) return InteractionResultHolder.success(st);
            boolean sky = level.canSeeSky(p.blockPosition().above())
                    && p.getBlockY() + 1 >= level.getHeight(Heightmap.Types.MOTION_BLOCKING, p.getBlockX(), p.getBlockZ());
            if (!sky || !level.dimensionType().hasSkyLight() || level.dimensionType().hasCeiling()) {
                sp.displayClientMessage(Component.translatable("msg.primeva.sextant_sky").withStyle(ChatFormatting.RED), true);
                return InteractionResultHolder.fail(st);
            }
            Knowledge k = sp.getData(ModAttachments.KNOWLEDGE);
            boolean day = level.isDay();
            int mult = !day && level.getMoonPhase() == 0 ? 2 : 1;
            if (day) {
                k.add(Essence.LUMEN, 4 * mult);
                k.add(Essence.EMBER, 3 * mult);
                k.add(Essence.GALE, 2 * mult);
            } else {
                k.add(Essence.UMBRA, 4 * mult);
                k.add(Essence.GALE, 3 * mult);
                k.add(Essence.TIDE, 2 * mult);
            }
            if (level.isThundering()) k.add(Essence.GALE, 4);
            Network.sync(sp);
            sp.displayClientMessage(Component.translatable(day ? "msg.primeva.sextant_day" : mult > 1 ? "msg.primeva.sextant_fullmoon" : "msg.primeva.sextant_night")
                    .withStyle(day ? ChatFormatting.GOLD : ChatFormatting.DARK_AQUA), true);
            ((ServerLevel) level).sendParticles(ParticleTypes.END_ROD, p.getX(), p.getEyeY() + 1.5, p.getZ(), 12, 0.6, 0.4, 0.6, 0.01);
            level.playSound(null, p.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1f, 1.5f);
            sp.getCooldowns().addCooldown(this, Config.get(Config.SEXTANT_COOLDOWN));
            st.hurtAndBreak(1, sp, hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
            return InteractionResultHolder.consume(st);
        }

        @Override
        public void appendHoverText(ItemStack s, TooltipContext c, List<Component> t, TooltipFlag f) {
            t.add(Component.translatable("tooltip.primeva.astral_sextant").withStyle(ChatFormatting.GRAY));
        }
    }

    /** Cérebro Apodrecido: comida nojenta que ensina Umbra e turva a mente. */
    public static class DecayingBrain extends Item {
        public DecayingBrain(Properties p) {
            super(p.food(new net.minecraft.world.food.FoodProperties.Builder().nutrition(2).saturationModifier(0.1f).alwaysEdible().build()));
        }

        @Override
        public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity e) {
            if (e instanceof ServerPlayer sp) {
                Knowledge k = sp.getData(ModAttachments.KNOWLEDGE);
                k.add(Essence.UMBRA, 3);
                Network.sync(sp);
                Warp.addTemp(sp, 1);
                sp.addEffect(new MobEffectInstance(MobEffects.HUNGER, 300, 0));
                sp.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 160, 0));
            }
            return super.finishUsingItem(stack, level, e);
        }

        @Override
        public void appendHoverText(ItemStack s, TooltipContext c, List<Component> t, TooltipFlag f) {
            t.add(Component.translatable("tooltip.primeva.decaying_brain").withStyle(ChatFormatting.DARK_GREEN));
        }
    }

    /** Peça de armadura de Aço do Vazio (o conserto e o bônus de conjunto ficam em CommonEvents). */
    public static class VoidArmor extends ArmorItem {
        public VoidArmor(Type type, Properties p) {
            super(ModArmor.VOIDSTEEL, type, p.durability(type.getDurability(30)).rarity(Rarity.RARE));
        }

        @Override
        public void appendHoverText(ItemStack s, TooltipContext c, List<Component> t, TooltipFlag f) {
            t.add(Component.translatable("tooltip.primeva.voidsteel_armor").withStyle(ChatFormatting.DARK_PURPLE));
        }
    }

    private NewItems() {}
}
