package eiteam.esteemedinnovation.thumper;

import eiteam.esteemedinnovation.commons.util.RenderUtility;
import eiteam.esteemedinnovation.commons.EsteemedInnovation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

public class TileEntityThumperRenderer extends TileEntitySpecialRenderer<TileEntityThumper> {
    private static final ResourceLocation THUMPER_RL = new ResourceLocation(EsteemedInnovation.MOD_ID, "block/thumper_thumper");

    @Override
    public void render(TileEntityThumper thumper, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5, y + 0.5, z + 0.5);
        EnumFacing facing = thumper.getLevelObj().getBlockState(thumper.getPos()).getValue(BlockThumper.FACING);
        if (facing.getAxis() == EnumFacing.Axis.Z) {
            GlStateManager.rotate(90F, 0, 1, 0);
        }
        GlStateManager.translate(-0.5, -0.5, -0.5);
        double trans = 0;
        if (thumper.progress < 20) {
            trans = 0.0D;
        } else if (thumper.progress < 80) {
            trans = (3.0D * Math.sin(Math.toRadians(1.5D * (thumper.progress - 20))));
        } else if (thumper.progress < 90) {
            trans = 3.0D - (0.1D * Math.sin(Math.toRadians(9D * (thumper.progress - 80))));
        } else if (thumper.progress < 100) {
            trans = 2.9D;
        } else if (thumper.progress >= 100) {
            trans = 2.9D - 2.9D * (1.0D - Math.sin(Math.toRadians(90.0D - (9.0D * (thumper.progress - 100)))));
        }
        GlStateManager.translate(0, trans, 0);
        Tessellator tess = Tessellator.getInstance();
        RenderUtility.renderModel(tess.getBuffer(), THUMPER_RL);
        bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        tess.draw();

        GlStateManager.popMatrix();
    }
}
