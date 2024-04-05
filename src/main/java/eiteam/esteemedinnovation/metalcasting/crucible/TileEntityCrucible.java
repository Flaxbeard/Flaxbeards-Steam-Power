package eiteam.esteemedinnovation.metalcasting.crucible;

import eiteam.esteemedinnovation.api.crucible.CrucibleFormula;
import eiteam.esteemedinnovation.api.crucible.CrucibleLiquid;
import eiteam.esteemedinnovation.api.crucible.CrucibleRegistry;
import eiteam.esteemedinnovation.api.mold.CrucibleMold;
import eiteam.esteemedinnovation.api.tile.BlockEntityTickableSafe;
import eiteam.esteemedinnovation.metalcasting.mold.TileEntityMold;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;

public class TileEntityCrucible extends BlockEntityTickableSafe {
    public ArrayList<CrucibleLiquid> contents = new ArrayList<>();
    public HashMap<CrucibleLiquid, Integer> number = new HashMap<>();
    public boolean hasUpdated = true;
    public boolean needsUpdate = false;
    public int tipTicks = 0;
    private int targetFill = -1;
    private boolean tipping;
    public boolean isPowered;
    private int lastComparatorOutput = 0;

    public TileEntityCrucible() {
        isPowered = false;
    }

    @Nonnull
    @Override
    public NBTTagCompound getUpdateTag() {
        return writeToNBT(new NBTTagCompound());
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        NBTTagList nbttaglist = (NBTTagList) nbt.getTag("liquids");

        for (int i = 0; i < nbttaglist.tagCount(); ++i) {
            NBTTagCompound nbttagcompound1 = nbttaglist.getCompoundTagAt(i);
            CrucibleLiquid liquid = CrucibleRegistry.getLiquidFromName(nbttagcompound1.getString("name"));
            if (liquid != null) {
                contents.add(liquid);
                number.put(liquid, (int) nbttagcompound1.getShort("amount"));
            }
        }
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        NBTTagList nbttaglist = new NBTTagList();

        for (CrucibleLiquid liquid : contents) {
            NBTTagCompound nbttagcompound1 = new NBTTagCompound();
            nbttagcompound1.setShort("amount", (short) (int) number.get(liquid));
            nbttagcompound1.setString("name", liquid.getName());
            nbttaglist.appendTag(nbttagcompound1);
        }

        nbt.setTag("liquids", nbttaglist);

        return nbt;
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        super.getUpdatePacket();
        NBTTagCompound access = new NBTTagCompound();
        NBTTagList nbttaglist = new NBTTagList();
        access.setInteger("tipTicks", tipTicks);
        access.setBoolean("tipping", tipping);

        for (CrucibleLiquid liquid : contents) {
            if (liquid != null) {
                NBTTagCompound nbttagcompound1 = new NBTTagCompound();
                nbttagcompound1.setShort("amount", (short) (int) number.get(liquid));
                nbttagcompound1.setString("name", liquid.getName());
                nbttaglist.appendTag(nbttagcompound1);
            }
        }

        access.setTag("liquids", nbttaglist);

        return new SPacketUpdateTileEntity(pos, 1, access);
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        super.onDataPacket(net, pkt);
        NBTTagCompound access = pkt.getNbtCompound();
        NBTTagList nbttaglist = (NBTTagList) access.getTag("liquids");

        contents = new ArrayList<>();
        number = new HashMap<>();
        if (tipTicks == 0) {
            tipTicks = access.getInteger("tipTicks");
        }
        tipping = access.getBoolean("tipping");
        for (int i = 0; i < nbttaglist.tagCount(); ++i) {
            NBTTagCompound nbttagcompound1 = nbttaglist.getCompoundTagAt(i);
            CrucibleLiquid liquid = CrucibleRegistry.getLiquidFromName(nbttagcompound1.getString("name"));
            contents.add(liquid);
            number.put(liquid, (int) nbttagcompound1.getShort("amount"));
        }
        markForResync();
    }

    @Override
    public boolean canUpdate(BlockState target) {
        return target.getBlock() instanceof BlockCrucible;
    }

    @Override
    public void safeUpdate() {
        if (targetFill < 0) {
            targetFill = getFill();
        }
        if (getFill() == targetFill) {
            hasUpdated = true;
        }
        IBlockState state = world.getBlockState(pos);
        EnumFacing myDir = state.getValue(BlockCrucible.FACING);

        if (world.getRedstonePowerFromNeighbors(pos) > 0) {
            isPowered = true;
        }
        if (tipping || isPowered) {
            tipTicks++;
            if (tipTicks == 45 && !world.isRemote) {
                int posX = pos.getX() + myDir.getXOffset();
                int posY = pos.getY();
                int posZ = pos.getZ() + myDir.getZOffset();
                BlockPos offsetPos = new BlockPos(posX, posY, posZ);
                TileEntity tile = world.getTileEntity(offsetPos);
                if (tile instanceof TileEntityMold) {
                    TileEntityMold mold = (TileEntityMold) tile;
                    if (mold.canPour() && !contents.isEmpty()) {
                        ItemStack moldStack = mold.mold;
                        CrucibleMold crucibleMold = (CrucibleMold) moldStack.getItem();
                        CrucibleLiquid liquid = getNextLiquid(crucibleMold, moldStack);
                        if (liquid != null) {
                            if (!world.isRemote) {
                                mold.pour(liquid);
                            }
                            int currNum = number.get(liquid);
                            currNum -= crucibleMold.getCostToMold(liquid, moldStack);
                            if (currNum == 0) {
                                contents.remove(liquid);
                            }
                            number.remove(liquid);
                            if (currNum > 0) {
                                number.put(liquid, currNum);
                            }
                            needsUpdate = true;
                        }
                    }
                }
            }
            if (tipTicks > 140) {
                tipTicks = 0;
                tipping = false;
                isPowered = false;
            }
        }

        // Optimization: Don't iterate over all the liquids if we have nothing.
        if (!contents.isEmpty()) {
            for (CrucibleFormula formula : CrucibleRegistry.alloyFormulas) {
                if (formula.matches(contents, number)) {
                    decreaseLiquid(formula.getLiquid1(), formula.getLiquid1Amount());
                    decreaseLiquid(formula.getLiquid2(), formula.getLiquid2Amount());

                    if (!number.containsKey(formula.getOutputLiquid())) {
                        contents.add(formula.getOutputLiquid());
                        number.put(formula.getOutputLiquid(), 0);
                    }
                    int currNum = number.get(formula.getOutputLiquid());
                    currNum += formula.getOutputAmount();
                    number.remove(formula.getOutputLiquid());
                    number.put(formula.getOutputLiquid(), currNum);
                    needsUpdate = true;
                }
            }
        }

        if (getComparatorOutput() != lastComparatorOutput){
            lastComparatorOutput = getComparatorOutput();
            markDirty();
        }
        if (needsUpdate) {
            markForResync(state);
            needsUpdate = false;
        }

    }

    /**
     * Depletes the liquid contents of the crucible for the provided liquid and amount.
     * @param liquid The liquid to remove
     * @param amount The amount of said liquid to remove
     */
    private void decreaseLiquid(CrucibleLiquid liquid, int amount) {
        int currNum = number.get(liquid);
        currNum -= amount;
        if (currNum == 0) {
            contents.remove(liquid);
        }
        number.remove(liquid);
        number.put(liquid, currNum);
    }

    public int getFill() {
        int fill = 0;
        for (CrucibleLiquid liquid : contents) {
            fill += number.get(liquid);
        }
        return fill;
    }

    public ItemStack fillWith(ItemStack stack, int amount, Pair<CrucibleLiquid, Integer> output) {
        int fill = getFill();
        if (!world.isRemote) {
            if (fill + amount <= 90 && hasUpdated) {
                CrucibleLiquid fluid = output.getLeft();
                if (!contents.contains(fluid)) {
                    contents.add(fluid);
                    number.put(fluid, 0);
                }
                int currAmount = number.get(fluid);
                currAmount += amount;
                number.remove(fluid);
                number.put(fluid, currAmount);
                stack.shrink(1);
                hasUpdated = false;
                targetFill = fill + amount;
                needsUpdate = true;
            }
        }
        return stack;
    }

    public CrucibleLiquid getNextLiquid(CrucibleMold mold, ItemStack moldStack) {
        for (CrucibleLiquid liquid : CrucibleRegistry.liquids) {
            if (number.containsKey(liquid)) {
                if (mold.canUseOn(liquid, moldStack) && number.get(liquid) >= mold.getCostToMold(liquid, moldStack)) {
                    return liquid;
                }
            }
        }
        return null;
    }

    public boolean isTipping() {
        return !(!tipping || tipTicks > 90);
    }

    public void setTipping() {
        tipping = true;
        tipTicks = 0;
    }

    public int getComparatorOutput() {
        return (int) ((double) 15 * (((double) getFill() / 90D)));
    }
}
