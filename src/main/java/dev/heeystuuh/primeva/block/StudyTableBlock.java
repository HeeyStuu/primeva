package dev.heeystuuh.primeva.block;

import dev.heeystuuh.primeva.Essence;
import dev.heeystuuh.primeva.Knowledge;
import dev.heeystuuh.primeva.ModAttachments;
import dev.heeystuuh.primeva.item.CrystalItem;
import dev.heeystuuh.primeva.item.PhialItem;
import dev.heeystuuh.primeva.network.Network;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import com.mojang.serialization.MapCodec;

/**
 * Mesa de Estudos: transforma essência em percepção.
 * Frasco (5 de essência) -> +3 de percepção.  Cristal -> +25 de percepção.
 */
public class StudyTableBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<StudyTableBlock> CODEC = simpleCodec(StudyTableBlock::new);
    private static final VoxelShape SHAPE = Shapes.or(Block.box(0, 12, 0, 16, 15, 16),
            Block.box(1, 0, 1, 4, 12, 4), Block.box(12, 0, 1, 15, 12, 4), Block.box(1, 0, 12, 4, 12, 15), Block.box(12, 0, 12, 15, 12, 15));

    public StudyTableBlock(Properties props) {
        super(props);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite());
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return SHAPE;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
                                              InteractionHand hand, BlockHitResult hit) {
        Essence e;
        int gain;
        if (stack.getItem() instanceof PhialItem ph) {
            e = ph.essence;
            gain = 3;
        } else if (stack.getItem() instanceof CrystalItem cr) {
            e = cr.essence;
            gain = 25;
        } else {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (!(player instanceof ServerPlayer sp)) return ItemInteractionResult.SUCCESS;
        Knowledge k = sp.getData(ModAttachments.KNOWLEDGE);
        if (!k.research.contains("study")) {
            sp.displayClientMessage(Component.translatable("msg.primeva.need_study").withStyle(ChatFormatting.RED), true);
            return ItemInteractionResult.FAIL;
        }
        int count = player.isShiftKeyDown() ? stack.getCount() : 1;
        int total = gain * count;
        k.add(e, total);
        if (!player.getAbilities().instabuild) stack.shrink(count);
        sp.displayClientMessage(Component.literal("+" + total + " ").withStyle(e.fmt).append(e.displayName()), true);
        ((ServerLevel) level).sendParticles(ParticleTypes.ENCHANT, pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5, 30, 0.3, 0.3, 0.3, 0.5);
        level.playSound(null, pos, SoundEvents.BOOK_PAGE_TURN, SoundSource.BLOCKS, 1f, 0.9f);
        level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 0.6f, 1.4f);
        Network.sync(sp);
        return ItemInteractionResult.CONSUME;
    }
}
