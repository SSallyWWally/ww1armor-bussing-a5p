package com.exodusvehicles.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

/**
 * A stack of shells (up to 21: rows of 6, 5, 4, 3, 2, 1). Right-click takes one off the top, right-click with a shell
 * puts one back; the last one taken clears the pile. Broken, it drops the shells it still holds.
 * <p>
 * The 21 cm pile is long and takes a 2x2 footprint: the head block holds the count and draws the pile, the other
 * three are solid placeholders so piles butt up against each other evenly instead of overlapping. Clicking or breaking
 * any of the four acts on the whole pile. The 7.7 cm pile fits in one block.
 */
public class ShellPileBlock extends Block {
    public static final int MAX = 21;
    public static final IntegerProperty SHELLS = IntegerProperty.create("shells", 1, MAX);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    /** 0 = head (holds the count, draws the pile), 1 = right, 2 = forward, 3 = right + forward. */
    public static final IntegerProperty PART = IntegerProperty.create("part", 0, 3);
    private static final int[] ROW_ENDS = {6, 11, 15, 18, 20, 21};

    private final boolean big;
    private final Supplier<Item> shell;
    private final VoxelShape[] shapes = new VoxelShape[MAX + 1];

    public ShellPileBlock(Properties properties, boolean big, Supplier<Item> shell) {
        super(properties);
        this.big = big;
        this.shell = shell;
        double d = big ? 4.6 : 2.3;
        for (int n = 1; n <= MAX; n++) {
            int rows = 0;
            while (rows < ROW_ENDS.length && ROW_ENDS[rows] < n) {
                rows++;
            }
            double top = Math.min(24.0, d + rows * d * 0.866);
            this.shapes[n] = big ? Block.box(0, 0, 0, 16, top, 16) : Block.box(1, 0, 1, 15, top, 15);
        }
        BlockState def = this.stateDefinition.any().setValue(SHELLS, MAX).setValue(FACING, Direction.NORTH);
        if (big) {
            def = def.setValue(PART, 0);
        }
        registerDefaultState(def);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(SHELLS, FACING);
        // PART only on the big pile (the field pile's states have no such property); added for both, unused by the small
        builder.add(PART);
    }

    // ------------------------------------------------------------------ multiblock layout

    private static BlockPos offset(BlockPos head, Direction facing, int part) {
        BlockPos p = head;
        if ((part & 1) != 0) {
            p = p.relative(facing.getClockWise());
        }
        if ((part & 2) != 0) {
            p = p.relative(facing);
        }
        return p;
    }

    private BlockPos headOf(BlockPos pos, BlockState state) {
        if (!this.big) {
            return pos;
        }
        Direction f = state.getValue(FACING);
        int part = state.getValue(PART);
        BlockPos p = pos;
        if ((part & 1) != 0) {
            p = p.relative(f.getCounterClockWise());
        }
        if ((part & 2) != 0) {
            p = p.relative(f.getOpposite());
        }
        return p;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        Direction f = ctx.getHorizontalDirection();
        BlockState s = this.defaultBlockState().setValue(FACING, f);
        if (this.big) {
            Level level = ctx.getLevel();
            for (int part = 1; part < 4; part++) {
                BlockPos p = offset(ctx.getClickedPos(), f, part);
                if (!level.getBlockState(p).canBeReplaced(ctx) || !level.getWorldBorder().isWithinBounds(p)) {
                    return null;     // no room for the whole pile
                }
            }
        }
        return s;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (this.big && !level.isClientSide) {
            for (int part = 1; part < 4; part++) {
                level.setBlock(offset(pos, state.getValue(FACING), part), state.setValue(PART, part), 3);
            }
        }
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return true;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        int n = state.getValue(SHELLS);
        if (this.big && state.getValue(PART) != 0) {
            BlockState head = level.getBlockState(headOf(pos, state));
            if (head.is(this)) {
                n = head.getValue(SHELLS);
            }
        }
        return this.shapes[n];
    }

    // ------------------------------------------------------------------ use

    @Nullable
    private BlockPos headPos(Level level, BlockPos pos, BlockState state) {
        BlockPos h = headOf(pos, state);
        BlockState hs = level.getBlockState(h);
        return hs.is(this) && (!this.big || hs.getValue(PART) == 0) ? h : null;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
                                              InteractionHand hand, BlockHitResult hit) {
        BlockPos h = headPos(level, pos, state);
        if (h == null || !stack.is(this.shell.get())) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        BlockState hs = level.getBlockState(h);
        if (hs.getValue(SHELLS) >= MAX) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (!level.isClientSide) {
            level.setBlock(h, hs.setValue(SHELLS, hs.getValue(SHELLS) + 1), 3);
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            level.playSound(null, pos, SoundEvents.CHAIN_PLACE, SoundSource.BLOCKS, 0.8F, this.big ? 0.7F : 1.1F);
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        BlockPos h = headPos(level, pos, state);
        if (h == null) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide) {
            BlockState hs = level.getBlockState(h);
            int n = hs.getValue(SHELLS);
            ItemStack s = new ItemStack(this.shell.get());
            if (!player.getInventory().add(s)) {
                player.drop(s, false);
            }
            if (n <= 1) {
                removeAll(level, h, hs);
            } else {
                level.setBlock(h, hs.setValue(SHELLS, n - 1), 3);
            }
            level.playSound(null, pos, SoundEvents.CHAIN_STEP, SoundSource.BLOCKS, 0.9F, this.big ? 0.6F : 1.0F);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private void removeAll(Level level, BlockPos head, BlockState hs) {
        if (this.big) {
            for (int part = 1; part < 4; part++) {
                BlockPos p = offset(head, hs.getValue(FACING), part);
                BlockState ps = level.getBlockState(p);
                if (ps.is(this) && ps.getValue(PART) == part) {
                    level.setBlock(p, Blocks.AIR.defaultBlockState(), 3 | 32);
                }
            }
        }
        level.setBlock(head, Blocks.AIR.defaultBlockState(), 3 | 32);
    }

    // ------------------------------------------------------------------ breaking

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide) {
            BlockPos h = headPos(level, pos, state);
            if (h != null && !player.getAbilities().instabuild) {
                int n = level.getBlockState(h).getValue(SHELLS);
                for (int i = 0; i < n; i++) {
                    Block.popResource(level, pos, new ItemStack(this.shell.get()));
                }
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    /** One part gone (broken, blown up, replaced): the rest of the pile goes with it. */
    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
        super.onRemove(state, level, pos, newState, moved);
        if (!this.big || level.isClientSide || newState.is(this)) {
            return;
        }
        BlockPos head = headOf(pos, state);
        Direction f = state.getValue(FACING);
        for (int part = 0; part < 4; part++) {
            BlockPos p = offset(head, f, part);
            if (p.equals(pos)) {
                continue;
            }
            BlockState ps = level.getBlockState(p);
            if (ps.is(this) && ps.getValue(PART) == part && ps.getValue(FACING) == f) {
                level.setBlock(p, Blocks.AIR.defaultBlockState(), 3 | 32);
            }
        }
    }
}
