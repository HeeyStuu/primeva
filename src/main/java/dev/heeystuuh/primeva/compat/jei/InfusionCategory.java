package dev.heeystuuh.primeva.compat.jei;

import dev.heeystuuh.primeva.ModItems;
import dev.heeystuuh.primeva.PrimevaMod;
import dev.heeystuuh.primeva.Research;
import dev.heeystuuh.primeva.recipe.InfusionRecipe;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/** Categoria "Infusão" no JEI: pedestais em círculo ao redor do item central, como no altar. */
public class InfusionCategory implements IRecipeCategory<InfusionRecipe> {
    public static final RecipeType<InfusionRecipe> TYPE = RecipeType.create(PrimevaMod.MODID, "infusion", InfusionRecipe.class);
    private static final ResourceLocation SHEET = PrimevaMod.id("textures/gui/grimoire.png");
    private static final int W = 170, H = 128, CX = 56, CY = 52, R = 38;

    private final IDrawable icon;

    public InfusionCategory(IGuiHelper gui) {
        this.icon = gui.createDrawableItemStack(new ItemStack(ModItems.ALTAR.get()));
    }

    @Override
    public RecipeType<InfusionRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.primeva.infusion");
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public int getWidth() {
        return W;
    }

    @Override
    public int getHeight() {
        return H;
    }

    private static int[] pedestalPos(int i, int n) {
        double a = -Math.PI / 2 + i * 2 * Math.PI / n;
        return new int[]{(int) Math.round(CX + Math.cos(a) * R) - 8, (int) Math.round(CY + Math.sin(a) * R) - 8};
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, InfusionRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, CX - 8, CY - 8).addIngredients(recipe.center);
        int n = recipe.pedestals.size();
        for (int i = 0; i < n; i++) {
            int[] p = pedestalPos(i, n);
            builder.addSlot(RecipeIngredientRole.INPUT, p[0], p[1]).addIngredients(recipe.pedestals.get(i));
        }
        builder.addSlot(RecipeIngredientRole.OUTPUT, W - 26, CY - 8).addItemStack(recipe.result).setOutputSlotBackground();
        builder.addSlot(RecipeIngredientRole.CATALYST, W - 26, CY + 18).addItemStack(new ItemStack(ModItems.ALTAR.get()));
    }

    @Override
    public void draw(InfusionRecipe recipe, IRecipeSlotsView slots, GuiGraphics g, double mouseX, double mouseY) {
        int n = recipe.pedestals.size();
        // fios de essência e molduras dos pedestais
        for (int i = 0; i < n; i++) {
            int[] p = pedestalPos(i, n);
            int px = p[0] + 8, py = p[1] + 8;
            for (int s = 2; s < 9; s++) {
                int lx = px + (CX - px) * s / 10, ly = py + (CY - py) * s / 10;
                g.fill(lx - 1, ly - 1, lx + 1, ly + 1, 0xFF9050D0);
            }
            g.blit(SHEET, px - 10, py - 10, 20, 20, 770, 200, 40, 40, 1024, 512);
        }
        g.blit(SHEET, CX - 13, CY - 13, 26, 26, 815, 200, 52, 52, 1024, 512);
        // seta
        g.blit(SHEET, W - 42, CY - 6, 12, 12, 798, 170, 24, 24, 1024, 512);

        Font font = Minecraft.getInstance().font;
        Research r = Research.get(recipe.research);
        Component res = Component.translatable("jei.primeva.research", r != null ? r.title() : Component.literal(recipe.research));
        g.drawString(font, res, 2, H - 30, 0xFF5A2A8A, false);
        Component tier = Component.translatable("gui.primeva.tier." + recipe.tier);
        g.drawString(font, tier, 2, H - 20, recipe.tier >= 3 ? 0xFF9A2A1A : recipe.tier == 2 ? 0xFF8A5A10 : 0xFF404040, false);
        g.drawString(font, Component.translatable("gui.primeva.instability_label"), 2, H - 10, 0xFF404040, false);
        int fx = 2 + font.width(Component.translatable("gui.primeva.instability_label")) + 2;
        for (int i = 0; i < Math.max(6, recipe.instability); i++) {
            g.blit(SHEET, fx + i * 9, H - 11, 9, 9, i < recipe.instability ? 875 : 897, 200, 20, 20, 1024, 512);
        }
    }
}
