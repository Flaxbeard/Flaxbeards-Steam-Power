package eiteam.esteemedinnovation.api.crucible;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class CrucibleLiquid {
    private final int cr;
    private final int cg;
    private final int cb;
    private final int ca;
    private final String name;

    public CrucibleLiquid(String name, int r, int g, int b) {
        this(name, r, g, b, 255);
    }

    public CrucibleLiquid(String name, int r, int g, int b, int a) {
        this.name = name;
        cr = r;
        cg = g;
        cb = b;
        ca = a;
    }

    public int getRed() {
        return cr;
    }

    public int getBlue() {
        return cb;
    }

    public int getGreen() {
        return cg;
    }

    public int getAlpha() {
        return ca;
    }

    public String getName() {
        return name;
    }

    /**
     * Overload for {@link #getDisplayItems(int, boolean)} that passes true for the isInput value.
     */
    public ItemStack[] getDisplayItems(int amount) {
        return getDisplayItems(amount, true);
    }

    /**
     * @param amount The amount of this liquid
     * @param isInput Whether this is being used as an input liquid (true) or an output liquid (false).
     * @return The ItemStacks used as item representations of this fluid, for use primarily in the journal.
     */
    public ItemStack[] getDisplayItems(int amount, boolean isInput) {
        List<ItemStack> items = new ArrayList<>();

        if (isInput) {
            for (Map.Entry<Ingredient, Pair<CrucibleLiquid, Integer>> entry : CrucibleRegistry.liquidRecipes.entrySet()) {
                Pair<CrucibleLiquid, Integer> output = entry.getValue();
                CrucibleLiquid liquid = output.getLeft();
                int amountCreated = output.getRight();

                if (liquid == this && amountCreated == amount) {
                    Ingredient input = entry.getKey();
                    items.addAll(Arrays.asList(input.getItems()));
                }
            }
        }

        return items.toArray(new ItemStack[items.size()]);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CrucibleLiquid)) {
            return false;
        }

        CrucibleLiquid other = (CrucibleLiquid) o;

        return getRed() == other.getRed() && getGreen() == other.getGreen() && getBlue() == other.getBlue() &&
          getAlpha() == other.getAlpha() && getName().equals(other.getName());
    }

    @Override
    public int hashCode() {
        int result = getRed();
        result = 31 * result + getGreen();
        result = 31 * result + getBlue();
        result = 31 * result + getAlpha();
        result = 31 * result + getName().hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "{CrucibleLiquid R: " + getRed() + " G: " + getGreen() + " B: " + getBlue() + " A: " + getAlpha() + " Name: " + getName() + " }";
    }
}
