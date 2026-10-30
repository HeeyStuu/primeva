package dev.heeystuuh.primeva.compat.jei;

import dev.heeystuuh.primeva.Essence;
import dev.heeystuuh.primeva.ModItems;
import dev.heeystuuh.primeva.PrimevaMod;
import dev.heeystuuh.primeva.Research;
import dev.heeystuuh.primeva.recipe.CrucibleRecipe;
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

import java.util.Map;

/** Categoria "Cadinho Arcano" no JEI. */
public class CrucibleCategory implements IRecipeCategory<CrucibleRecipe> {
    public static final RecipeType<CrucibleRecipe> TYPE = RecipeType.create(PrimevaMod.MODID, "crucible", CrucibleRecipe.class);
    private static final ResourceLocation SHEET = PrimevaMod.id("textures/gui/grimoire.png");
    private static final ResourceLocation ESS = PrimevaMod.id("textures/gui/essences.png");
    private static final int W = 150, H = 64;
    private final IDrawable icon;

    public CrucibleCategory(IGuiHelper gui) {
        icon = gui.createDrawableItemStack(new ItemStack(ModItems.CRUCIBLE.get()));
    }

    @Override
    public RecipeType<CrucibleRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.primeva.crucible");
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

    @Override
    public void setRecipe(IRecipeLayoutBuilder b, CrucibleRecipe r, IFocusGroup focuses) {
        b.addSlot(RecipeIngredientRole.INPUT, 6, 6).addIngredients(r.catalyst);
        b.addSlot(RecipeIngredientRole.CATALYST, 38, 6).addItemStack(new ItemStack(ModItems.CRUCIBLE.get()));
        b.addSlot(RecipeIngredientRole.OUTPUT, W - 24, 6).addItemStack(r.result).setOutputSlotBackground();
    }

    @Override
    public void draw(CrucibleRecipe r, IRecipeSlotsView slots, GuiGraphics g, double mx, double my) {
        Font font = Minecraft.getInstance().font;
        g.drawString(font, "+", 26, 10, 0xFF404040, false);
        g.blit(SHEET, W - 46, 8, 12, 12, 798, 170, 24, 24, 1024, 512);
        int x = 4, y = 28;
        for (Map.Entry<Essence, Integer> e : r.essences.entrySet()) {
            String txt = "x" + e.getValue();
            int w = 13 + font.width(txt) + 6;
            if (x + w > W) {
                x = 4;
                y += 12;
            }
            g.blit(ESS, x, y, 11, 11, e.getKey().ordinal() * 32, 0, 32, 32, 192, 32);
            g.drawString(font, txt, x + 13, y + 2, 0xFF404040, false);
            x += w;
        }
        if (!r.research.isEmpty()) {
            Research res = Research.get(r.research);
            g.drawString(font, Component.translatable("jei.primeva.research", res != null ? res.title() : Component.literal(r.research)), 2, H - 10, 0xFF5A2A8A, false);
        }
    }
}
