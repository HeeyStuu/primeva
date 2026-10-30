package dev.heeystuuh.primeva;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Map;

/** O que a Lente está mirando: item na outra mão, criatura ou bloco (até 10 blocos). */
public record ScanTarget(String key, Component name, Map<Essence, Integer> essences, Vec3 pos) {
    public static final double RANGE = 10.0;

    public static ScanTarget find(Player player) {
        ItemStack other = player.getItemInHand(InteractionHand.OFF_HAND);
        if (!other.isEmpty() && !(other.getItem() instanceof dev.heeystuuh.primeva.item.LensItem)) {
            Vec3 hand = player.getEyePosition().add(player.getLookAngle().scale(0.7)).add(0, -0.35, 0);
            return ofItem(other.getItem(), other.getHoverName(), hand);
        }
        Vec3 eye = player.getEyePosition();
        Vec3 end = eye.add(player.getLookAngle().scale(RANGE));
        HitResult block = player.pick(RANGE, 0f, false);
        double blockDist = block.getType() == HitResult.Type.MISS ? RANGE * RANGE : block.getLocation().distanceToSqr(eye);
        EntityHitResult ent = ProjectileUtil.getEntityHitResult(player, eye, end,
                player.getBoundingBox().expandTowards(player.getLookAngle().scale(RANGE)).inflate(1.0),
                e -> e instanceof LivingEntity && e.isAlive() && !e.isSpectator(), blockDist);
        if (ent != null) {
            Entity e = ent.getEntity();
            return new ScanTarget("entity:" + BuiltInRegistries.ENTITY_TYPE.getKey(e.getType()), e.getType().getDescription(),
                    ModDataMaps.of(e.getType()), e.position().add(0, e.getBbHeight() * 0.6, 0));
        }
        if (block instanceof BlockHitResult bh && block.getType() == HitResult.Type.BLOCK) {
            BlockState st = player.level().getBlockState(bh.getBlockPos());
            if (st.isAir()) return null;
            return ofItem(st.getBlock().asItem(), st.getBlock().getName(), Vec3.atCenterOf(bh.getBlockPos()));
        }
        return null;
    }

    private static ScanTarget ofItem(Item item, Component name, Vec3 pos) {
        return new ScanTarget("item:" + BuiltInRegistries.ITEM.getKey(item), name, ModDataMaps.of(item), pos);
    }

    public Essence dominant() {
        Essence best = null;
        for (Map.Entry<Essence, Integer> e : essences.entrySet()) if (best == null || e.getValue() > essences.get(best)) best = e.getKey();
        return best;
    }
}
