package com.gdmc.httpinterfacemod;

import com.gdmc.httpinterfacemod.utils.RegistryHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Util;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
import net.minecraftforge.fml.loading.FMLCommonLaunchHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("gdmchttp")
public class GdmcHttpMod
{
    public static final String MODID = "gdmchttp";

    // Directly reference a log4j logger.
    private static final Logger LOGGER = LogManager.getLogger();

    public GdmcHttpMod() {
        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) {
        // do something when the server starts
        LOGGER.info("HELLO from server starting");
        RegistryHandler.registerCommands(event);
        MinecraftServer minecraftServer = event.getServer();

        try {
            GdmcHttpServer.startServer(minecraftServer);
            minecraftServer.sendMessage(new StringTextComponent("GDMC Server started successfully."), Util.field_240973_b_);
        } catch (IOException e) {
            LOGGER.warn("Unable to start server!");
            minecraftServer.sendMessage(new StringTextComponent("GDMC Server failed to start!"), Util.field_240973_b_);
        }
    }

    @SubscribeEvent
    public void onServerStopping(FMLServerStoppingEvent event) {
        LOGGER.info("HELLO from server stopping");

        GdmcHttpServer.stopServer();
    }
}
