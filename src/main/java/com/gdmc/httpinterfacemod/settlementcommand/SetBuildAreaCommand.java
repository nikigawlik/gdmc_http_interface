package com.gdmc.httpinterfacemod.settlementcommand;

import com.gdmc.httpinterfacemod.handlers.BuildAreaHandler;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.BlockPosArgument;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SetBuildAreaCommand<S> {

    private static final String COMMAND_NAME = "setbuildarea";
    private static final Logger LOGGER = LogManager.getLogger();

    private SetBuildAreaCommand() { }

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(
                Commands.literal(COMMAND_NAME)
                .then(Commands.argument("from", BlockPosArgument.blockPos())
                .then(Commands.argument("to", BlockPosArgument.blockPos())
                .executes( context -> {
                    return perform(context, BlockPosArgument.getBlockPos(context, "from"), BlockPosArgument.getBlockPos(context, "to"));
                }))));
    }

    private static int perform(CommandContext<CommandSource> commandSourceContext, BlockPos from, BlockPos to) {
        int x1 = from.getX();
        int y1 = from.getY();
        int z1 = from.getZ();
        int x2 = to.getX();
        int y2 = to.getY();
        int z2 = to.getZ();

        BuildAreaHandler.setBuildArea(x1, y1, z1, x2, y2, z2);
        String feedback = String.format("Build area set to %d, %d, %d to %d, %d, %d,", x1, y1, z1, x2, y2, z2 );
        commandSourceContext.getSource().sendFeedback(new StringTextComponent(feedback), true);
        LOGGER.info(feedback);
        return 1;
    }
}
