package dev.heeystuuh.primeva.item;

import dev.heeystuuh.primeva.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.function.Supplier;

/** Elixir: bebe e devolve o frasco vazio. */
public class ElixirItem extends Item {
    private final Supplier<List<MobEffectInstance>> effects;
    private final String key;

    public ElixirItem(String key, Supplier<List<MobEffectInstance>> effects, Properties props) {
        super(props);
        this.key = key;
        this.effects = effects;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 32;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return ItemUtils.startUsingInstantly(level, player, hand);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide) for (MobEffectInstance e : effects.get()) entity.addEffect(new MobEffectInstance(e));
        if (entity instanceof Player p && !p.getAbilities().instabuild) {
            stack.shrink(1);
            ItemStack empty = new ItemStack(ModItems.EMPTY_PHIAL.get());
            if (stack.isEmpty()) return empty;
            if (!p.getInventory().add(empty)) p.drop(empty, false);
        }
        return stack;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.primeva." + key).withStyle(ChatFormatting.LIGHT_PURPLE));
    }
}
