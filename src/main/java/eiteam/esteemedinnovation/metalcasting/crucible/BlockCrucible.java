package eiteam.esteemedinnovation.metalcasting.crucible;

import eiteam.esteemedinnovation.api.crucible.CrucibleLiquid;
import eiteam.esteemedinnovation.api.crucible.CrucibleRegistry;
import eiteam.esteemedinnovation.api.wrench.Wrenchable;
import eiteam.esteemedinnovation.commons.EsteemedInnovation;
import eiteam.esteemedinnovation.commons.util.WorldHelper;
import eiteam.esteemedinnovation.heater.BlockSteamHeater;
import eiteam.esteemedinnovation.heater.TileEntitySteamHeater;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.core.Direction;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import javax.annotation.Nonnull;

import static eiteam.esteemedinnovation.metalcasting.MetalcastingModule.HELL_CRUCIBLE;

public class BlockCrucible extends Block implements Wrenchable {
    public static final PropertyDirection FACING = BlockHorizontal.FACING;
    private static final float PX = (1.0F / 16.0F);
    public static final AxisAlignedBB CRUCIBLE_AABB = new AxisAlignedBB(PX, PX, PX, 1F - PX, 1F - PX, 1F - PX);
    public static final ResourceLocation LIQUID_ICON_RL = new ResourceLocation(EsteemedInnovation.MOD_ID, "blocks/crucible_liquid");

    public BlockCrucible() {
        super(Material.ROCK);
        setHardness(3.5F);
        setDefaultState(blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
    }

    @Nonnull
    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Nonnull
    @Override
    public BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(FACING).getIndex();
    }

    @Nonnull
    @Override
    public IBlockState getStateFromMeta(int meta) {
        EnumFacing dir = EnumFacing.byIndex(meta);

        if (dir.getAxis() == EnumFacing.Axis.Y) {
            dir = EnumFacing.NORTH;
        }
        return getDefaultState().withProperty(FACING, dir);
    }

    @Nonnull
    @Override
    public IBlockState withRotation(@Nonnull IBlockState state, Rotation rot) {
        return state.withProperty(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Nonnull
    @Override
    public IBlockState withMirror(@Nonnull IBlockState state, Mirror mirrorIn) {
        return state.withRotation(mirrorIn.toRotation(state.getValue(FACING)));
    }

    @Nonnull
    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        return CRUCIBLE_AABB;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean hasComparatorInputOverride(IBlockState state) {
        return true;
    }

    @Override
    public int getComparatorInputOverride(IBlockState state, World world, BlockPos pos) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileEntityCrucible) {
            TileEntityCrucible crucible = (TileEntityCrucible) te;
            return crucible.getComparatorOutput();
        }
        return 0;
    }

    public boolean isCrucibleHeated(World world, BlockPos pos) {
        BlockPos underCruciblePosition = new BlockPos(pos.getX(), pos.getY() - 1, pos.getZ());
        IBlockState stateUnderCrucible = world.getBlockState(underCruciblePosition);
        TileEntity tileUnderCrucible = world.getTileEntity(underCruciblePosition);

        if (this == HELL_CRUCIBLE || stateUnderCrucible.getMaterial() == Material.FIRE ||
          stateUnderCrucible.getMaterial() == Material.LAVA) {
            return true;
        }

        if (tileUnderCrucible instanceof TileEntitySteamHeater) {
            TileEntitySteamHeater steamHeater = (TileEntitySteamHeater) tileUnderCrucible;

            if (world.getBlockState(steamHeater.getPos()).getValue(BlockSteamHeater.FACING) == EnumFacing.UP &&
              steamHeater.getSteam() >= TileEntitySteamHeater.CONSUMPTION) {
                steamHeater.decrSteam(TileEntitySteamHeater.CONSUMPTION);
                return true;
            }
        }

        return false;
    }

    @Override
    public void onEntityCollision(World world, BlockPos pos, IBlockState state, Entity entity) {
        if (entity instanceof EntityItem) {
            EntityItem item = (EntityItem) entity;
            if (isCrucibleHeated(world, pos)) {
                Pair<CrucibleLiquid, Integer> output;
                if (CrucibleRegistry.liquidRecipes.containsKey(Pair.of(item.getItem().getItem(), item.getItem().getItemDamage()))) {
                    output = CrucibleRegistry.liquidRecipes.get(Pair.of(item.getItem().getItem(), item.getItem().getItemDamage()));
                } else if (CrucibleRegistry.liquidRecipes.containsKey(Pair.of(item.getItem().getItem(), -1))) {
                    output = CrucibleRegistry.liquidRecipes.get(Pair.of(item.getItem().getItem(), -1));

                } else {
                    return;
                }
                TileEntityCrucible crucible = (TileEntityCrucible) world.getTileEntity(pos);
                int amount = output.getRight();
                if (crucible != null) {
                    // TODO: Waiting for response to MinecraftForge#3092. If it is not accepted, we will have to do
                    // reflection to get the delayBeforeCanPickup value.
//                    if (item.delayBeforeCanPickup > 2){
//                        item.delayBeforeCanPickup = 2;
//                    } else if (item.delayBeforeCanPickup <= 1) {
                        ItemStack stack = item.getItem();
                        ItemStack out = crucible.fillWith(stack, amount, output);
                        if (crucible.getFill() + amount <= 90) {
//                            item.delayBeforeCanPickup = 2;
                        }

                        if (out.getCount() <= 0) {
                            entity.setDead();
                        } else {
                            item.setItem(out);
                        }
                        crucible.needsUpdate = true;
//                    }
                }
            }
        }
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        world.setBlockState(pos, state.withProperty(FACING, placer.getHorizontalFacing().getOpposite()));
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
        return new TileEntityCrucible();
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        TileEntityCrucible tile = (TileEntityCrucible) world.getTileEntity(pos);
        if (tile == null) {
            return false;
        }
        ItemStack heldItem = player.getHeldItem(hand);
        if (!player.isSneaking() && heldItem.isEmpty()) {
            if (!tile.isTipping()) {
                tile.setTipping();
                tile.needsUpdate = true;
            }
        } else if (!heldItem.isEmpty()) {
            for (CrucibleLiquid liquid : tile.contents) {
                Triple<Item, Integer, CrucibleLiquid> triple = new ImmutableTriple<>(heldItem.getItem(), heldItem.getItemDamage(), liquid);
                boolean valid;
                if (!CrucibleRegistry.dunkRecipes.containsKey(triple)) {
                    triple = new ImmutableTriple<>(heldItem.getItem(), -1, liquid);
                    valid = CrucibleRegistry.dunkRecipes.containsKey(triple);
                } else {
                    valid = true;
                }

                if (valid) {
                    Pair<Integer, ItemStack> pair = CrucibleRegistry.dunkRecipes.get(triple);
                    int needed = pair.getLeft();
                    ItemStack result = pair.getRight().copy();
                    if (tile.number.get(liquid) >= needed) {
                        player.inventory.decrStackSize(player.inventory.currentItem, 1);
                        int currNum = tile.number.get(liquid);
                        currNum -= needed;
                        if (currNum == 0) {
                            tile.contents.remove(liquid);
                        }
                        tile.number.remove(liquid);
                        tile.number.put(liquid, currNum);
                        if (!player.inventory.addItemStackToInventory(result)) {
                            if (!player.world.isRemote) {
                                player.entityDropItem(result, 0.0F);
                            }
                        }
                        tile.needsUpdate = true;

                        break;
                    }
                }
            }
        }
        return true;
    }

    @Override
    public boolean onWrench(@Nonnull ItemStack stack, Player player, Level level, BlockPos pos, HumanoidArm hand, Direction facing, BlockState state, float hitX, float hitY, float hitZ) {
        if (facing != EnumFacing.DOWN && facing != EnumFacing.UP) {
            WorldHelper.rotateProperly(FACING, level, state, pos, facing);
            return true;
        }
        return false;
    }
}
