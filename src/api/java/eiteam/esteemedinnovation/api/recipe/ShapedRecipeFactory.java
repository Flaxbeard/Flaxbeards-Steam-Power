package eiteam.esteemedinnovation.api.recipe;

import com.google.gson.JsonObject;
import eiteam.esteemedinnovation.api.book.BookRecipeRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.IRecipeFactory;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraftforge.oredict.ShapedOreRecipe;

public class ShapedRecipeFactory implements IRecipeFactory {

    /**
     * Creates a {@link ShapedRecipe} based on the {@param context} and {@param json}
     * @param context The {@link JsonContext}
     * @param json The {@link JsonObject}
     * @return The new {@link ShapedRecipe}
     */
    @Override
    public IRecipe parse(JsonContext context, JsonObject json) {
        String group = JsonUtils.getString(json, "group", "");
        ShapedOreRecipe recipe = ShapedOreRecipe.factory(context, json);

        CraftingHelper.ShapedPrimer primer = new CraftingHelper.ShapedPrimer();
        primer.width = recipe.getRecipeWidth();
        primer.height = recipe.getRecipeHeight();
        primer.mirrored = JsonUtils.getBoolean(json, "mirrored", true);
        primer.input = recipe.getIngredients();

        ShapedRecipe shapedRecipe = new ShapedRecipe(group.isEmpty() ? null : new ResourceLocation(group), recipe.getRecipeOutput(), primer);

        String bookKey = JsonUtils.getString(json, "book_key", "");
        if (!bookKey.equals("")) {
            BookRecipeRegistry.addRecipe(bookKey, shapedRecipe);
        }

        return shapedRecipe;
    }

    public static class ShapedRecipe extends ShapedOreRecipe {
        public ShapedRecipe(ResourceLocation group, ItemStack result, CraftingHelper.ShapedPrimer primer) {
            super(group, result, primer);
        }
    }
}