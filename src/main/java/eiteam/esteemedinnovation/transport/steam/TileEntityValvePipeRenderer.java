package eiteam.esteemedinnovation.transport.steam;

import eiteam.esteemedinnovation.commons.EsteemedInnovation;
import eiteam.esteemedinnovation.commons.util.RenderUtility;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TileEntityValvePipeRenderer extends TileEntitySpecialRenderer<TileEntityValvePipe> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(EsteemedInnovation.MOD_ID, "textures/blocks/block_copper.png");
    private static final ResourceLocation VALVE_RL = new ResourceLocation(EsteemedInnovation.MOD_ID, "block/pipe_valve");

    @Override
    public void render(TileEntityValvePipe valve, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        World world = valve.getLevelObj();
        BlockPos pos = valve.getPos();
        IBlockState state = world.getBlockState(pos);
        EnumFacing facing = state.getValue(BlockValvePipe.FACING);
        float outset = 3.5F + (valve.open ? 0.0F : -1.0F) + (valve.open ? -0.1F * valve.turnTicks : 0.1F * valve.turnTicks);
        GlStateManager.translate(outset * facing.getXOffset() / 16F, outset * facing.getYOffset() / 16F,
          outset * facing.getZOffset() / 16F);
        IBlockState actualState = state.getActualState(world, pos);

        // The no_connections model is actually slightly larger than the normal pipe, by a single cube in each direction
        // So we have to handle that here, by translating 1 cube in the direction of the valve.
        boolean anyConnections = actualState.getValue(BlockValvePipe.UP) || actualState.getValue(BlockValvePipe.DOWN) ||
          actualState.getValue(BlockValvePipe.EAST) || actualState.getValue(BlockValvePipe.WEST) ||
          actualState.getValue(BlockValvePipe.NORTH) || actualState.getValue(BlockValvePipe.SOUTH);
        if (!anyConnections) {
            GlStateManager.translate(facing.getXOffset() / 16F, facing.getYOffset() / 16F, facing.getZOffset() / 16F);
        }
        GlStateManager.translate(0.5, 0.5, 0.5);
        switch (facing) {
            case UP: {
                GlStateManager.rotate(90, 0, 0, 1);
                break;
            }
            case DOWN: {
                GlStateManager.rotate(-90, 0, 0, 1);
                break;
            }
            case NORTH: {
                GlStateManager.rotate(90, 0, 1, 0);
                break;
            }
            case SOUTH: {
                GlStateManager.rotate(-90, 0, 1, 0);
                break;
            }
            case WEST: {
                GlStateManager.rotate(180, 0, 1, 0);
                break;
            }
            default: {
                break;
            }
        }
        GlStateManager.rotate((225.0F * (valve.isOpen() ? valve.turnTicks : 10 - valve.turnTicks) / 10.0F), 1, 0, 0);
        GlStateManager.translate(-0.5, -0.5, -0.5);

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buffer = tess.getBuffer();
        RenderUtility.renderModel(buffer, VALVE_RL);
        bindTexture(TEXTURE);
        tess.draw();

        GlStateManager.popMatrix();
    }
}
