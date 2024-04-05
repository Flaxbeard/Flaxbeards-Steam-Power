package eiteam.esteemedinnovation.fishfarm;

import eiteam.esteemedinnovation.api.tile.SteamTransporterBlockEntity;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootTableList;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

public class TileEntityFishGenocideMachine extends SteamTransporterBlockEntity {
    public TileEntityFishGenocideMachine() {
        super(new EnumFacing[] {
          EnumFacing.UP,
          EnumFacing.DOWN
        });

        addSidesToGaugeBlacklist(EnumFacing.HORIZONTALS);
    }

    /**
     * @return A new Pair: Chunk, # of water source blocks in chunk at the Y position.
     */
    private Pair<Chunk, Integer> randSourceBlock() {
        List<Chunk> chunks = new ArrayList<>();
        int water = 0;
        for (int x = -3; x < 4; x++) {
            for (int z = -3; z < 4; z++) {
                BlockPos pos = new BlockPos(this.pos.getX() + x, this.pos.getY(), this.pos.getZ() + z);
                if (world.getBlockState(pos).getBlock() == Blocks.WATER) {
                    chunks.add(new Chunk(world, pos.getX(), pos.getZ()));
                    water++;
                }
            }
        }
        return Pair.of(chunks.get(world.rand.nextInt(chunks.size())), water);
    }

    @Override
    public boolean canUpdate(BlockState target) {
        return false; // TODO
    }

    @Override
    public void safeUpdate() {
        Pair<Chunk, Integer> pair = randSourceBlock();
        int src = pair.getRight();
        if (getSteamShare() > src) {
            decrSteam(src);
            if (world.rand.nextInt((int) (300.0F / src)) == 0 && !world.isRemote) {
                Chunk loc = pair.getLeft();
                LootContext lootContext = new LootContext.Builder((WorldServer) world).build();

                List<ItemStack> fishes = world.getLootTableManager().getLootTableFromLocation(
                  LootTableList.GAMEPLAY_FISHING_FISH).generateLootForPools(world.rand, lootContext);
                ItemStack output = fishes.get(world.rand.nextInt(fishes.size()));

                ItemStack smeltingResult = FurnaceRecipes.instance().getSmeltingResult(output);
                if (smeltingResult != null) {
                    output = smeltingResult;
                }
                dropItem(output, loc.x + 0.5F, pos.getY() + 1.0F, loc.z + 0.5F);
            }
        }
        super.safeUpdate();
    }

    /**
     * Drops a floating item of the ItemStack at the position.
     * @param item The ItemStack
     * @param x X position
     * @param y Y position
     * @param z Z position
     */
    public void dropItem(ItemStack item, float x, float y, float z) {
        EntityFloatingItem entityItem = new EntityFloatingItem(world, x, y, z, item);
        world.spawnEntity(entityItem);
    }
}
