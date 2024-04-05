package eiteam.esteemedinnovation.transport.fluid.funnel;

import eiteam.esteemedinnovation.api.tile.BlockEntityTickableSafe;
import eiteam.esteemedinnovation.api.util.FluidHelper;
import eiteam.esteemedinnovation.transport.TransportationModule;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fluids.capability.IFluidHandler;

import java.util.HashSet;

import static net.minecraftforge.fluids.Fluid.BUCKET_VOLUME;
import static net.minecraftforge.fluids.capability.CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY;

public class TileEntityFunnel extends BlockEntityTickableSafe {
    protected FluidTank tank = new FluidTank(BUCKET_VOLUME);

    @Override
    public boolean canUpdate(BlockState target) {
        return target.getBlock() == TransportationModule.FUNNEL;
    }

    @Override
    public void safeUpdate() {
        if (world.isRemote || world.getBlockState(pos).getActualState(world, pos).getValue(BlockFunnel.POWERED)) {
            return;
        }

        if (tank.getFluidAmount() == 0) {
            BlockPos up = pos.up();
            IBlockState aboveState = world.getBlockState(up);
            Fluid fluid = FluidHelper.getFluidFromBlockState(aboveState);
            if (fluid != null) {
                BlockPos source = FluidHelper.findSourceBlockPos(world, up, fluid, new HashSet<>());
                if (source != null) {
                    IBlockState sourceState = world.getBlockState(source);
                    Block sourceBlock = sourceState.getBlock();
                    FluidStack fluidStack;
                    if (sourceBlock instanceof IFluidBlock) {
                        fluidStack = ((IFluidBlock) sourceBlock).drain(world, source, true);
                    } else {
                        fluidStack = new FluidStack(fluid, BUCKET_VOLUME);
                        if (!FluidHelper.isInfiniteWaterSource(world, up, fluid)) {
                            world.setBlockToAir(source);
                        }
                    }
                    tank.fill(fluidStack, true);
                }
            }
        }

        FluidStack fluid = tank.getFluid();
        if (tank.getFluidAmount() > 0 && fluid != null) {
            EnumFacing dir = world.getBlockState(pos).getValue(BlockFunnel.FACING);
            BlockPos targetPos = pos.offset(dir);
            TileEntity targetTile = world.getTileEntity(targetPos);
            if (targetTile == null) {
                return;
            }

            IFluidHandler handler = FluidHelper.getFluidHandler(targetTile, dir.getOpposite());
            if (handler != null) {
                handler.fill(fluid, true);
                tank.drain(fluid, true);
            }
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        tank.readFromNBT(tag);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        tag = super.writeToNBT(tag);
        tank.writeToNBT(tag);
        return tag;
    }

    private boolean hasTankCapability(Capability<?> capability, EnumFacing face) {
        return capability == FLUID_HANDLER_CAPABILITY &&
          (face == EnumFacing.UP || face == world.getBlockState(pos).getValue(BlockFunnel.FACING));
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        return hasTankCapability(capability, facing) ||
          super.hasCapability(capability, facing);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (hasTankCapability(capability, facing)) {
            return (T) tank;
        }
        return super.getCapability(capability, facing);
    }
}
