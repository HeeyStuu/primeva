package dev.heeystuuh.primeva.item;

import dev.heeystuuh.primeva.Essence;
import dev.heeystuuh.primeva.ModItems;
import dev.heeystuuh.primeva.PortableHoles;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * Focos Arcanos: ferramentas mágicas com carga própria (durabilidade).
 * Recarregam sozinhos bebendo um frasco da essência do foco que esteja no inventário.
 */
public class FocusItem extends Item {
    public enum Kind {
        FROST(Essence.TIDE, 2, 14), SHOCK(Essence.GALE, 2, 20), EXCAVATION(Essence.TERRA, 1, 5),
        EXCHANGE(Essence.TERRA, 1, 4), HOLE(Essence.UMBRA, 4, 40),
        FLAME(Essence.EMBER, 3, 24), RADIANCE(Essence.LUMEN, 4, 80);
        public final Essence essence;
        public final int cost, cooldown;

        Kind(Essence e, int cost, int cooldown) {
            this.essence = e;
            this.cost = cost;
            this.cooldown = cooldown;
        }
    }

    public static final int CHARGE = 96;
    public final Kind kind;

    public FocusItem(Kind kind, Properties props) {
        super(props.durability(CHARGE));
        this.kind = kind;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return false;
    }

    private boolean ensureCharge(Player p, ItemStack focus) {
        if (p.getAbilities().instabuild) return true;
        if (focus.getMaxDamage() - focus.getDamageValue() > kind.cost) return true;
        Item phial = ModItems.phial(kind.essence);
        for (int i = 0; i < p.getInventory().getContainerSize(); i++) {
            ItemStack s = p.getInventory().getItem(i);
            if (s.is(phial)) {
                s.shrink(1);
                ItemStack empty = new ItemStack(ModItems.EMPTY_PHIAL.get());
                if (!p.getInventory().add(empty)) p.drop(empty, false);
                focus.setDamageValue(Math.max(0, focus.getDamageValue() - 24));
                p.level().playSound(null, p.blockPosition(), SoundEvents.BOTTLE_EMPTY, SoundSource.PLAYERS, 0.8f, 1.2f);
                return true;
            }
        }
        return false;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack focus = player.getItemInHand(hand);
        if (!(level instanceof ServerLevel sl) || !(player instanceof ServerPlayer sp)) return InteractionResultHolder.success(focus);
        if (!ensureCharge(sp, focus)) {
            sp.displayClientMessage(Component.translatable("msg.primeva.wand_empty", ModItems.phial(kind.essence).getDescription()).withStyle(ChatFormatting.RED), true);
            return InteractionResultHolder.fail(focus);
        }
        boolean ok = switch (kind) {
            case FROST -> frost(sl, sp);
            case SHOCK -> shock(sl, sp);
            case EXCAVATION -> excavate(sl, sp);
            case EXCHANGE -> exchange(sl, sp, hand);
            case HOLE -> hole(sl, sp);
            case FLAME -> flame(sl, sp);
            case RADIANCE -> radiance(sl, sp);
        };
        if (!ok) return InteractionResultHolder.fail(focus);
        if (!sp.getAbilities().instabuild && !dev.heeystuuh.primeva.CommonEvents.saveCharge(sp)) focus.setDamageValue(Math.min(focus.getMaxDamage() - 1, focus.getDamageValue() + kind.cost));
        sp.getCooldowns().addCooldown(this, kind.cooldown);
        dev.heeystuuh.primeva.Discovery.grant(sp, "do:wand");
        sp.swing(hand, true);
        return InteractionResultHolder.consume(focus);
    }

    private static void trail(ServerLevel sl, Vec3 a, Vec3 b, net.minecraft.core.particles.ParticleOptions p) {
        Vec3 d = b.subtract(a);
        int n = (int) Math.max(4, d.length() * 3);
        for (int i = 0; i <= n; i++) {
            Vec3 q = a.add(d.scale(i / (double) n));
            sl.sendParticles(p, q.x, q.y, q.z, 1, 0.02, 0.02, 0.02, 0);
        }
    }

    private static LivingEntity aim(ServerPlayer p, double range) {
        Vec3 eye = p.getEyePosition(), end = eye.add(p.getLookAngle().scale(range));
        HitResult block = p.pick(range, 0f, false);
        double max = block.getType() == HitResult.Type.MISS ? range * range : block.getLocation().distanceToSqr(eye);
        EntityHitResult hit = ProjectileUtil.getEntityHitResult(p, eye, end, new AABB(eye, end).inflate(1),
                e -> e instanceof LivingEntity && e.isAlive() && e != p, max);
        return hit != null && hit.getEntity() instanceof LivingEntity le ? le : null;
    }

    // Gélido: congela o alvo e transforma água em gelo
    private boolean frost(ServerLevel sl, ServerPlayer p) {
        Vec3 eye = p.getEyePosition().add(0, -0.2, 0);
        LivingEntity t = aim(p, 20);
        boolean did = false;
        if (t != null) {
            t.hurt(sl.damageSources().freeze(), 6f);
            t.setTicksFrozen(Math.max(t.getTicksFrozen(), 300));
            t.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 120, 3), p);
            trail(sl, eye, t.position().add(0, t.getBbHeight() * 0.6, 0), ParticleTypes.SNOWFLAKE);
            did = true;
        }
        HitResult hit = p.pick(20, 0f, true);
        if (hit instanceof BlockHitResult bh && hit.getType() == HitResult.Type.BLOCK) {
            BlockPos c = bh.getBlockPos();
            for (BlockPos q : BlockPos.betweenClosed(c.offset(-2, -1, -2), c.offset(2, 1, 2))) {
                BlockState st = sl.getBlockState(q);
                if (st.is(Blocks.WATER) && st.getFluidState().isSource() && sl.mayInteract(p, q)) {
                    sl.setBlockAndUpdate(q, Blocks.FROSTED_ICE.defaultBlockState());
                    did = true;
                } else if (st.is(Blocks.LAVA) && st.getFluidState().isSource() && sl.mayInteract(p, q)) {
                    sl.setBlockAndUpdate(q, Blocks.OBSIDIAN.defaultBlockState());
                    did = true;
                }
            }
            if (!did) trail(sl, eye, hit.getLocation(), ParticleTypes.SNOWFLAKE);
            did = true;
        }
        sl.playSound(null, p.blockPosition(), SoundEvents.POWDER_SNOW_PLACE, SoundSource.PLAYERS, 1f, 0.8f);
        sl.playSound(null, p.blockPosition(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 0.4f, 1.6f);
        return did;
    }

    // Fulminante: raio que salta entre até 4 inimigos
    private boolean shock(ServerLevel sl, ServerPlayer p) {
        LivingEntity t = aim(p, 18);
        if (t == null) return false;
        List<LivingEntity> hit = new ArrayList<>();
        Vec3 from = p.getEyePosition().add(0, -0.2, 0);
        LivingEntity cur = t;
        float dmg = 8f;
        while (cur != null && hit.size() < 4) {
            Vec3 to = cur.position().add(0, cur.getBbHeight() * 0.5, 0);
            trail(sl, from, to, ParticleTypes.ELECTRIC_SPARK);
            cur.hurt(sl.damageSources().indirectMagic(p, p), dmg);
            hit.add(cur);
            dmg *= 0.8f;
            from = to;
            LivingEntity next = null;
            double best = 7 * 7;
            for (LivingEntity o : sl.getEntitiesOfClass(LivingEntity.class, cur.getBoundingBox().inflate(7),
                    e -> e.isAlive() && e != p && !hit.contains(e) && !(e instanceof Player))) {
                double d = o.distanceToSqr(cur);
                if (d < best) {
                    best = d;
                    next = o;
                }
            }
            cur = next;
        }
        sl.playSound(null, t.blockPosition(), SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.PLAYERS, 0.6f, 1.6f);
        return true;
    }

    // Escavação: quebra o bloco mirado à distância
    private boolean excavate(ServerLevel sl, ServerPlayer p) {
        HitResult hit = p.pick(16, 0f, false);
        if (!(hit instanceof BlockHitResult bh) || hit.getType() != HitResult.Type.BLOCK) return false;
        BlockPos pos = bh.getBlockPos();
        BlockState st = sl.getBlockState(pos);
        float hard = st.getDestroySpeed(sl, pos);
        if (st.isAir() || hard < 0 || hard > 50f || st.hasBlockEntity() || !sl.mayInteract(p, pos)) return false;
        trail(sl, p.getEyePosition().add(0, -0.2, 0), Vec3.atCenterOf(pos), new DustParticleOptions(Essence.TERRA.color(), 0.8f));
        sl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, st), pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 20, 0.3, 0.3, 0.3, 0.1);
        return sl.destroyBlock(pos, true, p);
    }

    // Troca: substitui o bloco mirado pelo bloco da outra mão
    private boolean exchange(ServerLevel sl, ServerPlayer p, InteractionHand hand) {
        ItemStack other = p.getItemInHand(hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
        if (!(other.getItem() instanceof BlockItem bi)) {
            p.displayClientMessage(Component.translatable("msg.primeva.exchange_need_block").withStyle(ChatFormatting.GRAY), true);
            return false;
        }
        HitResult hit = p.pick(12, 0f, false);
        if (!(hit instanceof BlockHitResult bh) || hit.getType() != HitResult.Type.BLOCK) return false;
        Block nb = bi.getBlock();
        Direction face = bh.getDirection();
        BlockPos c = bh.getBlockPos();
        int done = 0;
        int radius = p.isShiftKeyDown() ? 1 : 0;
        for (int a = -radius; a <= radius; a++) {
            for (int b = -radius; b <= radius; b++) {
                if (other.isEmpty() && !p.getAbilities().instabuild) break;
                BlockPos pos = switch (face.getAxis()) {
                    case X -> c.offset(0, a, b);
                    case Y -> c.offset(a, 0, b);
                    case Z -> c.offset(a, b, 0);
                };
                BlockState st = sl.getBlockState(pos);
                float hard = st.getDestroySpeed(sl, pos);
                if (st.isAir() || st.is(nb) || hard < 0 || hard > 50f || st.hasBlockEntity() || !sl.mayInteract(p, pos)) continue;
                if (!st.getFluidState().isEmpty()) continue;
                Block.dropResources(st, sl, pos, null, p, ItemStack.EMPTY);
                sl.setBlockAndUpdate(pos, nb.defaultBlockState());
                sl.sendParticles(new DustParticleOptions(Essence.LUMEN.color(), 1f), pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 6, 0.4, 0.4, 0.4, 0);
                if (!p.getAbilities().instabuild) other.shrink(1);
                done++;
            }
        }
        if (done > 0) sl.playSound(null, c, SoundEvents.ILLUSIONER_MIRROR_MOVE, SoundSource.PLAYERS, 0.8f, 1.3f);
        return done > 0;
    }

    // Labareda: cone de fogo à frente que incendeia inimigos e queima a grama
    private boolean flame(ServerLevel sl, ServerPlayer p) {
        Vec3 eye = p.getEyePosition(), look = p.getLookAngle();
        int hit = 0;
        for (LivingEntity e : sl.getEntitiesOfClass(LivingEntity.class, p.getBoundingBox().inflate(7), e -> e.isAlive() && e != p)) {
            Vec3 to = e.position().add(0, e.getBbHeight() * 0.5, 0).subtract(eye);
            if (to.length() > 7 || to.normalize().dot(look) < 0.75) continue;
            e.hurt(sl.damageSources().indirectMagic(p, p), 5f);
            e.igniteForSeconds(6);
            hit++;
        }
        for (int i = 0; i < 40; i++) {
            Vec3 d = look.add((sl.random.nextDouble() - 0.5) * 0.5, (sl.random.nextDouble() - 0.5) * 0.4, (sl.random.nextDouble() - 0.5) * 0.5).normalize();
            double dist = 1 + sl.random.nextDouble() * 6;
            Vec3 q = eye.add(d.scale(dist));
            sl.sendParticles(ParticleTypes.FLAME, q.x, q.y, q.z, 1, 0.05, 0.05, 0.05, 0.01);
        }
        sl.playSound(null, p.blockPosition(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 1f, 0.8f);
        return true;
    }

    // Radiância: cura você e aliados próximos e queima mortos-vivos
    private boolean radiance(ServerLevel sl, ServerPlayer p) {
        p.heal(6f);
        p.removeEffect(MobEffects.POISON);
        p.removeEffect(MobEffects.WITHER);
        for (Player o : sl.getEntitiesOfClass(Player.class, p.getBoundingBox().inflate(8), o -> o != p && o.isAlive())) o.heal(4f);
        for (LivingEntity e : sl.getEntitiesOfClass(LivingEntity.class, p.getBoundingBox().inflate(8), e -> e.isAlive() && e.isInvertedHealAndHarm())) {
            e.hurt(sl.damageSources().indirectMagic(p, p), 8f);
            e.igniteForSeconds(4);
        }
        sl.sendParticles(ParticleTypes.END_ROD, p.getX(), p.getY() + 1, p.getZ(), 60, 3, 1, 3, 0.02);
        sl.playSound(null, p.blockPosition(), SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 1f, 1.6f);
        return true;
    }

    // Vazio Portátil: abre um túnel temporário de 1x2 por 8 blocos
    private boolean hole(ServerLevel sl, ServerPlayer p) {
        HitResult hit = p.pick(6, 0f, false);
        if (!(hit instanceof BlockHitResult bh) || hit.getType() != HitResult.Type.BLOCK) return false;
        Direction into = bh.getDirection().getOpposite();
        BlockPos start = bh.getBlockPos();
        boolean vertical = into.getAxis() == Direction.Axis.Y;
        int opened = 0;
        for (int i = 0; i < 8; i++) {
            BlockPos base = start.relative(into, i);
            for (int k = 0; k < (vertical ? 1 : 2); k++) {
                BlockPos pos = base.above(k);
                if (vertical) pos = base;
                if (PortableHoles.open(sl, p, pos)) opened++;
            }
        }
        if (opened == 0) return false;
        sl.sendParticles(ParticleTypes.REVERSE_PORTAL, start.getX() + 0.5, start.getY() + 0.5, start.getZ() + 0.5, 40, 0.4, 0.6, 0.4, 0.05);
        sl.playSound(null, start, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.8f, 0.6f);
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tooltip, TooltipFlag flag) {
        String id = kind.name().toLowerCase();
        tooltip.add(Component.translatable("tooltip.primeva.focus_" + id).withStyle(kind.essence.fmt));
        tooltip.add(Component.translatable("tooltip.primeva.wand_charge", stack.getMaxDamage() - stack.getDamageValue(), CHARGE).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.primeva.wand_refill", ModItems.phial(kind.essence).getDescription()).withStyle(ChatFormatting.DARK_GRAY));
    }
}
