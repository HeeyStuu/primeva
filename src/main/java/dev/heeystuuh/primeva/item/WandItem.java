package dev.heeystuuh.primeva.item;

import dev.heeystuuh.primeva.Essence;
import dev.heeystuuh.primeva.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Varinhas elementais. A durabilidade é a carga: cada magia gasta carga e,
 * quando ela acaba, a varinha bebe sozinha um frasco da sua essência do inventário (+16 de carga).
 */
public class WandItem extends Item {
    public static final int CHARGE = 64;
    public final Essence essence;

    public WandItem(Essence essence, Properties props) {
        super(props.durability(CHARGE));
        this.essence = essence;
    }

    private int cost() {
        return switch (essence) {
            case TIDE -> 4;
            case TERRA, UMBRA -> 2;
            default -> 1;
        };
    }

    private int cooldown() {
        return switch (essence) {
            case EMBER -> 12;
            case TIDE -> 100;
            case TERRA -> 16;
            case GALE -> 30;
            case LUMEN -> 8;
            case UMBRA -> 24;
        };
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return false;
    }

    /** Recarrega com um frasco do inventário se preciso. */
    private boolean ensureCharge(Player p, ItemStack wand) {
        if (p.getAbilities().instabuild) return true;
        if (wand.getMaxDamage() - wand.getDamageValue() > cost()) return true;
        Item phial = ModItems.phial(essence);
        for (int i = 0; i < p.getInventory().getContainerSize(); i++) {
            ItemStack s = p.getInventory().getItem(i);
            if (s.is(phial)) {
                s.shrink(1);
                ItemStack empty = new ItemStack(ModItems.EMPTY_PHIAL.get());
                if (!p.getInventory().add(empty)) p.drop(empty, false);
                wand.setDamageValue(Math.max(0, wand.getDamageValue() - 16));
                p.level().playSound(null, p.blockPosition(), SoundEvents.BOTTLE_EMPTY, SoundSource.PLAYERS, 0.8f, 1.2f);
                return true;
            }
        }
        return false;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack wand = player.getItemInHand(hand);
        if (!(level instanceof ServerLevel sl) || !(player instanceof ServerPlayer sp)) return InteractionResultHolder.success(wand);
        if (!ensureCharge(sp, wand)) {
            sp.displayClientMessage(Component.translatable("msg.primeva.wand_empty", ModItems.phial(essence).getDescription()).withStyle(ChatFormatting.RED), true);
            return InteractionResultHolder.fail(wand);
        }
        boolean ok = switch (essence) {
            case EMBER -> ember(sl, sp);
            case TIDE -> tide(sl, sp);
            case TERRA -> terra(sl, sp, wand);
            case GALE -> gale(sl, sp);
            case LUMEN -> lumen(sl, sp);
            case UMBRA -> umbra(sl, sp);
        };
        if (!ok) return InteractionResultHolder.fail(wand);
        if (!sp.getAbilities().instabuild && !dev.heeystuuh.primeva.CommonEvents.saveCharge(sp)) wand.setDamageValue(Math.min(wand.getMaxDamage() - 1, wand.getDamageValue() + cost()));
        sp.getCooldowns().addCooldown(this, cooldown());
        dev.heeystuuh.primeva.Discovery.grant(sp, "do:wand");
        sp.swing(hand, true);
        return InteractionResultHolder.consume(wand);
    }

    private static void trail(ServerLevel sl, Vec3 a, Vec3 b, DustParticleOptions dust) {
        Vec3 d = b.subtract(a);
        int n = (int) Math.max(4, d.length() * 3);
        for (int i = 0; i <= n; i++) {
            Vec3 p = a.add(d.scale(i / (double) n));
            sl.sendParticles(dust, p.x, p.y, p.z, 1, 0.03, 0.03, 0.03, 0);
        }
    }

    // ---------------- magias ----------------
    private boolean ember(ServerLevel sl, ServerPlayer p) {
        Vec3 look = p.getLookAngle();
        for (int i = -1; i <= 1; i++) {
            Vec3 dir = look.yRot(i * 0.12f).normalize();
            SmallFireball fb = new SmallFireball(sl, p, dir.scale(1.0));
            fb.setPos(p.getX() + dir.x * 0.8, p.getEyeY() - 0.1, p.getZ() + dir.z * 0.8);
            sl.addFreshEntity(fb);
        }
        sl.playSound(null, p.blockPosition(), SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 1f, 1.2f);
        return true;
    }

    private boolean tide(ServerLevel sl, ServerPlayer p) {
        for (Player o : sl.getEntitiesOfClass(Player.class, p.getBoundingBox().inflate(6))) {
            o.heal(6f);
            o.clearFire();
            o.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 1));
            o.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 1200, 0));
            sl.sendParticles(ParticleTypes.SPLASH, o.getX(), o.getY() + 1, o.getZ(), 40, 0.5, 0.6, 0.5, 0.2);
            sl.sendParticles(new DustParticleOptions(Essence.TIDE.color(), 1.2f), o.getX(), o.getY() + 1, o.getZ(), 20, 0.6, 0.8, 0.6, 0);
        }
        sl.playSound(null, p.blockPosition(), SoundEvents.CONDUIT_ACTIVATE, SoundSource.PLAYERS, 0.8f, 1.4f);
        return true;
    }

    private boolean terra(ServerLevel sl, ServerPlayer p, ItemStack wand) {
        HitResult hit = p.pick(6.0, 0f, false);
        if (!(hit instanceof BlockHitResult bh) || hit.getType() != HitResult.Type.BLOCK) return false;
        BlockPos c = bh.getBlockPos();
        int broken = 0;
        for (BlockPos pos : BlockPos.betweenClosed(c.offset(-1, -1, -1), c.offset(1, 1, 1))) {
            BlockState st = sl.getBlockState(pos);
            float hard = st.getDestroySpeed(sl, pos);
            if (st.isAir() || hard < 0 || hard > 3.0f || st.hasBlockEntity()) continue;
            if (!sl.mayInteract(p, pos)) continue;
            if (sl.destroyBlock(pos, true, p)) broken++;
        }
        sl.sendParticles(new DustParticleOptions(Essence.TERRA.color(), 1.5f), c.getX() + 0.5, c.getY() + 0.5, c.getZ() + 0.5, 40, 1, 1, 1, 0);
        sl.playSound(null, c, SoundEvents.ROOTED_DIRT_BREAK, SoundSource.PLAYERS, 1f, 0.7f);
        return broken > 0;
    }

    private boolean gale(ServerLevel sl, ServerPlayer p) {
        Vec3 look = p.getLookAngle();
        p.setDeltaMovement(look.x * 1.9, Math.max(0.35, look.y * 1.4 + 0.3), look.z * 1.9);
        p.hurtMarked = true;
        p.fallDistance = 0;
        p.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 60, 0));
        sl.sendParticles(ParticleTypes.CLOUD, p.getX(), p.getY() + 0.2, p.getZ(), 20, 0.4, 0.1, 0.4, 0.05);
        sl.playSound(null, p.blockPosition(), SoundEvents.BREEZE_JUMP, SoundSource.PLAYERS, 1f, 1.1f);
        return true;
    }

    private boolean lumen(ServerLevel sl, ServerPlayer p) {
        HitResult hit = p.pick(24.0, 0f, false);
        if (hit instanceof BlockHitResult bh && hit.getType() == HitResult.Type.BLOCK) {
            BlockPos at = bh.getBlockPos().relative(bh.getDirection());
            if (sl.getBlockState(at).isAir() && sl.mayInteract(p, at)) {
                sl.setBlockAndUpdate(at, Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL, 15));
                trail(sl, p.getEyePosition().add(0, -0.2, 0), Vec3.atCenterOf(at), new DustParticleOptions(Essence.LUMEN.color(), 1.0f));
                sl.sendParticles(ParticleTypes.END_ROD, at.getX() + 0.5, at.getY() + 0.5, at.getZ() + 0.5, 12, 0.2, 0.2, 0.2, 0.05);
            }
        }
        for (LivingEntity e : sl.getEntitiesOfClass(LivingEntity.class, p.getBoundingBox().inflate(16), e -> e instanceof Enemy)) {
            e.addEffect(new MobEffectInstance(MobEffects.GLOWING, 200, 0));
        }
        sl.playSound(null, p.blockPosition(), SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 0.6f, 1.8f);
        return true;
    }

    private boolean umbra(ServerLevel sl, ServerPlayer p) {
        Vec3 eye = p.getEyePosition(), end = eye.add(p.getLookAngle().scale(16));
        EntityHitResult hit = ProjectileUtil.getEntityHitResult(p, eye, end, new AABB(eye, end).inflate(1),
                e -> e instanceof LivingEntity && e.isAlive() && e != p, 16 * 16);
        if (hit == null) return false;
        Entity t = hit.getEntity();
        if (t instanceof LivingEntity le) {
            le.hurt(sl.damageSources().indirectMagic(p, p), 9f);
            le.addEffect(new MobEffectInstance(MobEffects.WITHER, 80, 1), p);
            p.heal(4f);
            trail(sl, t.position().add(0, t.getBbHeight() * 0.6, 0), eye.add(0, -0.3, 0), new DustParticleOptions(Essence.UMBRA.color(), 1.2f));
            sl.sendParticles(ParticleTypes.SCULK_SOUL, t.getX(), t.getY() + 1, t.getZ(), 8, 0.3, 0.4, 0.3, 0.02);
            sl.playSound(null, p.blockPosition(), SoundEvents.WARDEN_SONIC_CHARGE, SoundSource.PLAYERS, 0.6f, 1.8f);
            return true;
        }
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.primeva.wand_" + essence.id).withStyle(essence.fmt));
        tooltip.add(Component.translatable("tooltip.primeva.wand_charge", stack.getMaxDamage() - stack.getDamageValue(), CHARGE).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.primeva.wand_refill", ModItems.phial(essence).getDescription()).withStyle(ChatFormatting.DARK_GRAY));
    }
}
