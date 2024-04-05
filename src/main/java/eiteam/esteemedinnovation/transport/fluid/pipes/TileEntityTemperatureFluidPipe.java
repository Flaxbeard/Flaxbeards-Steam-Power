package eiteam.esteemedinnovation.transport.fluid.pipes;

import eiteam.esteemedinnovation.api.tile.BlockEntityTickableSafe;
import eiteam.esteemedinnovation.api.util.FluidHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import java.util.ArrayList;
import java.util.List;

import static net.minecraftforge.fluids.capability.CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY;

/**
 * Base class for TileEntities which transport fluids within a certain fluid range.
 * Child classes must implement the {@link BlockEntityTickableSafe#canUpdate(net.minecraft.world.level.block.state.BlockState)}.
 */
// TODO: Impellers
public abstract class TileEntityTemperatureFluidPipe extends BlockEntityTickableSafe {
    private int transferAmount;
    private int tickCounter;
    private EnumFacing prevInteractSide;
    private List<EnumFacing> dirs = new ArrayList<EnumFacing>() {{
        add(EnumFacing.DOWN);
        for (EnumFacing dir : EnumFacing.HORIZONTALS) {
            add(dir);
        }
        add(EnumFacing.UP);
    }};
    public TemperatureFluidTank tank;

    public TileEntityTemperatureFluidPipe(int transferAmount, int minTemp, int maxTemp) {
        this.transferAmount = transferAmount;
        tank = new TemperatureFluidTank(minTemp, maxTemp);
    }

    @Override
    public void safeUpdate() {
        if (world.isRemote) {
            return;
        }

        tickCounter++;

        if (tickCounter > 10) {
            tickCounter = 0;
        }

        if (tickCounter != 10) {
            return;
        }

        FluidPipeBlockCapabilities.Mode transferMode = world.getBlockState(pos).getValue(FluidPipeBlockCapabilities.MODE);

        // TODO: Impeller
        for (EnumFacing dir : dirs) {
            BlockPos offset = pos.offset(dir);
            TileEntity offsetTile = world.getTileEntity(offset);
            if (offsetTile == null) {
                continue;
            }


            if (transferMode == FluidPipeBlockCapabilities.Mode.TRANSFER) {
                if (offsetTile instanceof TileEntityTemperatureFluidPipe && offsetTile.hasCapability(FLUID_HANDLER_CAPABILITY, dir)) {
                    FluidPipeBlockCapabilities.Mode offsetMode = world.getBlockState(offset).getValue(FluidPipeBlockCapabilities.MODE);
                    IFluidHandler handler = offsetTile.getCapability(FLUID_HANDLER_CAPABILITY, dir);
                    if (offsetMode != FluidPipeBlockCapabilities.Mode.INPUT) {
                        tryFillOurTank(handler);
                        prevInteractSide = dir;
                    }
                    if (offsetMode != FluidPipeBlockCapabilities.Mode.OUTPUT) {
                        tryDrainOurTank(handler);
                        prevInteractSide = dir;
                    }
                }
            }
            if (transferMode == FluidPipeBlockCapabilities.Mode.INPUT && !(offsetTile instanceof TileEntityTemperatureFluidPipe)) {
                IFluidHandler cap = FluidHelper.getFluidHandler(offsetTile, dir);
                if (cap != null) {
                    tryDrainOurTank(cap);
                    prevInteractSide = dir;
                }
            } else if (transferMode == FluidPipeBlockCapabilities.Mode.OUTPUT && !(offsetTile instanceof TileEntityTemperatureFluidPipe)) {
                IFluidHandler cap = FluidHelper.getFluidHandler(offsetTile, dir);
                if (cap != null) {
                    tryFillOurTank(cap);
                    prevInteractSide = dir;
                }
            }
        }

        if (prevInteractSide != null) {
            dirs.remove(prevInteractSide);
            dirs.add(0, prevInteractSide);
        }
    }

    private void safelyFillAndDrain(IFluidHandler toFill, IFluidHandler toDrain) {
        FluidStack firstDrain = toDrain.drain(transferAmount, false);
        if (firstDrain != null) {
            int testFill = toFill.fill(firstDrain, false);
            if (testFill > 0) {
                FluidStack secondDrain = toDrain.drain(testFill, false);
                if (secondDrain != null) {
                    toDrain.drain(toFill.fill(firstDrain, true), true);
                }
            }
        }
    }

    private void tryFillOurTank(IFluidHandler capability) {
        safelyFillAndDrain(tank, capability);
    }

    private void tryDrainOurTank(IFluidHandler capability) {
        safelyFillAndDrain(capability, tank);
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

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        return capability == FLUID_HANDLER_CAPABILITY || super.hasCapability(capability, facing);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == FLUID_HANDLER_CAPABILITY) {
            return (T) tank;
        }
        return super.getCapability(capability, facing);
    }
}
