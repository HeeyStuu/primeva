package dev.heeystuuh.primeva.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.EvokerFangs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/** Cetro Carmesim: invoca uma fileira de presas que saem do chão na direção em que você olha. */
public class CrimsonScepterItem extends Item {
    public CrimsonScepterItem(Properties p) {
        super(p.durability(250));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player p, InteractionHand hand) {
        ItemStack stack = p.getItemInHand(hand);
        if (level instanceof ServerLevel sl) {
            float yaw = p.getYRot() * Mth.DEG_TO_RAD;
            double dx = -Mth.sin(yaw), dz = Mth.cos(yaw);
            for (int i = 1; i <= 12; i++) {
                double x = p.getX() + dx * i * 1.25, z = p.getZ() + dz * i * 1.25;
                BlockPos col = BlockPos.containing(x, p.getY() + 2, z);
                for (int k = 0; k < 6; k++) {
                    if (!sl.getBlockState(col.below()).isAir() || col.getY() <= sl.getMinBuildHeight()) break;
                    col = col.below();
                }
                sl.addFreshEntity(new EvokerFangs(sl, x, col.getY(), z, p.getYRot() * Mth.DEG_TO_RAD, i, p));
            }
            sl.playSound(null, p.blockPosition(), SoundEvents.EVOKER_CAST_SPELL, SoundSource.PLAYERS, 1f, 1f);
            stack.hurtAndBreak(1, p, LivingEntity.getSlotForHand(hand));
            p.getCooldowns().addCooldown(this, 30);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack s, TooltipContext c, List<Component> t, TooltipFlag f) {
        t.add(Component.translatable("tooltip.primeva.crimson_scepter").withStyle(ChatFormatting.DARK_RED));
    }
}
