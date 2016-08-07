package flaxbeard.steamcraft.client.render.tile;

import flaxbeard.steamcraft.Steamcraft;
import flaxbeard.steamcraft.block.BlockFan;
import flaxbeard.steamcraft.client.render.RenderUtility;
import flaxbeard.steamcraft.tile.TileEntityFan;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class TileEntityFanRenderer extends TileEntitySpecialRenderer<TileEntityFan> {
    private static final ResourceLocation BLADES_TEXTURE = new ResourceLocation(Steamcraft.MOD_ID, "textures/blocks/fan_blades_noise.png");
    private static final ResourceLocation BLADES_RL = new ResourceLocation(Steamcraft.MOD_ID, "block/fan_blades");

    @Override
    public void renderTileEntityAt(TileEntityFan tile, double x, double y, double z, float partialTicks, int destroyStage) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5, y + 0.5, z + 0.5);
        IBlockState state = tile.getWorld().getBlockState(tile.getPos());
        EnumFacing dir = state.getValue(BlockFan.FACING);
        if (dir == EnumFacing.DOWN) {
            GlStateManager.rotate(-90F, 0, 0, 1);
        }
        if (dir == EnumFacing.UP) {
            GlStateManager.rotate(90F, 0, 0, 1);
        }
        if (dir == EnumFacing.SOUTH) {
            GlStateManager.rotate(-90F, 0, 1, 0);
        }
        if (dir == EnumFacing.NORTH) {
            GlStateManager.rotate(90F, 0, 1, 0);
        }
        if (dir == EnumFacing.WEST) {
            GlStateManager.rotate(180F, 0, 1, 0);
        }
        GlStateManager.rotate(tile.rotateTicks * 25F, 1, 0, 0);
        GlStateManager.translate(-0.5, -0.5, -0.5);

        Tessellator tess = Tessellator.getInstance();
        VertexBuffer buffer = tess.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.ITEM);
        IBakedModel valveModel = RenderUtility.bakeModel(BLADES_RL);
        for (BakedQuad quad : valveModel.getQuads(null, null, 0)) {
            buffer.addVertexData(quad.getVertexData());
        }
        bindTexture(BLADES_TEXTURE);
        tess.draw();

        GlStateManager.popMatrix();
    }
}
