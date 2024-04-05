package eiteam.esteemedinnovation.heater;

import eiteam.esteemedinnovation.api.steamnet.SteamNetwork;
import eiteam.esteemedinnovation.commons.util.WorldHelper;
import eiteam.esteemedinnovation.transport.steam.TileEntitySteamPipe;
import net.minecraft.block.BlockFurnace;
import net.minecraft.block.state.IBlockState;
import net.minecraft.core.Direction;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

import static eiteam.esteemedinnovation.heater.TileEntitySteamFurnace.*;

// FIXME: Shift-clicking in the SteamFurnace GUI with things added by addSteamingRecipe are not put into slot 0.
public class TileEntitySteamHeater extends TileEntitySteamPipe {
    // When multiple heaters are used on a furnace, there is a single primary heater
    public boolean isPrimaryHeater;
    public static final int CONSUMPTION = HeaterModule.heaterConsumption;

    public TileEntitySteamHeater() {
        addSidesToGaugeBlacklist(EnumFacing.VALUES);
    }

    public static void replaceWith(TileEntityFurnace current, TileEntityFurnace replacement) {
        if (current != null) {
            ItemStack[] furnaceItemStacks = {
              current.getStackInSlot(0),
              current.getStackInSlot(1),
              current.getStackInSlot(2)
            };
            int furnaceBurnTime = current.getField(FURNACE_BURN_TIME_ID);
            int currentItemBurnTime = current.getField(CURRENT_ITEM_BURN_TIME_ID);
            int furnaceCookTime = current.getField(COOK_TIME_ID);
            current.getWorld().setTileEntity(current.getPos(), replacement);
            @SuppressWarnings("TypeMayBeWeakened")
            TileEntityFurnace replaced = (TileEntityFurnace) current.getWorld().getTileEntity(current.getPos());
            assert replaced != null;
            replaced.setInventorySlotContents(0, furnaceItemStacks[0]);
            replaced.setInventorySlotContents(1, furnaceItemStacks[1]);
            replaced.setInventorySlotContents(2, furnaceItemStacks[2]);
            replaced.setField(FURNACE_BURN_TIME_ID, furnaceBurnTime);
            replaced.setField(CURRENT_ITEM_BURN_TIME_ID, currentItemBurnTime);
            replaced.setField(COOK_TIME_ID, furnaceCookTime);
        }
    }

    public static void replace(TileEntityFurnace furnace) {
        replaceWith(furnace, new TileEntitySteamFurnace());
    }


    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        superReadFromNBT(nbt);
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        superWriteToNBT(nbt);
        return nbt;
    }

    @Override
    public void initialUpdate() {
        super.initialUpdate();
        setValidDistributionDirectionsExcluding(world.getBlockState(pos).getValue(BlockSteamHeater.FACING));
    }

    @Override
    public void safeUpdate() {
        superUpdate();
        EnumFacing dir = world.getBlockState(pos).getValue(BlockSteamHeater.FACING);

        List<TileEntitySteamHeater> secondaryHeaters = new ArrayList<>();
        BlockPos offsetPos = getRelativePos(dir);
        TileEntity tile = world.getTileEntity(offsetPos);
        if (tile == null && getSteamShare() >= CONSUMPTION) {
            AxisAlignedBB heaterAABB = new AxisAlignedBB(0, 0, 1, 1, 1, 21F / 16F);
            heaterAABB = WorldHelper.getDirectionalBoundingBox(dir, heaterAABB, true).offset(pos);
            List<Entity> burnedEntities = world.getEntitiesWithinAABB(Entity.class, heaterAABB);
            for (Entity burnedEntity : burnedEntities) {
                burnedEntity.attackEntityFrom(SteamNetwork.STEAMED_DAMAGE, 1F);
                decrSteam(CONSUMPTION);
            }
            return;
        }
        if (!(tile instanceof TileEntityFurnace)) {
            return;
        }
        TileEntityFurnace furnace = (TileEntityFurnace) tile;

        int numHeaters = 0;
        isPrimaryHeater = false;
        for (EnumFacing dir2 : EnumFacing.VALUES) {
            int x = pos.getX() + dir.getXOffset() + dir2.getXOffset();
            int y = pos.getY() + dir.getYOffset() + dir2.getYOffset();
            int z = pos.getZ() + dir.getZOffset() + dir2.getZOffset();
            BlockPos pos2 = new BlockPos(x, y, z);
            TileEntity tile2 = world.getTileEntity(pos2);
            IBlockState state2 = world.getBlockState(pos2);
            if (tile2 != null)  {
                if (tile2 instanceof TileEntitySteamHeater) {
                    TileEntitySteamHeater heater2 = (TileEntitySteamHeater) tile2;
                    if (heater2.getSteamShare() >= CONSUMPTION && state2.getValue(BlockSteamHeater.FACING).getOpposite() == dir2) {
                        isPrimaryHeater = x == pos.getX() && y == pos.getY() && z == pos.getZ();
                        secondaryHeaters.add(heater2);
                        numHeaters++;
                        if (secondaryHeaters.size() > 4) {
                            secondaryHeaters.remove(0);
                        }
                        numHeaters = Math.min(4, numHeaters);
                    }
                }
            }
        }
        if (isPrimaryHeater && numHeaters > 0) {
            if (!(furnace instanceof TileEntitySteamFurnace) && furnace.getClass() == TileEntityFurnace.class) {
                replace(furnace);
            }

            if (!(furnace instanceof TileEntitySteamFurnace)) {
                return;
            }

            int furnaceBurnTime = furnace.getField(FURNACE_BURN_TIME_ID);
            int furnaceCookTime = furnace.getField(COOK_TIME_ID);

            if ((furnaceBurnTime == 1 || furnaceBurnTime == 0) && getSteamShare() >= CONSUMPTION &&
              ((TileEntitySteamFurnace) furnace).canSmelt()) {
                if (furnaceBurnTime == 0) {
                    BlockFurnace.setState(true, world, offsetPos);
                }

                for (TileEntitySteamHeater heater : secondaryHeaters) {
                    heater.decrSteam(CONSUMPTION);
                }

                furnace.setField(0, furnaceBurnTime + 3);

                if (numHeaters > 1 && furnaceCookTime > 0) {
                    int newCookTime = Math.min(furnaceCookTime + 2 * numHeaters - 1, 199);
                    furnace.setField(COOK_TIME_ID, newCookTime);
                }
                world.notifyBlockUpdate(offsetPos, world.getBlockState(offsetPos), world.getBlockState(offsetPos), 0);
            }
        }
    }

    @Override
    public boolean onWrench(ItemStack stack, Player player, Level level, BlockPos pos, HumanoidArm hand, Direction facing, BlockState state, float hitX, float hitY, float hitZ) {
        int steam = getSteamShare();
        getNetwork().split(this, true);
        EnumFacing dir = state.getValue(BlockSteamHeater.FACING);
        setValidDistributionDirectionsExcluding(dir);
        BlockPos offsetPos = pos.offset(dir);
        level.notifyBlockUpdate(offsetPos, level.getBlockState(offsetPos), level.getBlockState(offsetPos), 0);
        SteamNetwork.newOrJoin(this);
        getNetwork().addSteam(steam);
        return true;
    }
}
