package com.example.gdmcexamplemod.utils;

import com.mojang.brigadier.CommandDispatcher;
import com.example.gdmcexamplemod.settlementcommand.BuildSettlementCommand;
import net.minecraft.command.CommandSource;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;

public class RegistryHandler {

    public static void registerCommands(FMLServerStartingEvent event) {

        CommandDispatcher<CommandSource> dispatcher = event.getCommandDispatcher();

        BuildSettlementCommand.register(dispatcher);


    }

}
