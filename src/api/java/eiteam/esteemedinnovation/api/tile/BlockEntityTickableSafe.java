package eiteam.esteemedinnovation.api.tile;

import eiteam.esteemedinnovation.api.wrench.PipeWrench;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Base BlockEntity class that ensures that no tick code is executed when the block in its occupied position is not
 * the expected one (usually, when this issue arises, it is air.)
 * <p>
 * You will still need to set up a ticker in your EntityBlock class. Use {@link BlockEntityTickableSafe#onTick(Level, BlockPos, BlockState, BlockEntityTickableSafe)}
 * for your safe ticker.
 * <p>
 * This should only be necessary to use (as an alternative to {@link BlockEntityBase} with a ticker) when you need to
 * access block state values within the update code.
 */
public abstract class BlockEntityTickableSafe extends BlockEntityBase {
    private boolean isInitialized;

    public BlockEntityTickableSafe(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    /**
     * @param target The block state that is in the position of this tile entity.
     * @return Whether we are safe to execute the ticking code for the provided block state.
     */
    public abstract boolean canUpdate(BlockState target);

    /**
     * Called on every tick when {@link #canUpdate(BlockState)} is true.
     */
    public abstract void safeUpdate();

    /**
     * Like {@link BlockEntity#onLoad()}, except that it is safe (see {@link #safeUpdate()}) and happens after the world
     * has loaded. onLoad() occurs during the world loading process, so you cannot access any blockstate values or
     * anything of that sort. This is designed to be used for initialization stuff.
     * <p>
     * This is called before {@link #safeUpdate()} is called.
     * <p>
     * You must either call the supermethod or {@link #setInitialized(boolean)} in your implementation, or at some
     * point during update or safeUpdate, otherwise this method will repeatedly get called.
     * <p>
     * This is not necessarily the first update. Rather, it is the first update after the TE has been uninitialized. It
     * just happens to also begin uninitialized. It truly is called whenever an update occurs and the TE is not
     * initialized (see {@link #isInitialized()}).
     * <p>
     * For example, you might have a device that sets some value in the BE based on the block's facing value. The
     * facing value probably won't change, so it doesn't make sense to retrieve that value every update. Using this,
     * you would store the value once, and then simply use it in {@link #safeUpdate()} when you need. However, if you
     * had some custom behavior utilizing a {@link PipeWrench} that would rotate the block, you would need to reset
     * that value in the BE. You can do that by having a proxy method in the BE that calls {@link #setInitialized(boolean)}
     * (since that method is protected for security reasons; more on that later) so that the next update resets the
     * value in the BE.
     * <p>
     * Here is a code sample for the scenario previously described:
     * <pre>
     * <code>
     *     // SomeBlockEntity.java
     *     class SomeBlockEntity extends BlockEntityTickableSafe {
     *         private Direction facing;
     *
     *         {@literal @}Override
     *         public void initialUpdate() {
     *             super.initialUpdate();
     *             facing = world.getBlockState(pos).getValue(SomeBlock.FACING);
     *         }
     *
     *         {@literal @}Override
     *         public void safeUpdate() {
     *             // Destroys the block in the direction it is facing, for example.
     *             world.destroyBlock(pos.relative(facing), true);
     *         }
     *
     *         void uninitialize() {
     *             setInitialized(false);
     *         }
     *     }
     *
     *     // SomeBlock.java, in same package
     *     class SomeBlock extends BlockEntity implements Wrenchable {
     *         static final PropertyDirection FACING = BlockDirectional.FACING;
     *
     *         // BlockState implementations
     *
     *         {@literal @}Override
     *         public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
     *             return type == MY_TYPE ? BlockEntityTickableSafe::onTick : null;
     *
     *         {@literal @}Override
     *         public boolean onWrench(ItemStack stack, Player player, Level world, BlockPos pos, HumanoidArm hand, Direction facing, BlockState state, float hitX, float hitY, float hitZ) {
     *             // Rotate the block
     *             world.setBlockState(pos, state.withProperty(FACING, state.getValue(FACING).rotateY()));
     *             TileEntity te = world.getBlockEntity(pos);
     *             if (te instanceof SomeBlockEntity) {
     *                 // Uninitialize the block entity since our facing value has changed.
     *                 ((SomeTileEntity) te).uninitialize();
     *             }
     *         }
     *     }
     * </code>
     * </pre>
     */
    public void initialUpdate() {
        setInitialized(true);
    }

    protected void setInitialized(boolean value) {
        isInitialized = value;
    }

    protected boolean isInitialized() {
        return isInitialized;
    }

    public static <T extends BlockEntityTickableSafe> void onTick(Level level, BlockPos blockPos, BlockState blockState, T t) {
        if (t.canUpdate(level.getBlockState(t.getBlockPos()))) {
            if (!t.isInitialized()) {
                t.initialUpdate();
            }
            t.safeUpdate();
        }
    }
}
