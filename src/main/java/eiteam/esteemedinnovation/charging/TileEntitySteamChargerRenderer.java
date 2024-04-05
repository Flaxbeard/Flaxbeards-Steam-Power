package eiteam.esteemedinnovation.charging;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

public class TileEntitySteamChargerRenderer extends TileEntitySpecialRenderer<TileEntitySteamCharger> {
    @Override
    public void render(TileEntitySteamCharger charger, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        ItemStack stackInSlotZero = charger.getStackInSlot(0);
        if (stackInSlotZero == null) {
            return;
        }
        Minecraft mc = Minecraft.getMinecraft();
        EnumFacing facing = charger.getLevelObj().getBlockState(charger.getPos()).getValue(BlockSteamCharger.FACING);
        RenderHelper.enableStandardItemLighting();
        GlStateManager.enableLighting();
        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5, y + 0.5 + (1F / 16F) - 0.02F, z + 0.5);
        if (facing == EnumFacing.NORTH) {
            GlStateManager.rotate(180F, 0, 1, 0);
        } else if (facing == EnumFacing.EAST) {
            GlStateManager.rotate(270F, 0, 1, 0);
        } else if (facing == EnumFacing.WEST) {
            GlStateManager.rotate(90F, 0, 1, 0);
        }
        GlStateManager.rotate(90F, 1, 0, 0);
        mc.getRenderItem().renderItem(stackInSlotZero, ItemCameraTransforms.TransformType.NONE);
        GlStateManager.popMatrix();
    }

}
