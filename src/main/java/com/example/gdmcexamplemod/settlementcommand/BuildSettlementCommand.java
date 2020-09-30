package com.example.gdmcexamplemod.settlementcommand;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.BlockPosArgument;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BuildSettlementCommand<S> {

    private static final String COMMAND_NAME = "buildsettlement";
    private static final Logger LOGGER = LogManager.getLogger();

    private BuildSettlementCommand() { }

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(
                Commands.literal(COMMAND_NAME)
                .then(Commands.argument("from", BlockPosArgument.blockPos())
                .then(Commands.argument("to", BlockPosArgument.blockPos())
                .executes( context -> {
                    return perform(context, BlockPosArgument.getLoadedBlockPos(context, "from"), BlockPosArgument.getLoadedBlockPos(context, "to"));
                }))));
    }

    private static int perform(CommandContext<CommandSource> commandSourceContext, BlockPos from, BlockPos to) {

        ServerWorld world = commandSourceContext.getSource().getServer().getWorld(DimensionType.OVERWORLD);
        CommandSource source = commandSourceContext.getSource();
        int x1 = from.getX(), y1 = from.getY(), z1 = from.getZ();
        int x2 = to.getX(), y2 = to.getY(), z2 = to.getZ();

        int result = buildSettlement(source, world, x1, y1, z1, x2, y2, z2);

        if (result > 0) {
            LOGGER.info("BuildSettlement command has been performed");
        } else {
            LOGGER.error("BuildSettlement command has failed");
        }

        return result;
    }

    private static int buildSettlement(CommandSource source, ServerWorld world, int x1, int y1, int z1, int x2, int y2, int z2) {

        source.getEntity().sendMessage(new StringTextComponent("Hello World!"));
        source.getEntity().sendMessage(new StringTextComponent(String.format("You want to build a settlement from %d, %d, %d to %d, %d, %d,", x1, y1, z1, x2, y2, z2 )));

        // TODO: generate a settlement

        return 1;
    }
}
