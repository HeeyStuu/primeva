package dev.heeystuuh.primeva.item;

import dev.heeystuuh.primeva.ModItems;
import dev.heeystuuh.primeva.Warp;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

import java.util.List;

/** Tônico da Lucidez: beber limpa a mente (toda a marca temporária e 1 da permanente). */
public class LucidityTonicItem extends Item {
    public LucidityTonicItem(Properties p) {
        super(p);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 40;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player p, InteractionHand hand) {
        return ItemUtils.startUsingInstantly(level, p, hand);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity e) {
        if (e instanceof ServerPlayer sp) {
            Warp.cleanse(sp);
            level.playSound(null, sp.blockPosition(), SoundEvents.AMETHYST_BLOCK_RESONATE, net.minecraft.sounds.SoundSource.PLAYERS, 1f, 1.4f);
            if (!sp.getAbilities().instabuild) {
                stack.shrink(1);
                ItemStack empty = new ItemStack(ModItems.EMPTY_PHIAL.get());
                if (stack.isEmpty()) return empty;
                if (!sp.getInventory().add(empty)) sp.drop(empty, false);
            }
        }
        return stack;
    }

    @Override
    public void appendHoverText(ItemStack s, TooltipContext c, List<Component> t, TooltipFlag f) {
        t.add(Component.translatable("tooltip.primeva.lucidity_tonic").withStyle(ChatFormatting.GRAY));
    }
}
