package eiteam.esteemedinnovation.api.crucible;

import eiteam.esteemedinnovation.api.util.ItemStackUtility;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// TODO: Much of this can be replaced with JSON.
public class CrucibleRegistry {
    /**
     * All of the CrucibleLiquids that the mod knows about.
     */
    public static List<CrucibleLiquid> liquids = new ArrayList<>();

    /**
     * All of the CrucibleFormulas
     */
    public static List<CrucibleFormula> alloyFormulas = new ArrayList<>();

    /**
     * All of the CrucibleLiquid recipes.
     * <ul>
     *     <li>Key: The ingredient</li>
     *     <li>Value: pair of the liquid and the amount created.</li>
     * </ul>
     */
    public static Map<Ingredient, Pair<CrucibleLiquid, Integer>> liquidRecipes = new HashMap<>();

    /**
     * All of the Crucible dunking recipes.
     * <ul>
     *     <li>Key: A pair of the ingredient and the {@link CrucibleLiquid}.</li>
     *     <li>Value: A pair of the required liquid amount and the output ItemStack.</li>
     * </ul>
     */
    public static Map<Pair<Ingredient, CrucibleLiquid>, Pair<Integer, ItemStack>> dunkRecipes = new HashMap<>();

    /**
     * Gets the given CrucibleLiquid from the name.
     * @param name The liquid's name.
     * @return Null if it cannot find that liquid, otherwise, the liquid.
     */
    public static CrucibleLiquid getLiquidFromName(String name) {
        for (CrucibleLiquid liquid : liquids) {
            if (liquid.getName().equals(name)) {
                return liquid;
            }
        }
        return null;
    }

    /**
     * Registers a Crucible dunking recipe.
     * @param ingredient The item to dunk
     * @param liquid The input liquid.
     * @param liquidAmount The amount of liquid needed.
     * @param result The output ItemStack.
     */
    public static void registerDunkRecipe(Ingredient ingredient, CrucibleLiquid liquid, int liquidAmount, ItemStack result) {
        dunkRecipes.put(Pair.of(ingredient, liquid), Pair.of(liquidAmount, result));
    }

    /**
     * Registers a Crucible melting recipe.
     * @param ingredient The input item
     * @param liquid The output liquid.
     * @param m The output liquid amount.
     */
    public static void registerMeltRecipe(Ingredient ingredient, CrucibleLiquid liquid, int m) {
        liquidRecipes.put(ingredient, Pair.of(liquid, m));
    }

    /**
     * Sets the given CrucibleLiquid as an actual CrucibleLiquid that can be used.
     * @param liquid The liquid
     */
    public static void registerLiquid(CrucibleLiquid liquid) {
        liquids.add(liquid);
    }

    /**
     * Registers the provided CrucibleFormula
     * @param formula The formula
     */
    public static void registerFormula(CrucibleFormula formula) {
        alloyFormulas.add(formula);
    }

    /**
     * Finds all of the alloy formulas that result in the provided liquid.
     * @param resultLiquid The output liquid
     * @return A set of all matching formulas
     */
    public static List<CrucibleFormula> findRecipesThatResultInLiquid(CrucibleLiquid resultLiquid) {
        return alloyFormulas.stream().filter(formula -> formula.getOutputLiquid().equals(resultLiquid)).collect(Collectors.toList());
    }

    /**
     * The recipes for casting molds.
     * Key: Input liquid, input mold.
     * Value: Output item.
     */
    private static final Map<Pair<CrucibleLiquid, ItemStack>, ItemStack> moldingRecipes = new HashMap<>();

    /**
     * Registers a casting recipe.
     * @param inputLiquid The input liquid
     * @param mold The input mold
     * @param out The output
     */
    public static void registerMoldingRecipe(CrucibleLiquid inputLiquid, @Nonnull ItemStack mold, @Nonnull ItemStack out) {
        moldingRecipes.put(Pair.of(inputLiquid, mold), out);
    }

    /**
     * @param inputLiquid The input liquid
     * @param mold The input mold
     * @return The output ItemStack
     */
    @Nonnull
    public static ItemStack getMoldingOutput(CrucibleLiquid inputLiquid, @Nonnull ItemStack mold) {
        for (Map.Entry<Pair<CrucibleLiquid, ItemStack>, ItemStack> entry : moldingRecipes.entrySet()) {
            Pair<CrucibleLiquid, ItemStack> input = entry.getKey();
            if (ItemStackUtility.compareItemStacks(mold, input.getRight()) && input.getLeft().equals(inputLiquid)) {
                return entry.getValue();
            }
        }
        return ItemStack.EMPTY;
    }
}
