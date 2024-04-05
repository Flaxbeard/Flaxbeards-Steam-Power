package eiteam.esteemedinnovation.transport.fluid.screw;

import eiteam.esteemedinnovation.api.tile.SteamTransporterBlockEntity;
import eiteam.esteemedinnovation.api.util.FluidHelper;
import eiteam.esteemedinnovation.transport.TransportationModule;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TileEntityPump extends SteamTransporterBlockEntity {
    public FluidTank myTank = new FluidTank(1000);
    private final IFluidHandler inputHandler = new InputOnlyFluidHandler(myTank);
    private final IFluidHandler outputHandler = new OutputOnlyFluidHandler(myTank);
    public int progress = 0;
    public int rotateTicks = 0;
    private boolean running = false;
    private static final int STEAM_CONSUMPTION = TransportationModule.screwConsumption;

    public TileEntityPump() {
        super(EnumFacing.VALUES);
        this.addSidesToGaugeBlacklist(EnumFacing.VALUES);
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        NBTTagCompound access = super.getUpdateTag();
        access.setShort("progress", (short) progress);
        if (myTank.getFluid() != null) {
            access.setString("fluid", myTank.getFluid().getFluid().getName());
        }
        access.setBoolean("running", running);
        access.setInteger("fluidQuantity", myTank.getFluidAmount());
        return new SPacketUpdateTileEntity(pos, 1, access);
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        super.onDataPacket(net, pkt);
        NBTTagCompound access = pkt.getNbtCompound();
        progress = access.getShort("progress");
        if (access.hasKey("fluid")) {
            int amt = access.getInteger("fluidQuantity");
            myTank.setFluid(new FluidStack(FluidRegistry.getFluid(access.getString("fluid")), amt));
        }
        running = access.getBoolean("running");
        markForResync();
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        progress = nbt.getShort("progress");

        if (nbt.hasKey("fluid")) {
            int amt = nbt.getInteger("fluidQuantity");
            myTank.setFluid(new FluidStack(FluidRegistry.getFluid(nbt.getString("fluid")), amt));
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setShort("progress", (short) progress);

        nbt.setShort("water", (short) myTank.getFluidAmount());
        if (myTank.getFluid() != null) {
            nbt.setString("fluid", myTank.getFluid().getFluid().getName());
        }
        nbt.setInteger("fluidQuantity", myTank.getFluidAmount());
        return nbt;
    }

    private EnumFacing getOutputDirection() {
        return getInputDirection().getOpposite();
    }

    private EnumFacing getInputDirection() {
        return world.getBlockState(pos).getValue(BlockPump.FACING);
    }

    @Override
    public boolean canUpdate(BlockState target) {
        return target.getBlock() == TransportationModule.ARCHIMEDES_SCREW;
    }

    @Override
    public void safeUpdate() {
        if (world.isRemote) {
            if (running && progress < 100) {
                progress++;
                rotateTicks++;
            }
        } else {
            EnumFacing inputDir = getInputDirection();
            BlockPos offsetPos = getRelativePos(inputDir);
            Fluid fluid = FluidHelper.getFluidFromBlockState(world.getBlockState(offsetPos));
            if (getSteamShare() >= STEAM_CONSUMPTION && myTank.getFluidAmount() == 0 && fluid != null &&
              myTank.getFluidAmount() < 1000) {
                myTank.fill(new FluidStack(fluid, 1000), true);
                if (!FluidHelper.isInfiniteWaterSource(world, offsetPos, fluid)) {
                    world.setBlockToAir(offsetPos);
                }
                progress = 0;
                decrSteam(STEAM_CONSUMPTION);
                running = true;
                markDirty();
            }
            if (myTank.getFluidAmount() > 0 && myTank.getFluid() != null && progress < 100) {
                progress++;
                rotateTicks++;
            }
            EnumFacing outputDir = getOutputDirection();
            offsetPos = getRelativePos(outputDir);
            if (myTank.getFluidAmount() > 0 && progress == 100) {
                TileEntity tile = world.getTileEntity(offsetPos);
                if (tile != null) {
                    IFluidHandler fluidHandler = FluidHelper.getFluidHandler(tile, outputDir);
                    if (fluidHandler != null) {
                        int testFill = fluidHandler.fill(myTank.getFluid(), false);
                        if (testFill != 0) {
                            int amnt = fluidHandler.fill(myTank.getFluid(), true);
                            if (amnt > 0) {
                                myTank.drain(amnt, true);
                                if (myTank.getFluidAmount() == 0) {
                                    running = false;
                                    progress = 0;
                                }
                            } else if (running) {
                                running = false;
                            }
                        } else if (running) {
                            running = false;
                        }
                    } else if (running) {
                        running = false;
                    }
                }
                markDirty();
            }
        }

        super.safeUpdate();
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            EnumFacing output = getOutputDirection();
            return facing == output || facing == output.getOpposite();
        }
        return false;
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            EnumFacing output = getOutputDirection();
            if (facing == output) {
                return (T) outputHandler;
            } else if (facing == output.getOpposite()) {
                return (T) inputHandler;
            }
        }
        return null;
    }
}
