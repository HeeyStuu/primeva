package dev.heeystuuh.primeva.compat.jei;

import dev.heeystuuh.primeva.Essence;
import dev.heeystuuh.primeva.ModItems;
import dev.heeystuuh.primeva.ModRecipes;
import dev.heeystuuh.primeva.PrimevaMod;
import dev.heeystuuh.primeva.recipe.CrucibleRecipe;
import dev.heeystuuh.primeva.recipe.InfusionRecipe;
import dev.heeystuuh.primeva.item.FocusItem;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.ArrayList;
import java.util.List;

/** Integração com o JEI: mostra as Infusões e explica como conseguir os itens sem receita de bancada. */
@JeiPlugin
public class PrimevaJeiPlugin implements IModPlugin {
    @Override
    public ResourceLocation getPluginUid() {
        return PrimevaMod.id("jei");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration reg) {
        reg.addRecipeCategories(new InfusionCategory(reg.getJeiHelpers().getGuiHelper()));
        reg.addRecipeCategories(new CrucibleCategory(reg.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration reg) {
        List<InfusionRecipe> list = new ArrayList<>();
        if (Minecraft.getInstance().level != null) {
            for (RecipeHolder<InfusionRecipe> h : Minecraft.getInstance().level.getRecipeManager().getAllRecipesFor(ModRecipes.INFUSION_TYPE.get())) {
                list.add(h.value());
            }
        }
        reg.addRecipes(InfusionCategory.TYPE, list);
        List<CrucibleRecipe> cru = new ArrayList<>();
        if (Minecraft.getInstance().level != null) {
            for (RecipeHolder<CrucibleRecipe> h : Minecraft.getInstance().level.getRecipeManager().getAllRecipesFor(ModRecipes.CRUCIBLE_TYPE.get())) {
                cru.add(h.value());
            }
        }
        reg.addRecipes(CrucibleCategory.TYPE, cru);
        reg.addItemStackInfo(new ItemStack(ModItems.CRUCIBLE.get()), Component.translatable("jei.primeva.info.crucible"));
        reg.addItemStackInfo(new ItemStack(ModItems.PRIMORDIAL_SALT.get()), Component.translatable("jei.primeva.info.salt"));
        reg.addItemStackInfo(new ItemStack(ModItems.HUNGRY_CHEST.get()), Component.translatable("jei.primeva.info.hungry_chest"));
        reg.addItemStackInfo(new ItemStack(ModItems.ARCANE_LAMP.get()), Component.translatable("jei.primeva.info.arcane_lamp"));
        reg.addItemStackInfo(new ItemStack(ModItems.RUNIC_RING.get()), Component.translatable("jei.primeva.info.runic_ring"));
        reg.addItemStackInfo(new ItemStack(ModItems.REVEALING_GOGGLES.get()), Component.translatable("jei.primeva.info.goggles"));
        reg.addItemStackInfo(List.of(new ItemStack(ModItems.QUICKSILVER.get()), new ItemStack(ModItems.VERMILION_ORE.get()), new ItemStack(ModItems.DEEPSLATE_VERMILION_ORE.get())),
                Component.translatable("jei.primeva.info.vermilion"));
        reg.addItemStackInfo(List.of(new ItemStack(ModItems.AMBER.get()), new ItemStack(ModItems.AMBER_ORE.get())), Component.translatable("jei.primeva.info.amber"));
        List<ItemStack> shards = new ArrayList<>();
        for (Essence e : Essence.values()) {
            shards.add(new ItemStack(ModItems.shard(e)));
            shards.add(new ItemStack(ModItems.CLUSTER_ITEMS.get(e).get()));
        }
        reg.addItemStackInfo(shards, Component.translatable("jei.primeva.info.shards"));
        reg.addItemStackInfo(List.of(new ItemStack(ModItems.ARGENT_SAPLING.get()), new ItemStack(ModItems.ELDER_SAPLING.get())), Component.translatable("jei.primeva.info.trees"));
        reg.addItemStackInfo(List.of(new ItemStack(ModItems.ARCANE_GOLEM.get()), new ItemStack(ModItems.SEAL_HARVEST.get()),
                new ItemStack(ModItems.SEAL_PICKUP.get()), new ItemStack(ModItems.SEAL_GUARD.get())), Component.translatable("jei.primeva.info.golem"));
        reg.addItemStackInfo(List.of(new ItemStack(ModItems.CRIMSON_CLOTH.get()), new ItemStack(ModItems.CRIMSON_SCYTHE.get()), new ItemStack(ModItems.CRIMSON_BLADE.get()), new ItemStack(ModItems.CRIMSON_RITES.get()),
                new ItemStack(ModItems.CRIMSON_HELMET.get()), new ItemStack(ModItems.CRIMSON_CHESTPLATE.get()), new ItemStack(ModItems.CRIMSON_LEGGINGS.get()),
                new ItemStack(ModItems.CRIMSON_BOOTS.get())), Component.translatable("jei.primeva.info.crimson"));
        reg.addItemStackInfo(List.of(new ItemStack(ModItems.LOOT_BAG_COMMON.get()), new ItemStack(ModItems.LOOT_BAG_UNCOMMON.get()), new ItemStack(ModItems.LOOT_BAG_RARE.get()),
                new ItemStack(ModItems.CURIO_ANCIENT.get()), new ItemStack(ModItems.CURIO_ABYSS.get()), new ItemStack(ModItems.CURIO_GEAR.get()),
                new ItemStack(ModItems.CURIO_SPECIMEN.get())), Component.translatable("jei.primeva.info.loot"));
        reg.addItemStackInfo(List.of(new ItemStack(ModItems.ESSENCE_JAR.get()), new ItemStack(ModItems.ESSENCE_TUBE.get())), Component.translatable("jei.primeva.info.jar"));
        reg.addItemStackInfo(List.of(new ItemStack(ModItems.HERALD_SIGIL.get()), new ItemStack(ModItems.CRIMSON_SCEPTER.get())), Component.translatable("jei.primeva.info.herald"));
        reg.addItemStackInfo(List.of(new ItemStack(ModItems.SEAL_LUMBER.get()), new ItemStack(ModItems.SEAL_STOCK.get())), Component.translatable("jei.primeva.info.golem"));
        List<ItemStack> foci = new ArrayList<>();
        for (FocusItem.Kind k : FocusItem.Kind.values()) foci.add(new ItemStack(ModItems.FOCI.get(k).get()));
        reg.addItemStackInfo(foci, Component.translatable("jei.primeva.info.foci"));
        reg.addItemStackInfo(List.of(new ItemStack(ModItems.VOIDSTEEL_SWORD.get()), new ItemStack(ModItems.VOIDSTEEL_PICKAXE.get()),
                new ItemStack(ModItems.VOIDSTEEL_AXE.get()), new ItemStack(ModItems.VOIDSTEEL_SHOVEL.get())), Component.translatable("jei.primeva.info.voidsteel"));

        List<ItemStack> phials = new ArrayList<>();
        for (Essence e : Essence.values()) phials.add(new ItemStack(ModItems.phial(e)));
        reg.addItemStackInfo(phials, Component.translatable("jei.primeva.info.phial"));
        reg.addItemStackInfo(new ItemStack(ModItems.DISTILLER.get()), Component.translatable("jei.primeva.info.distiller"));
        reg.addItemStackInfo(new ItemStack(ModItems.CORRUPTED_SOIL.get()), Component.translatable("jei.primeva.info.corrupted_soil"));
        reg.addItemStackInfo(new ItemStack(ModItems.LENS.get()), Component.translatable("jei.primeva.info.lens"));
        reg.addItemStackInfo(new ItemStack(ModItems.ALTAR.get()), Component.translatable("jei.primeva.info.altar"));
        reg.addItemStackInfo(new ItemStack(ModItems.CONTAINMENT_PILLAR.get()), Component.translatable("jei.primeva.info.pillar"));
        reg.addItemStackInfo(new ItemStack(ModItems.CATALYST.get()), Component.translatable("jei.primeva.info.catalyst"));
        reg.addItemStackInfo(new ItemStack(ModItems.STUDY_TABLE.get()), Component.translatable("jei.primeva.info.study"));
        List<ItemStack> wands = new ArrayList<>();
        for (Essence e : Essence.values()) wands.add(new ItemStack(ModItems.wand(e)));
        reg.addItemStackInfo(wands, Component.translatable("jei.primeva.info.wand"));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration reg) {
        reg.addRecipeCatalyst(new ItemStack(ModItems.ALTAR.get()), InfusionCategory.TYPE);
        reg.addRecipeCatalyst(new ItemStack(ModItems.PEDESTAL.get()), InfusionCategory.TYPE);
        reg.addRecipeCatalyst(new ItemStack(ModItems.CONTAINMENT_PILLAR.get()), InfusionCategory.TYPE);
        reg.addRecipeCatalyst(new ItemStack(ModItems.CATALYST.get()), InfusionCategory.TYPE);
        reg.addRecipeCatalyst(new ItemStack(ModItems.CRUCIBLE.get()), CrucibleCategory.TYPE);
    }
}
