package eiteam.esteemedinnovation.tools.steam.upgrades.drillhead;

import eiteam.esteemedinnovation.api.tool.SteamToolSlot;
import eiteam.esteemedinnovation.api.tool.SteamToolUpgrade;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class ItemDrillHeadUpgrade extends Item implements SteamToolUpgrade {
    @Override
    public boolean isUniversal() {
        return false;
    }

    @Override
    public SteamToolSlot getToolSlot() {
        return SteamToolSlot.DRILL_HEAD;
    }

    @Override
    public String getInformation(ItemStack self, ItemStack tool) {
        DrillHeadMaterial mat = DrillHeadMaterial.materials.get(getMyMaterial(self));
        if (mat != null) {
            String name = mat.locName;
            return I18n.hasKey(name) ? I18n.format(name) : mat.materialName;
        } else {
            return "";
        }
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        tooltip.add(getInformation(stack, null));
    }

    @Override
    public int getToolStrength(IBlockState state, ItemStack toolStack, ItemStack upgradeStack) {
        return DrillHeadMaterial.materials.get(getMyMaterial(upgradeStack)).harvestLevel;
    }

    @Override
    public boolean modifiesToolTier() {
        return true;
    }

    /**
     * Sets the material name to the itemstack's NBT data.
     * @param self The ItemStack to set.
     * @param material The material to apply to the ItemStack.
     */
    public void setMyMaterial(ItemStack self, String material) {
        if (!self.hasTagCompound()) {
            self.setTagCompound(new NBTTagCompound());
        }
        self.getTagCompound().setString("material", material);
    }

    /**
     * Gets the material string for the ItemStack.
     * @param self The ItemStack to get.
     * @return The ItemStack's material name. Defaults to "Iron".
     */
    public static String getMyMaterial(ItemStack self) {
        if (!self.hasTagCompound()) {
            return "Iron";
        }
        if (self.getTagCompound().hasKey("material")) {
            return self.getTagCompound().getString("material");
        } else {
            return "Iron";
        }
    }

    @Override
    public ResourceLocation getBaseIcon() {
        // The Drill Head Upgrade has custom rendering techniques that do not require new textures.
        return null;
    }
}
