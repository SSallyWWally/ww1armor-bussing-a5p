package com.exodusvehicles.block;

import com.exodusvehicles.ArmoredCarMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A stack of 21 cm shells (up to 9: rows of 4, 3 and 2). Right-click takes one off the top, right-click with a shell
 * puts one back; the last one taken clears the pile. Broken, it drops the shells it still holds.
 */
public class ShellPileBlock extends Block {
    public static final int MAX = 9;
    public static final IntegerProperty SHELLS = IntegerProperty.create("shells", 1, MAX);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    private static final VoxelShape LOW = Block.box(0, 0, 0, 16, 4, 16);
    private static final VoxelShape MID = Block.box(0, 0, 0, 16, 7, 16);
    private static final VoxelShape HIGH = Block.box(0, 0, 0, 16, 10, 16);

    public ShellPileBlock(Properties properties) {
        super(properties);
        registerDefaultState(this.stateDefinition.any().setValue(SHELLS, MAX).setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(SHELLS, FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return this.defaultBlockState().setValue(FACING, ctx.getHorizontalDirection());
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        int n = state.getValue(SHELLS);
        return n <= 4 ? LOW : n <= 7 ? MID : HIGH;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
                                              InteractionHand hand, BlockHitResult hit) {
        if (stack.is(ArmoredCarMod.SHELL_ITEM) && state.getValue(SHELLS) < MAX) {
            if (!level.isClientSide) {
                level.setBlock(pos, state.setValue(SHELLS, state.getValue(SHELLS) + 1), 3);
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                level.playSound(null, pos, SoundEvents.CHAIN_PLACE, SoundSource.BLOCKS, 0.8F, 0.7F);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide) {
            int n = state.getValue(SHELLS);
            ItemStack shell = new ItemStack(ArmoredCarMod.SHELL_ITEM);
            if (!player.getInventory().add(shell)) {
                player.drop(shell, false);
            }
            if (n <= 1) {
                level.removeBlock(pos, false);
            } else {
                level.setBlock(pos, state.setValue(SHELLS, n - 1), 3);
            }
            level.playSound(null, pos, SoundEvents.CHAIN_STEP, SoundSource.BLOCKS, 0.9F, 0.6F);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide && !player.getAbilities().instabuild) {
            Block.popResource(level, pos, new ItemStack(ArmoredCarMod.SHELL_ITEM, state.getValue(SHELLS)));
        }
        return super.playerWillDestroy(level, pos, state, player);
    }
}
