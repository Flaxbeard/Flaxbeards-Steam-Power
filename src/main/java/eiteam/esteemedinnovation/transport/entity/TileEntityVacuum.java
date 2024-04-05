package eiteam.esteemedinnovation.transport.entity;

import eiteam.esteemedinnovation.api.steamnet.SteamNetwork;
import eiteam.esteemedinnovation.api.tile.SteamTransporterBlockEntity;
import eiteam.esteemedinnovation.api.tile.ThumperAdjacentBehaviorModifier;
import eiteam.esteemedinnovation.api.wrench.WrenchDisplay;
import eiteam.esteemedinnovation.api.wrench.Wrenchable;
import eiteam.esteemedinnovation.commons.util.MathUtility;
import eiteam.esteemedinnovation.transport.TransportationModule;
import net.minecraft.block.state.IBlockState;
import net.minecraft.core.Direction;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityHopper;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Post;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TileEntityVacuum extends SteamTransporterBlockEntity implements Wrenchable, WrenchDisplay, ThumperAdjacentBehaviorModifier {
    private static final int VACUUM_STEAM_CONSUMPTION = TransportationModule.vacuumConsumption;

    // half angle of cone
    private static final float THETA = (float) Math.PI / 2.0F;
    public boolean active;
    public boolean powered = false;
    public boolean lastSteam = false;
    public int rotateTicks = 0;
    public int range = 9;

    public static boolean isLyingInCone(float[] x, float[] t, float[] b, float aperture) {
        // This is for our convenience
        float halfAperture = aperture / 2.F;

        // Vector pointing to X point from apex
        float[] apexToXVect = dif(t, x);

        // Vector pointing from apex to circle-center point.
        float[] axisVect = dif(t, b);

        // X is lying in cone only if it's lying in
        // infinite version of its cone -- that is,
        // not limited by "round basement".
        // We'll use dotProd() to
        // determine angle between apexToXVect and axis.
        boolean isInInfiniteCone = dotProd(apexToXVect, axisVect)
          / magn(apexToXVect) / magn(axisVect)
          >
          // We can safely compare cos() of angles
          // between vectors instead of bare angles.
          Math.cos(halfAperture);


        if (!isInInfiniteCone) {
            return false;
        }

        // X is contained in cone only if projection of apexToXVect to axis
        // is shorter than axis.
        // We'll use dotProd() to figure projection length.
        return dotProd(apexToXVect, axisVect) / magn(axisVect) < magn(axisVect);
    }

    public static float dotProd(float[] a, float[] b) {
        return a[0] * b[0] + a[1] * b[1] + a[2] * b[2];
    }

    public static float[] dif(float[] a, float[] b) {
        return (new float[]{
          a[0] - b[0],
          a[1] - b[1],
          a[2] - b[2]
        });
    }

    public static float magn(float[] a) {
        return (float) (Math.sqrt(a[0] * a[0] + a[1] * a[1] + a[2] * a[2]));
    }

    @Override
    public boolean canUpdate(BlockState target) {
        return target.getBlock() == TransportationModule.VACUUM;
    }

    @Override
    public void initialUpdate() {
        super.initialUpdate();
        powered = world.isBlockPowered(pos);
        EnumFacing myDir = world.getBlockState(pos).getValue(BlockVacuum.FACING);
        setValidDistributionDirectionsExcluding(myDir, myDir.getOpposite());
    }

    @Override
    public void safeUpdate() {
        if (lastSteam != getSteamShare() > VACUUM_STEAM_CONSUMPTION) {
            markForResync();
        }
        lastSteam = getSteamShare() > VACUUM_STEAM_CONSUMPTION;
        if (!world.isRemote) {
            if ((getSteamShare() < VACUUM_STEAM_CONSUMPTION) || powered) {
                active = false;
            } else {
                active = true;
                decrSteam(VACUUM_STEAM_CONSUMPTION);
            }
        }
        if (active) {
            if (world.isRemote) {
                rotateTicks++;
            }
            EnumFacing dir = world.getBlockState(pos).getValue(BlockVacuum.FACING);
            float[] M = {
              pos.getX() + 0.5F,
              pos.getY() + 0.5F,
              pos.getZ() + 0.5F
            };
            float[] N = {
              pos.getX() + 0.5F + range * dir.getXOffset(),
              pos.getY() + 0.5F + range * dir.getYOffset(),
              pos.getZ() + 0.5F + range * dir.getZOffset()
            };
            //List entities = world.getEntitiesWithinAABB(Entity.class, AxisAlignedBB.getBoundingBox(xCoord+(dir.offsetX < 0 ? dir.offsetX * blocksInFront : 0), yCoord+(dir.offsetY < 0 ? dir.offsetY * blocksInFront : 0), zCoord+(dir.offsetZ < 0 ? dir.offsetZ * blocksInFront : 0), xCoord+1+(dir.offsetX > 0 ? dir.offsetX * blocksInFront : 0), yCoord+1+(dir.offsetY > 0 ? dir.offsetY * blocksInFront : 0), zCoord+1+(dir.offsetZ > 0 ? dir.offsetZ * blocksInFront : 0)));
            List<Entity> entities = world.getEntitiesWithinAABB(Entity.class,
              new AxisAlignedBB(pos.getX() - 20, pos.getY() - 20, pos.getZ() - 20, pos.getX() + 20, pos.getY() + 20, pos.getZ() + 20));
            for (int i = 0; i < 200; i++) {
                float[] X = {
                  (world.rand.nextFloat() * 40.0F) - 20.0F + pos.getX(),
                  (world.rand.nextFloat() * 40.0F) - 20.0F + pos.getY(),
                  (world.rand.nextFloat() * 40.0F) - 20.0F + pos.getZ()
                };
                if (isLyingInCone(X, M, N, THETA) && world.rayTraceBlocks(new Vec3d(X[0], X[1], X[2]),
                  new Vec3d(pos.getX() + 0.5F + dir.getXOffset(), pos.getY() + 0.5F + dir.getYOffset(),
                  pos.getZ() + 0.5F + dir.getZOffset())) == null) {
                    Vec3d vec = new Vec3d(X[0] - M[0], X[1] - M[1], X[2] - M[2]);
                    vec = vec.normalize();
                    world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, X[0], X[1], X[2], -vec.x * 0.5F,
                      -vec.y * 0.5F, -vec.z * 0.5F);
                }
            }

            for (Entity entity : entities) {
                float[] X = {
                  (float) entity.posX,
                  (float) entity.posY,
                  (float) entity.posZ
                };

                if (isLyingInCone(X, M, N, THETA) && world.rayTraceBlocks(
                  new Vec3d(entity.posX, entity.posY, entity.posZ),
                  new Vec3d(pos.getX() + 0.5F + dir.getXOffset(), pos.getY() + 0.5F + dir.getYOffset(),
                  pos.getZ() + 0.5F + dir.getZOffset())) == null) {
                    if (!(entity instanceof EntityPlayer) || !(((EntityPlayer) entity).capabilities.isFlying &&
                      ((EntityPlayer) entity).capabilities.isCreativeMode)) {
                        Vec3d vec = new Vec3d(X[0] - M[0], X[1] - M[1], X[2] - M[2]);
                        vec = vec.normalize();
                        double y = vec.y;
                        double x = vec.x;
                        double z = vec.z;
                        y *= 1;
                        if (entity.isSneaking()) {
                            x *= 0.25F;
                            y *= 0.25F;
                            z *= 0.25F;
                        }
                        entity.motionX -= x * 0.025F;
                        entity.motionY -= y * 0.05F;
                        entity.motionZ -= z * 0.025F;

                        entity.fallDistance = 0.0F;
                    }
                }
            }

            List<EntityItem> list = world.getEntitiesWithinAABB(EntityItem.class, new AxisAlignedBB(
              pos.getX() + dir.getXOffset() * 0.25F, pos.getY() + dir.getYOffset() * 0.25F,
              pos.getZ() + dir.getZOffset() * 0.25F, pos.getX() + 1.0D + dir.getXOffset() * 0.25F,
              pos.getY() + 1.0D + dir.getYOffset() * 0.25F, pos.getZ() + 1.0D + dir.getZOffset() * 0.25F));

            // TODO: This may be able to be optimized by iterating over the entire list. Test.
            if (!list.isEmpty()) {
                EntityItem item = list.get(0);
                BlockPos offsetPos = pos.offset(dir, -1);
                TileEntity tile = world.getTileEntity(offsetPos);
                if (tile instanceof ISidedInventory) {
                    ISidedInventory inv = (ISidedInventory) tile;
                    int[] access = inv.getSlotsForFace(dir.getOpposite());
                    for (int slot : access) {
                        if (putInInventory(item, slot, inv)) {
                            break;
                        }
                    }
                } else if (tile instanceof IInventory) {
                    IInventory inv = (IInventory) tile;
                    for (int i = 0; i < inv.getSizeInventory(); i++) {
                        if (putInInventory(item, i, inv)) {
                            break;
                        }
                    }
                }
            }
        }

        super.safeUpdate();
    }

    /**
     * @param item The item to put inside the inventory, held in an EntityItem.
     * @param slot The slot in which to put the item in within the inventory.
     * @param inv The inventory to put the item inside of.
     * @return Whether it was added or not.
     */
    private boolean putInInventory(EntityItem item, int slot, IInventory inv) {
        ItemStack checkStack1 = null;
        ItemStack checkStack2 = null;
        ItemStack stackInSlot = inv.getStackInSlot(slot);
        if (inv.getStackInSlot(slot) != null) {
            checkStack1 = stackInSlot.copy();
            checkStack1.setCount(1);
            checkStack2 = item.getItem().copy();
            checkStack2.setCount(1);
        }
        if ((stackInSlot == null || (ItemStack.areItemStacksEqual(checkStack1, checkStack2) &&
          stackInSlot.getCount() < stackInSlot.getMaxStackSize())) &&
          inv.isItemValidForSlot(slot, item.getItem())) {
            ItemStack stack = item.getItem().copy();
            boolean setDead = true;
            if (inv.getStackInSlot(slot) != null) {
                if ((stackInSlot.getCount() + stack.getCount()) > stack.getMaxStackSize() &&
                  checkStack2 != null) {
                    setDead = false;
                    int total = stackInSlot.getCount() + stack.getCount();
                    stack.setCount(stack.getMaxStackSize());
                    total -= stack.getMaxStackSize();
                    checkStack2.setCount(total);
                    item.setItem(checkStack2);
                    //item.getEntityItem().stackSize = (inv.getStackInSlot(slot).stackSize + stack.stackSize - stack.getMaxStackSize());
                } else {
                    stack.setCount(stackInSlot.getCount() + item.getItem().getCount());
                }
            }
            inv.setInventorySlotContents(slot, stack);
            if (setDead) {
                item.setDead();
            }
            return true;
        }
        return false;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound access) {
        super.writeToNBT(access);
        access.setBoolean("powered", powered);
        access.setShort("range", (short) range);

        return access;
    }

    @Override
    public void readFromNBT(NBTTagCompound access) {
        super.readFromNBT(access);
        powered = access.getBoolean("powered");
        range = access.getShort("range");

    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        NBTTagCompound access = super.getUpdateTag();
        access.setBoolean("active", getSteamShare() > VACUUM_STEAM_CONSUMPTION && !powered);
        access.setShort("range", (short) range);
        return new SPacketUpdateTileEntity(pos, 1, access);
    }


    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        super.onDataPacket(net, pkt);
        NBTTagCompound access = pkt.getNbtCompound();
        active = access.getBoolean("active");
        range = access.getShort("range");
        markForResync();
    }


    public void updateRedstoneState(boolean flag) {
        if (flag != powered) {
            powered = flag;
            markForResync();
        }
    }

    @Override
    public boolean onWrench(ItemStack stack, Player player, Level level, BlockPos pos, HumanoidArm hand, Direction facing, BlockState state, float xO, float yO, float zO) {
        if (player.isSneaking()) {
            range = MathUtility.minWithDefault(19, range + 2, 5);
            markForResync();
            return true;
        } else {
            int steam = getSteamShare();

            getNetwork().split(this, true);
            EnumFacing myDir = level.getBlockState(pos).getValue(BlockVacuum.FACING);
            setValidDistributionDirectionsExcluding(myDir, myDir.getOpposite());

            SteamNetwork.newOrJoin(this);
            getNetwork().addSteam(steam);
            return false;
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void displayWrench(Post event) {
        TileEntityFan.rangeDisplay(event, range);
    }

    @Override
    public void dropItems(SteamTransporterBlockEntity thumper, List<ItemStack> drops, IBlockState state, Collection<ThumperAdjacentBehaviorModifier> allBehaviorModifiers, EnumFacing directionIn) {
        BlockPos offsetPos = pos.offset(directionIn);
        TileEntity invTile = world.getTileEntity(offsetPos);
        // This will never happen because of isValidBehaviorModifier, but it won't compile without it :(
        if (!(invTile instanceof IInventory)) {
            return;
        }
        Collection<ItemStack> newDrops = new ArrayList<>();
        for (ItemStack drop : drops) {
            ItemStack remaining = TileEntityHopper.putStackInInventoryAllSlots(null, (IInventory) invTile, drop, directionIn);
            if (remaining != null && !remaining.isEmpty()) {
                newDrops.add(remaining);
            }
        }
        drops.clear();
        drops.addAll(newDrops);
    }

    @Override
    public boolean isValidBehaviorModifier(SteamTransporterBlockEntity thumper, Direction directionIn) {
        BlockPos offsetPos = pos.offset(directionIn);
        TileEntity tile = world.getTileEntity(offsetPos);
        return tile instanceof IInventory;
    }
}
