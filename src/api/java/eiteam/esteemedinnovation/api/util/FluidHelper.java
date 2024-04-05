package eiteam.esteemedinnovation.api.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.IFluidTank;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;

import javax.annotation.Nonnull;
import java.util.*;

public class FluidHelper {
    /**
     * Check if the player is holding a water container.
     * @param player The player.
     * @return Whether the player is holding a container and it has water in it.
     */
    public static boolean playerIsHoldingWaterContainer(Player player) {
        ItemStack heldItem = ItemStackUtility.getHeldItemStack(player);
        return itemStackIsWaterContainer(heldItem);
    }

    /**
     * Check if the ItemStack is a water container.
     * @param itemStack The ItemStack.
     * @return Whether the ItemStack is a container and has water in it.
     */
    public static boolean itemStackIsWaterContainer(ItemStack itemStack) {
        List<FluidStack> fluids = getFluidFromItemStack(itemStack);

        return !itemStack.isEmpty() && fluids.stream().anyMatch(f -> f.is(FluidTags.WATER));
    }

    /**
     * Gets the FluidStacks inside the ItemStack container.
     * @param itemStack The ItemStack.
     * @return The FluidStack in the ItemStack fluid container.
     */
    @Nonnull
    private static List<FluidStack> getFluidFromItemStack(ItemStack itemStack) {
        IFluidHandlerItem handler = itemStack.getCapability(Capabilities.FluidHandler.ITEM);
        if (itemStack.isEmpty() || handler == null) {
            return Collections.emptyList();
        }

        List<FluidStack> fluids = new ArrayList<>();

        for (int tank = 0; tank < handler.getTanks(); tank++) {
            FluidStack contents = handler.getFluidInTank(tank);
            if (!contents.isEmpty()) {
                fluids.add(contents);
            }
        }
        return fluids;
    }

    /**
     * Fills the IFluidTank with an ItemStack fluid container.
     * @param container The ItemStack holding the fluid.
     * @param tank The tank to fill.
     * @param drainContainer Whether to actually drain the `container` ItemStack.
     * @return The modified ItemStack, which probably has no fluid in it anymore.
     */
    public static ItemStack fillTankFromItem(ItemStack container, IFluidTank tank, boolean drainContainer) {
        IFluidHandlerItem handler = container.getCapability(Capabilities.FluidHandler.ITEM);

        if (container.isEmpty() || handler == null) {
            return container;
        }

        int roomLeftInContainer = getRoomLeftInTank(tank);

        if (roomLeftInContainer > 0) {
            FluidStack drained = handler.drain(roomLeftInContainer, drainContainer ? IFluidHandler.FluidAction.EXECUTE : IFluidHandler.FluidAction.SIMULATE);
            tank.fill(drained, IFluidHandler.FluidAction.EXECUTE);
        }

        return handler.getContainer();
    }

    /**
     * Gets how much room there is in the tank for more fluids.
     * @param tank The tank to check.
     * @return How much space there is in the tank for more fluid.
     */
    private static int getRoomLeftInTank(IFluidTank tank) {
        return tank.getCapacity() - tank.getFluidAmount();
    }

    /**
     * Gets the still texture for the fluid
     * @param mc Minecraft instance
     * @param fluid Fluid to get the texture of
     * @return the texture
     */
    public static TextureAtlasSprite getStillTexture(Minecraft mc, FluidStack fluid) {
        IClientFluidTypeExtensions fluidExt = IClientFluidTypeExtensions.of(fluid.getFluid());
        return mc.getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(fluidExt.getStillTexture(fluid));
    }

    /**
     * Checks whether the block is an infinite source block of water.
     * @param level The level
     * @param pos The block that is being tested against
     * @return Whether the given pos can be treated as an infinite source of water. For example, if the water is "XYZ",
     *         only "Y" will return true.
     */
    public static boolean isInfiniteWaterSource(Level level, BlockPos pos) {
        FluidState originFluidState = level.getFluidState(pos);
        if (!originFluidState.is(FluidTags.WATER)) {
            return false;
        }

        List<BlockPos> adjacent = new ArrayList<>(Arrays.asList(pos.north(), pos.south(), pos.east(), pos.west()));

        int sourceBlocks = 0;

        for (BlockPos blockToCheck : adjacent) {
            FluidState fluidStateToCheck = level.getFluidState(blockToCheck);
            if (fluidStateToCheck.is(FluidTags.WATER) && Fluids.WATER.isSource(fluidStateToCheck)) {
                sourceBlocks++;
            }
        }

        return sourceBlocks >= 2;
    }

    /**
     * Recursively scans the blocks around the given starting block to check if they are fluids.
     * @param level The level
     * @param start The block to start scanning at. It will check all directions adjacent to it except down.
     * @param fluid The fluid in the block
     * @param blocksChecked The list of blocks that have already been checked. If calling for the first time, call with an empty set.
     * @return The BlockPos that is a source block. If none is found, returns null.
     * @author Originally written by squeek502 for The Vegan Option, maintained by SatanicSanta
     */
    public static BlockPos findSourceBlockPos(Level level, BlockPos start, Fluid fluid, Set<BlockPos> blocksChecked) {
        FluidState originFluidState = level.getFluidState(start);
        if (originFluidState.isSourceOfType(fluid)) {
            return start;
        }

        List<BlockPos> blocksToCheck = new ArrayList<>(Arrays.asList(
          start.above(),
          start.north(),
          start.south(),
          start.east(),
          start.west()
        ));

        for (BlockPos blockToCheck : blocksToCheck) {
            FluidState fluidStateToCheck = level.getFluidState(blockToCheck);
            if (fluidStateToCheck.getFluidType() == fluid.getFluidType() && !blocksChecked.contains(blockToCheck)) {
                if (fluidStateToCheck.isSource()) {
                    return blockToCheck;
                } else {
                    blocksChecked.add(blockToCheck);
                    BlockPos foundSourceBlock = findSourceBlockPos(level, blockToCheck, fluid, blocksChecked);

                    if (foundSourceBlock != null) {
                        return foundSourceBlock;
                    }
                }
            }
        }
        return null;
    }

    /**
     * @param blockEntity The block entity
     * @param dir The direction
     * @return An {@link IFluidHandler} for the BE and direction.
     */
    public static IFluidHandler getFluidHandler(BlockEntity blockEntity, Level level, BlockPos pos, Direction dir) {
        if (blockEntity == null) {
            return null;
        }
        if (blockEntity instanceof IFluidHandler) {
            return (IFluidHandler) blockEntity;
        }
        return level.getCapability(Capabilities.FluidHandler.BLOCK, pos, dir);
    }
}
