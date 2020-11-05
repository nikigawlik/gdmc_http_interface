package com.gdmc.httpinterfacemod.utils;

import com.gdmc.httpinterfacemod.settlementcommand.CommandExample;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.CommandSource;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;

public class RegistryHandler {

    public static void registerCommands(FMLServerStartingEvent event) {

        // TODO might be wrong (didn't test)
        CommandDispatcher<CommandSource> dispatcher = event.getServer().getCommandManager().getDispatcher();
        // maybe try this instead:
//        CommandDispatcher<CommandSource> dispatcher = event.getServer().getFunctionManager().getCommandDispatcher();


//        BuildSettlementCommand.register(dispatcher);
        CommandExample.register(dispatcher);


    }

}
