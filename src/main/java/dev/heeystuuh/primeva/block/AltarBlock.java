package dev.heeystuuh.primeva.block;

import com.mojang.serialization.MapCodec;
import dev.heeystuuh.primeva.ModBlockEntities;
import dev.heeystuuh.primeva.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Altar de Infusão: item no centro, oferendas nos pedestais em volta, ative com o Grimório. */
public class AltarBlock extends BaseEntityBlock {
    public static final MapCodec<AltarBlock> CODEC = simpleCodec(AltarBlock::new);
    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(0, 0, 0, 16, 4, 16), Block.box(3, 4, 3, 13, 12, 13), Block.box(1, 12, 1, 15, 16, 15));

    public static final net.minecraft.world.level.block.state.properties.BooleanProperty ACTIVE =
            net.minecraft.world.level.block.state.properties.BooleanProperty.create("active");

    public AltarBlock(Properties props) {
        super(props);
        registerDefaultState(stateDefinition.any().setValue(ACTIVE, false));
    }

    @Override
    protected void createBlockStateDefinition(net.minecraft.world.level.block.state.StateDefinition.Builder<Block, BlockState> b) {
        b.add(ACTIVE);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, net.minecraft.util.RandomSource rnd) {
        if (state.getValue(ACTIVE)) {
            for (int i = 0; i < 2; i++) level.addParticle(net.minecraft.core.particles.ParticleTypes.PORTAL,
                    pos.getX() + 0.5 + (rnd.nextDouble() - 0.5) * 2, pos.getY() + 0.5 + rnd.nextDouble(), pos.getZ() + 0.5 + (rnd.nextDouble() - 0.5) * 2, 0, 0.1, 0);
        }
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return SHAPE;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AltarBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, ModBlockEntities.ALTAR.get(), AltarBlockEntity::serverTick);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
                                              InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof AltarBlockEntity be)) return ItemInteractionResult.FAIL;
        if (stack.is(ModItems.GRIMOIRE.get())) {
            if (player instanceof ServerPlayer sp) be.tryStart(sp);
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        if (!be.getCenter().isEmpty() || be.isActive()) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if (!level.isClientSide) {
            be.setCenter(stack.copyWithCount(1));
            if (!player.getAbilities().instabuild) stack.shrink(1);
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof AltarBlockEntity be) || be.getCenter().isEmpty() || be.isActive()) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide) {
            ItemStack out = be.getCenter();
            be.setCenter(ItemStack.EMPTY);
            if (!player.getInventory().add(out)) player.drop(out, false);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof AltarBlockEntity be) {
            be.abort();
            if (!be.getCenter().isEmpty()) Block.popResource(level, pos, be.getCenter());
        }
        super.onRemove(state, level, pos, newState, moved);
    }
}
