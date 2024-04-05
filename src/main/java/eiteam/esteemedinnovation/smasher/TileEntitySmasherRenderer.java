package eiteam.esteemedinnovation.smasher;

import eiteam.esteemedinnovation.commons.util.RenderUtility;
import eiteam.esteemedinnovation.commons.EsteemedInnovation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

public class TileEntitySmasherRenderer extends TileEntitySpecialRenderer<TileEntitySmasher> {
    private static final ResourceLocation HEAD_RL = new ResourceLocation(EsteemedInnovation.MOD_ID, "block/smasher_head");

    @Override
    public void render(TileEntitySmasher smasher, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5, y + 0.5, z + 0.5);
        double dist;
        if (smasher.extendedTicks <= 5) {
            dist = (Math.sin(Math.toRadians(18D * smasher.extendedTicks)) * 0.51F);
        } else if (smasher.extendedTicks <= 15) {
            dist = 0.51F;
        } else {
            dist = 0.51F - (Math.sin(Math.toRadians(9D * (smasher.extendedTicks - 15))) * 0.51F);
        }
        EnumFacing dir = smasher.getLevelObj().getBlockState(smasher.getPos()).getValue(BlockSmasher.FACING);
        GlStateManager.translate(dist * dir.getXOffset(), 0, dist * dir.getZOffset());
        if (dir == EnumFacing.NORTH) {
            GlStateManager.rotate(180F, 0, 1, 0);
        } else if (dir == EnumFacing.WEST) {
            GlStateManager.rotate(270F, 0, 1, 0);
        } else if (dir == EnumFacing.EAST) {
            GlStateManager.rotate(90F, 0, 1, 0);
        }
        GlStateManager.translate(-0.5, -0.5, -0.5);
        bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        RenderUtility.renderModel(Tessellator.getInstance().getBuffer(), HEAD_RL);
        Tessellator.getInstance().draw();

        GlStateManager.popMatrix();
    }
}
