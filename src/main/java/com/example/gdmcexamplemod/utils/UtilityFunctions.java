package com.example.gdmcexamplemod.utils;

import javafx.util.Pair;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.command.arguments.BlockStateInput;
import net.minecraft.state.IProperty;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.*;

public abstract class UtilityFunctions {

    @SafeVarargs
    public static <T extends Comparable<T>, V extends T> int setBlock(World world, Block block, int x, int y, int z, Pair<IProperty<?>, ?>... properties) {
        BlockState blockState = block.getDefaultState();
        for (Pair<IProperty<?>, ?> propertyValue : properties)
            try {
                blockState = blockState.with((IProperty<T>) propertyValue.getKey(), (V) propertyValue.getValue());
            } catch (ClassCastException e) {

            }

        boolean isSuccess = world.setBlockState(new BlockPos(x, y, z), blockState);
        return (isSuccess) ? 1 : 0;
    }

}
