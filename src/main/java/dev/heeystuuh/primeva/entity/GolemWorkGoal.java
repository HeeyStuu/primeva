package dev.heeystuuh.primeva.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.*;

/**
 * Trabalho dos golens: Colheita, Coleta, Lenhador e Abastecimento.
 * - Procura alvos só a cada 1-3 s (menos CPU) e respeita o filtro e a capacidade do golem.
 * - Se não consegue chegar a um alvo (caminho impossível ou preso), coloca o alvo numa lista negra por 1 minuto.
 */
public class GolemWorkGoal extends Goal {
    private final ArcaneGolem g;
    private BlockPos target;
    private ItemEntity item;
    private int search;
    // anti-travamento
    private final Map<BlockPos, Long> badPos = new HashMap<>();
    private final Map<UUID, Long> badItems = new HashMap<>();
    private double bestDist = Double.MAX_VALUE;
    private int noProgress;

    public GolemWorkGoal(ArcaneGolem g) {
        this.g = g;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    private ArcaneGolem.Task task() {
        return g.task();
    }

    private long now() {
        return g.level().getGameTime();
    }

    private boolean working() {
        ArcaneGolem.Task t = task();
        return g.home() != null && t != ArcaneGolem.Task.NONE && t != ArcaneGolem.Task.GUARD;
    }

    private boolean carrying() {
        return !g.inv.isEmpty();
    }

    private boolean bad(BlockPos p) {
        Long t = badPos.get(p);
        return t != null && t > now();
    }

    private void markBad() {
        if (target != null) badPos.put(target, now() + 1200);
        if (item != null) badItems.put(item.getUUID(), now() + 1200);
        target = null;
        item = null;
        resetProgress();
    }

    private void resetProgress() {
        bestDist = Double.MAX_VALUE;
        noProgress = 0;
    }

    @Override
    public boolean canUse() {
        if (!working()) return false;
        if (carrying()) return true;
        if (--search > 0) return false;
        badPos.values().removeIf(t -> t <= now());
        badItems.values().removeIf(t -> t <= now());
        boolean found = findTarget();
        search = found ? 20 : 60; // sem trabalho: espera mais para procurar de novo
        return found;
    }

    @Override
    public boolean canContinueToUse() {
        return working() && (carrying() || target != null || (item != null && item.isAlive()));
    }

    private Iterable<BlockPos> area(int dy) {
        BlockPos h = g.home();
        int r = g.workRange();
        return BlockPos.betweenClosed(h.offset(-r, -dy, -r), h.offset(r, dy, r));
    }

    private boolean findTarget() {
        target = null;
        item = null;
        resetProgress();
        BlockPos h = g.home();
        double best = Double.MAX_VALUE;
        switch (task()) {
            case HARVEST -> {
                for (BlockPos p : area(2)) {
                    BlockState st = g.level().getBlockState(p);
                    if (st.getBlock() instanceof CropBlock c && c.isMaxAge(st) && !bad(p)
                            && (g.filter.isEmpty() || g.filter.contains(c.getCloneItemStack(g.level(), p, st).getItem()))) {
                        double d = p.distSqr(g.blockPosition());
                        if (d < best) { best = d; target = p.immutable(); }
                    }
                }
            }
            case LUMBER -> {
                for (BlockPos p : area(3)) {
                    BlockState st = g.level().getBlockState(p);
                    if (st.is(BlockTags.LOGS) && !bad(p) && g.level().getBlockState(p.below()).is(BlockTags.DIRT)
                            && g.level().getBlockState(p.above()).is(BlockTags.LOGS)
                            && (g.filter.isEmpty() || g.filter.contains(st.getBlock().asItem()))) {
                        double d = p.distSqr(g.blockPosition());
                        if (d < best) { best = d; target = p.immutable(); }
                    }
                }
            }
            case PICKUP -> {
                for (ItemEntity ie : g.level().getEntitiesOfClass(ItemEntity.class, new AABB(h).inflate(g.workRange()), ItemEntity::isAlive)) {
                    Long t = badItems.get(ie.getUUID());
                    if (t != null && t > now()) continue;
                    if (!g.allows(ie.getItem())) continue;
                    double d = ie.distanceToSqr(g);
                    if (d < best) { best = d; item = ie; }
                }
            }
            case STOCK -> {
                Container home = HopperBlockEntity.getContainerAt(g.level(), h);
                if (home == null) return false;
                for (BlockPos p : area(2)) {
                    if (p.equals(h) || bad(p)) continue;
                    BlockEntity be = g.level().getBlockEntity(p);
                    if (!(be instanceof Container dest)) continue;
                    for (int i = 0; i < home.getContainerSize(); i++) {
                        ItemStack s = home.getItem(i);
                        if (s.isEmpty() || !g.allows(s) || !contains(dest, s) || !hasSpace(dest, s)) continue;
                        double d = p.distSqr(g.blockPosition());
                        if (d < best) { best = d; target = p.immutable(); }
                        break;
                    }
                }
            }
            default -> {}
        }
        return target != null || item != null;
    }

    private static boolean contains(Container c, ItemStack s) {
        for (int i = 0; i < c.getContainerSize(); i++) if (ItemStack.isSameItemSameComponents(c.getItem(i), s)) return true;
        return false;
    }

    private static boolean hasSpace(Container c, ItemStack s) {
        for (int i = 0; i < c.getContainerSize(); i++) {
            ItemStack o = c.getItem(i);
            if (o.isEmpty() || (ItemStack.isSameItemSameComponents(o, s) && o.getCount() < o.getMaxStackSize())) return true;
        }
        return false;
    }

    /** Anda até 'to'. Se o caminho é impossível ou o golem não chega mais perto por 4 s, desiste do alvo. */
    private void moveTo(Vec3 to, boolean isHome) {
        double d = g.position().distanceToSqr(to);
        if (d < bestDist - 0.25) {
            bestDist = d;
            noProgress = 0;
        } else if (++noProgress > 80) {
            if (isHome) {
                // não alcança a casa: deixa os itens no chão perto dela e segue a vida
                for (int i = 0; i < g.inv.getContainerSize(); i++) {
                    if (!g.inv.getItem(i).isEmpty()) g.spawnAtLocation(g.inv.getItem(i));
                    g.inv.setItem(i, ItemStack.EMPTY);
                }
                resetProgress();
            } else {
                markBad();
            }
            return;
        }
        if (g.tickCount % 10 == 0) {
            boolean ok = g.getNavigation().moveTo(to.x, to.y, to.z, 1.0);
            if (!ok && !isHome && d > 9) markBad();
        }
    }

    @Override
    public void tick() {
        BlockPos h = g.home();
        if (task() == ArcaneGolem.Task.STOCK) tickStock(h);
        else tickGather(h);
    }

    private void goHome(BlockPos h) {
        if (g.blockPosition().closerThan(h, 2.6)) {
            g.deposit();
            resetProgress();
            search = 0;
        } else {
            moveTo(Vec3.atBottomCenterOf(h.above()), true);
        }
    }

    // ---------------- Abastecimento ----------------
    private void tickStock(BlockPos h) {
        if (target == null) {
            if (carrying()) goHome(h);
            return;
        }
        if (!carrying()) {
            if (!g.blockPosition().closerThan(h, 2.6)) {
                moveTo(Vec3.atBottomCenterOf(h.above()), true);
                return;
            }
            Container home = HopperBlockEntity.getContainerAt(g.level(), h);
            BlockEntity be = g.level().getBlockEntity(target);
            if (home == null || !(be instanceof Container dest)) {
                target = null;
                return;
            }
            for (int i = 0; i < home.getContainerSize(); i++) {
                ItemStack s = home.getItem(i);
                if (s.isEmpty() || !g.allows(s) || !contains(dest, s)) continue;
                ItemStack taken = home.removeItem(i, s.getCount());
                ItemStack back = g.store(taken);
                if (!back.isEmpty()) home.setItem(i, back); // nada se perde
                home.setChanged();
                g.swing(InteractionHand.MAIN_HAND);
                resetProgress();
                return;
            }
            target = null;
            return;
        }
        if (g.blockPosition().closerThan(target, 2.6)) {
            BlockEntity be = g.level().getBlockEntity(target);
            if (be instanceof Container dest) {
                for (int i = 0; i < g.inv.getContainerSize(); i++) {
                    ItemStack s = g.inv.getItem(i);
                    if (s.isEmpty()) continue;
                    g.inv.setItem(i, HopperBlockEntity.addItem(null, dest, s, null));
                }
            }
            g.swing(InteractionHand.MAIN_HAND);
            target = null;
            search = 0;
            resetProgress();
        } else {
            moveTo(Vec3.atBottomCenterOf(target), false);
        }
    }

    // ---------------- Colheita / Lenhador / Coleta ----------------
    private void tickGather(BlockPos h) {
        if (carrying() && (g.full() || (target == null && (item == null || !item.isAlive())))) {
            goHome(h);
            return;
        }
        if (target != null) {
            g.getLookControl().setLookAt(target.getX() + 0.5, target.getY() + 0.5, target.getZ() + 0.5);
            if (g.blockPosition().closerThan(target, 2.4)) {
                if (g.level() instanceof ServerLevel sl) {
                    if (task() == ArcaneGolem.Task.HARVEST) harvest(sl, target);
                    else if (task() == ArcaneGolem.Task.LUMBER) fell(sl, target);
                    g.swing(InteractionHand.MAIN_HAND);
                }
                target = null;
                resetProgress();
                if (!g.full()) findTarget();
            } else {
                moveTo(Vec3.atBottomCenterOf(target), false);
            }
            return;
        }
        if (item != null) {
            if (!item.isAlive()) {
                item = null;
                findTarget();
                return;
            }
            if (g.distanceToSqr(item) < 2.5) {
                ItemStack rest = g.store(item.getItem());
                if (rest.isEmpty()) item.discard();
                else item.setItem(rest);
                item = null;
                resetProgress();
                if (!g.full()) findTarget();
            } else {
                moveTo(item.position(), false);
            }
        }
    }

    private void keep(ItemStack d) {
        ItemStack rest = g.store(d);
        if (!rest.isEmpty()) g.spawnAtLocation(rest);
    }

    private void harvest(ServerLevel sl, BlockPos p) {
        BlockState st = sl.getBlockState(p);
        if (!(st.getBlock() instanceof CropBlock c) || !c.isMaxAge(st)) return;
        boolean replanted = false;
        for (ItemStack d : Block.getDrops(st, sl, p, null)) {
            if (!replanted && d.getItem() == c.getCloneItemStack(sl, p, st).getItem()) {
                d.shrink(1);
                replanted = true;
            }
            if (!d.isEmpty()) keep(d);
        }
        sl.setBlockAndUpdate(p, c.getStateForAge(0));
    }

    private void fell(ServerLevel sl, BlockPos base) {
        if (!sl.getBlockState(base).is(BlockTags.LOGS)) return;
        Deque<BlockPos> q = new ArrayDeque<>();
        Set<BlockPos> seen = new HashSet<>();
        List<BlockPos> logs = new ArrayList<>();
        q.add(base);
        seen.add(base);
        while (!q.isEmpty() && logs.size() < 96) {
            BlockPos p = q.poll();
            if (!sl.getBlockState(p).is(BlockTags.LOGS)) continue;
            logs.add(p);
            for (BlockPos n : BlockPos.betweenClosed(p.offset(-1, 0, -1), p.offset(1, 1, 1))) {
                BlockPos im = n.immutable();
                if (seen.add(im)) q.add(im);
            }
        }
        for (BlockPos p : logs) {
            BlockState st = sl.getBlockState(p);
            for (ItemStack d : Block.getDrops(st, sl, p, null)) keep(d);
            sl.destroyBlock(p, false, g);
        }
        for (int i = 0; i < g.inv.getContainerSize(); i++) {
            ItemStack s = g.inv.getItem(i);
            if (s.is(ItemTags.SAPLINGS) && s.getItem() instanceof BlockItem bi) {
                BlockState sap = bi.getBlock().defaultBlockState();
                if (sap.canSurvive(sl, base)) {
                    sl.setBlockAndUpdate(base, sap);
                    s.shrink(1);
                }
                break;
            }
        }
    }

    @Override
    public void stop() {
        target = null;
        item = null;
        resetProgress();
        g.getNavigation().stop();
    }
}
