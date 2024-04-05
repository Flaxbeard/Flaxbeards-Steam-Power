package eiteam.esteemedinnovation.api.mold;

import eiteam.esteemedinnovation.api.util.ItemStackMetaComparator;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;

public class MoldRegistry {
    /**
     * The Items that can be used on the Carving Table.
     */
    public static final ArrayList<ItemStack> molds = new ArrayList<>();

    private static final Comparator<ItemStack> COMPARATOR = new ItemStackMetaComparator();

    /**
     * Allows the mold to be used on the Carving Table.
     * @param mold The item. Only adds the mold at metadata 0.
     */
    public static void addCarvableMold(Item mold) {
        addCarvableMold(new ItemStack(mold));
    }

    /**
     * Allows the modl to be used on the Craving Table
     * @param mold The item stack.
     */
    public static void addCarvableMold(ItemStack mold) {
        molds.add(mold);
        molds.sort(COMPARATOR);
    }

    /**
     * Removes the mold from the list of Items that can be used on the Carving Table.
     * @param mold The item
     * @return The return value of ArrayList#remove.
     * @see ArrayList#remove(Object)
     */
    public static boolean removeMold(Item mold) {
        return molds.removeIf(s -> s.getItem() == mold);
    }
}
