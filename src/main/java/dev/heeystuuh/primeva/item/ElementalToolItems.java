package dev.heeystuuh.primeva.item;

import dev.heeystuuh.primeva.Essence;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/** Ferramentas Elementais: a lógica de quebra em área/veio fica em CommonEvents. */
public final class ElementalToolItems {
    private static void tip(List<Component> t, String key, Essence e) {
        t.add(Component.translatable("tooltip.primeva." + key).withStyle(e.fmt));
        t.add(Component.translatable("tooltip.primeva.sneak_disable").withStyle(ChatFormatting.DARK_GRAY));
    }

    public static class Pick extends PickaxeItem {
        public Pick(Tier t, Properties p) { super(t, p); }
        @Override
        public void appendHoverText(ItemStack s, TooltipContext c, List<Component> t, TooltipFlag f) { tip(t, "ember_pickaxe", Essence.EMBER); }
    }

    public static class Axe extends AxeItem {
        public Axe(Tier t, Properties p) { super(t, p); }
        @Override
        public void appendHoverText(ItemStack s, TooltipContext c, List<Component> t, TooltipFlag f) { tip(t, "tide_axe", Essence.TIDE); }
    }

    public static class Shovel extends ShovelItem {
        public Shovel(Tier t, Properties p) { super(t, p); }
        @Override
        public void appendHoverText(ItemStack s, TooltipContext c, List<Component> t, TooltipFlag f) { tip(t, "terra_shovel", Essence.TERRA); }
    }

    /** Espada do Vendaval: arremessa quem ela acerta; clique direito solta uma rajada. */
    public static class Sword extends SwordItem {
        public Sword(Tier t, Properties p) { super(t, p); }

        @Override
        public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
            Vec3 d = target.position().subtract(attacker.position()).normalize();
            target.push(d.x * 0.9, 0.45, d.z * 0.9);
            target.hurtMarked = true;
            return super.hurtEnemy(stack, target, attacker);
        }

        @Override
        public InteractionResultHolder<ItemStack> use(Level level, Player p, InteractionHand hand) {
            ItemStack stack = p.getItemInHand(hand);
            if (level instanceof ServerLevel sl) {
                for (LivingEntity e : sl.getEntitiesOfClass(LivingEntity.class, p.getBoundingBox().inflate(5), e -> e != p && e.isAlive())) {
                    Vec3 d = e.position().subtract(p.position()).normalize();
                    e.push(d.x * 1.6, 0.6, d.z * 1.6);
                    e.hurtMarked = true;
                }
                sl.sendParticles(ParticleTypes.GUST, p.getX(), p.getY() + 1, p.getZ(), 6, 1.5, 0.3, 1.5, 0);
                sl.playSound(null, p.blockPosition(), SoundEvents.BREEZE_SHOOT, SoundSource.PLAYERS, 1f, 1f);
                stack.hurtAndBreak(2, p, LivingEntity.getSlotForHand(hand));
                p.getCooldowns().addCooldown(this, 40);
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }

        @Override
        public void appendHoverText(ItemStack s, TooltipContext c, List<Component> t, TooltipFlag f) {
            t.add(Component.translatable("tooltip.primeva.gale_sword").withStyle(Essence.GALE.fmt));
        }
    }

    /** Enxada do Lume: colhe e replanta plantações maduras num 5x5; senão ara 3x3. */
    public static class Hoe extends HoeItem {
        public Hoe(Tier t, Properties p) { super(t, p); }

        @Override
        public InteractionResult useOn(UseOnContext ctx) {
            Level level = ctx.getLevel();
            BlockPos c = ctx.getClickedPos();
            Player p = ctx.getPlayer();
            int harvested = 0;
            for (BlockPos pos : BlockPos.betweenClosed(c.offset(-2, -1, -2), c.offset(2, 1, 2))) {
                BlockState st = level.getBlockState(pos);
                if (st.getBlock() instanceof CropBlock crop && crop.isMaxAge(st)) {
                    if (level instanceof ServerLevel sl) {
                        List<ItemStack> drops = Block.getDrops(st, sl, pos, null, p, ctx.getItemInHand());
                        boolean replanted = false;
                        for (ItemStack d : drops) {
                            if (!replanted && d.getItem() == crop.getCloneItemStack(sl, pos, st).getItem()) {
                                d.shrink(1);
                                replanted = true;
                            }
                            Block.popResource(sl, pos, d);
                        }
                        sl.setBlockAndUpdate(pos, crop.getStateForAge(0));
                        sl.sendParticles(new DustParticleOptions(Essence.LUMEN.color(), 0.8f), pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 3, 0.3, 0.3, 0.3, 0);
                    }
                    harvested++;
                }
            }
            if (harvested > 0) {
                level.playSound(null, c, SoundEvents.CROP_BREAK, SoundSource.BLOCKS, 1f, 1f);
                if (p != null) ctx.getItemInHand().hurtAndBreak(1, p, LivingEntity.getSlotForHand(ctx.getHand()));
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
            int tilled = 0;
            for (BlockPos pos : BlockPos.betweenClosed(c.offset(-1, 0, -1), c.offset(1, 0, 1))) {
                BlockState st = level.getBlockState(pos);
                if ((st.is(Blocks.DIRT) || st.is(Blocks.GRASS_BLOCK) || st.is(Blocks.DIRT_PATH)) && level.getBlockState(pos.above()).isAir()) {
                    if (!level.isClientSide) level.setBlockAndUpdate(pos, Blocks.FARMLAND.defaultBlockState());
                    tilled++;
                }
            }
            if (tilled > 0) {
                level.playSound(p, c, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1f, 1f);
                if (p != null && !level.isClientSide) ctx.getItemInHand().hurtAndBreak(1, p, LivingEntity.getSlotForHand(ctx.getHand()));
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
            return super.useOn(ctx);
        }

        @Override
        public void appendHoverText(ItemStack s, TooltipContext c, List<Component> t, TooltipFlag f) {
            t.add(Component.translatable("tooltip.primeva.lumen_hoe").withStyle(Essence.LUMEN.fmt));
        }
    }

    private ElementalToolItems() {}
}
