package eiteam.esteemedinnovation.tools.steam.upgrades;

import eiteam.esteemedinnovation.api.tool.SteamTool;
import eiteam.esteemedinnovation.api.tool.SteamToolSlot;
import eiteam.esteemedinnovation.commons.util.WorldHelper;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.BlockEvent;

import javax.annotation.Nonnull;

import static eiteam.esteemedinnovation.tools.ToolsModule.upgradeResource;

public class ItemRotaryBladesUpgrade extends ItemSteamToolUpgrade {
    public ItemRotaryBladesUpgrade() {
        super(SteamToolSlot.SHOVEL_HEAD, upgradeResource("rotary"), null, 1);
    }

    @Override
    public boolean onBlockBreakWithTool(BlockEvent.BreakEvent event, @Nonnull ItemStack toolStack, @Nonnull ItemStack thisUpgradeStack) {
        EntityPlayer player = event.getPlayer();
        World world = event.getWorld();
        IBlockState state = event.getState();
        Block block = state.getBlock();
        SteamTool tool = (SteamTool) toolStack.getItem();

        RayTraceResult ray = tool.rayTrace(world, player, false);
        if (ray != null && block.isToolEffective(((SteamTool) toolStack.getItem()).toolClass(), state)) {
            WorldHelper.mineExtraBlocks(WorldHelper.getExtraBlockCoordinates(ray.sideHit), event.getPos(), world, (ItemTool) tool, toolStack, player);
        }

        return true;
    }

    @Override
    public void onUpdateBreakSpeedWithTool(PlayerEvent.BreakSpeed event, @Nonnull ItemStack toolStack, @Nonnull ItemStack thisUpgradeStack) {
        event.setNewSpeed(event.getNewSpeed() * 0.425F);
    }
}
