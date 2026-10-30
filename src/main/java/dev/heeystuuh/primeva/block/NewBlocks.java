package dev.heeystuuh.primeva.block;

import dev.heeystuuh.primeva.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;

import java.util.*;

/** Blocos do pacote de novidades. Todos trabalham com ticks agendados (sem block entity). */
public final class NewBlocks {

    /** Base com tick agendado: reagenda sozinho e pode ser parado por sinal de redstone. */
    public abstract static class Ticking extends Block {
        private final int interval;

        protected Ticking(int interval, Properties p) {
            super(p);
            this.interval = interval;
        }

        @Override
        protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState old, boolean moving) {
            super.onPlace(state, level, pos, old, moving);
            if (!level.isClientSide) level.scheduleTick(pos, this, interval);
        }

        @Override
        protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource r) {
            if (!level.hasNeighborSignal(pos)) work(level, pos, r);
            level.scheduleTick(pos, this, interval);
        }

        protected abstract void work(ServerLevel level, BlockPos pos, RandomSource r);
    }

    /** Lâmpada do Crescimento: acelera plantações e mudas ao redor. */
    public static class GrowthLamp extends Ticking {
        public GrowthLamp(Properties p) {
            super(20, p);
        }

        private static boolean grows(BlockState st) {
            Block b = st.getBlock();
            return b instanceof CropBlock || b instanceof StemBlock || b instanceof SaplingBlock || b instanceof SweetBerryBushBlock
                    || b instanceof NetherWartBlock || b instanceof CocoaBlock || b instanceof SugarCaneBlock || b instanceof CactusBlock
                    || b instanceof BambooStalkBlock || b instanceof PitcherCropBlock;
        }

        @Override
        protected void work(ServerLevel level, BlockPos pos, RandomSource r) {
            int rad = Config.get(Config.GROWTH_RADIUS);
            for (int i = 0; i < 10; i++) {
                BlockPos q = pos.offset(r.nextInt(rad * 2 + 1) - rad, r.nextInt(5) - 2, r.nextInt(rad * 2 + 1) - rad);
                BlockState st = level.getBlockState(q);
                if (!grows(st)) continue;
                st.randomTick(level, q, r);
                if (r.nextInt(3) == 0) level.sendParticles(ParticleTypes.HAPPY_VILLAGER, q.getX() + 0.5, q.getY() + 0.6, q.getZ() + 0.5, 2, 0.25, 0.2, 0.25, 0);
            }
        }
    }

    /** Lâmpada da Fertilidade: faz animais adultos próximos procriarem (até 12 de cada tipo). */
    public static class FertilityLamp extends Ticking {
        public FertilityLamp(Properties p) {
            super(300, p);
        }

        @Override
        protected void work(ServerLevel level, BlockPos pos, RandomSource r) {
            int rad = Config.get(Config.GROWTH_RADIUS);
            List<Animal> all = level.getEntitiesOfClass(Animal.class, new AABB(pos).inflate(rad, 3, rad), Animal::isAlive);
            Map<net.minecraft.world.entity.EntityType<?>, List<Animal>> by = new HashMap<>();
            for (Animal a : all) by.computeIfAbsent(a.getType(), t -> new ArrayList<>()).add(a);
            for (List<Animal> list : by.values()) {
                if (list.size() < 2 || list.size() >= 12) continue;
                int done = 0;
                for (Animal a : list) {
                    if (done >= 2) break;
                    if (a.getAge() == 0 && a.canFallInLove()) {
                        a.setInLove(null);
                        done++;
                    }
                }
                if (done == 1) list.stream().filter(a -> a.getAge() == 0 && !a.isInLove()).findFirst().ifPresent(a -> a.setInLove(null));
            }
        }
    }

    /** Levitador do Vendaval: empurra quem estiver acima (até 10 blocos). Agache para descer devagar. */
    public static class Levitator extends Ticking {
        public Levitator(Properties p) {
            super(2, p);
        }

        @Override
        protected void work(ServerLevel level, BlockPos pos, RandomSource r) {
            int h = 1;
            while (h <= 10 && !level.getBlockState(pos.above(h)).isCollisionShapeFullBlock(level, pos.above(h))) h++;
            if (h <= 1) return;
            AABB box = new AABB(pos.getX(), pos.getY() + 1, pos.getZ(), pos.getX() + 1, pos.getY() + h, pos.getZ() + 1);
            for (Entity e : level.getEntitiesOfClass(Entity.class, box, e -> e instanceof LivingEntity || e instanceof ItemEntity)) {
                Vec3 v = e.getDeltaMovement();
                double y = e.isShiftKeyDown() ? Math.max(v.y, -0.12) : Math.min(v.y + 0.18, 0.45);
                e.setDeltaMovement(v.x, y, v.z);
                e.resetFallDistance();
                e.hurtMarked = true;
            }
            if (r.nextInt(3) == 0) level.sendParticles(ParticleTypes.CLOUD, pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5, 1, 0.25, 0, 0.25, 0.05);
        }
    }

    /** Pedra de Proteção: impede monstros de nascerem naturalmente ao redor. */
    public static class WardingStone extends Ticking {
        private static final Map<ResourceKey<Level>, Set<BlockPos>> WARDS = new HashMap<>();

        public WardingStone(Properties p) {
            super(100, p);
        }

        @Override
        protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState old, boolean moving) {
            super.onPlace(state, level, pos, old, moving);
            if (!level.isClientSide) WARDS.computeIfAbsent(level.dimension(), k -> new HashSet<>()).add(pos.immutable());
        }

        @Override
        protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState ns, boolean moving) {
            if (!state.is(ns.getBlock()) && !level.isClientSide) {
                Set<BlockPos> s = WARDS.get(level.dimension());
                if (s != null) s.remove(pos);
            }
            super.onRemove(state, level, pos, ns, moving);
        }

        @Override
        protected void work(ServerLevel level, BlockPos pos, RandomSource r) {
            WARDS.computeIfAbsent(level.dimension(), k -> new HashSet<>()).add(pos.immutable());
            level.sendParticles(ParticleTypes.END_ROD, pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5, 2, 0.2, 0.2, 0.2, 0.01);
        }

        public static boolean warded(Level level, BlockPos at) {
            Set<BlockPos> s = WARDS.get(level.dimension());
            if (s == null || s.isEmpty()) return false;
            int rad = Config.get(Config.WARD_RADIUS);
            long r2 = (long) rad * rad;
            for (BlockPos w : s) if (w.distSqr(at) <= r2) return true;
            return false;
        }
    }

    @SubscribeEvent
    public static void onSpawn(FinalizeSpawnEvent e) {
        if (!(e.getEntity() instanceof Enemy)) return;
        MobSpawnType t = e.getSpawnType();
        if (t != MobSpawnType.NATURAL && t != MobSpawnType.CHUNK_GENERATION && t != MobSpawnType.PATROL) return;
        if (WardingStone.warded(e.getEntity().level(), e.getEntity().blockPosition())) e.setSpawnCancelled(true);
    }

    private NewBlocks() {}
}
