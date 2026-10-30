package dev.heeystuuh.primeva.block;

import dev.heeystuuh.primeva.Miasma;
import dev.heeystuuh.primeva.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

/**
 * Solo Miasmático. Se espalha enquanto houver muito miasma e volta a ser terra quando o chunk fica limpo.
 * O solo "enraizado" (dos focos de corrupção do mundo) não some sozinho: ele alimenta o miasma da região
 * até ser purificado por um Farol Purificador ou Incenso Purificador.
 */
public class CorruptedSoilBlock extends Block {
    public static final BooleanProperty ROOTED = BooleanProperty.create("rooted");

    public CorruptedSoilBlock(Properties props) {
        super(props);
        registerDefaultState(stateDefinition.any().setValue(ROOTED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) {
        b.add(ROOTED);
    }

    public static void purify(Level level, BlockPos pos) {
        level.setBlockAndUpdate(pos, Blocks.DIRT.defaultBlockState());
        if (level instanceof ServerLevel sl) sl.sendParticles(ParticleTypes.END_ROD, pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5, 4, 0.2, 0.1, 0.2, 0.01);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int m = Miasma.get(level, pos);
        if (state.getValue(ROOTED)) {
            // foco natural: mantém a região contaminada (até um limite)
            if (m < 35 && random.nextInt(6) == 0) Miasma.addRaw(level, pos, 1);
        } else if (m == 0) {
            level.setBlockAndUpdate(pos, Blocks.DIRT.defaultBlockState());
            return;
        }
        if (m >= 30 && random.nextInt(3) == 0) {
            BlockPos t = pos.offset(random.nextInt(3) - 1, random.nextInt(3) - 1, random.nextInt(3) - 1);
            BlockState ts = level.getBlockState(t);
            if (ts.is(BlockTags.DIRT) && !ts.is(this) && level.getBlockState(t.above()).isAir()) level.setBlockAndUpdate(t, ModBlocks.CORRUPTED_SOIL.get().defaultBlockState());
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(state.getValue(ROOTED) ? 3 : 6) == 0) {
            level.addParticle(ParticleTypes.WITCH, pos.getX() + random.nextDouble(), pos.getY() + 1.05, pos.getZ() + random.nextDouble(), 0, 0.02, 0);
        }
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        if (!level.isClientSide && entity instanceof Player p && level.random.nextInt(60) == 0) {
            p.addEffect(new MobEffectInstance(MobEffects.HUNGER, 100, 0));
        }
        if (!level.isClientSide && state.getValue(ROOTED) && entity instanceof net.minecraft.server.level.ServerPlayer sp && level.random.nextInt(20) == 0) {
            dev.heeystuuh.primeva.Discovery.grant(sp, "do:corruption_site");
        }
        super.stepOn(level, pos, state, entity);
    }
}
