package com.gdmc.httpinterfacemod.handlers;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import net.minecraft.command.arguments.BlockStateArgument;
import net.minecraft.command.arguments.BlockStateInput;
import net.minecraft.inventory.IClearable;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

public class SetBlockHandler extends HandlerBase {
    private static final SimpleCommandExceptionType FAILED_EXCEPTION = new SimpleCommandExceptionType(new TranslationTextComponent("commands.setblock.failed"));

    public SetBlockHandler(MinecraftServer mcServer) {
        super(mcServer);
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        String method = httpExchange.getRequestMethod().toLowerCase();

        // look at incoming request
        Map<String, String> queryParams = parseQueryString(httpExchange.getRequestURI().getRawQuery());

        String responseString = "";

        int statusCode = 200;

        int x = 0;
        int y = 0;
        int z = 0;

        InputStream bodyStream = httpExchange.getRequestBody();
        String blockID = new BufferedReader(new InputStreamReader(bodyStream))
                .lines().collect(Collectors.joining());

        try {
            x = Integer.parseInt(queryParams.get("x"));
            y = Integer.parseInt(queryParams.get("y"));
            z = Integer.parseInt(queryParams.get("z"));
        } catch (NumberFormatException e) {
            responseString = "Could not parse query parameter: " + e.getMessage();
            statusCode = 400;
        }

        BlockStateInput bsi;
        try {
            bsi = BlockStateArgument.blockState().parse(new StringReader(blockID));

            // construct response
            if(statusCode == 200) {
                if(method.equals("post")) {
                    responseString = setBlock(new BlockPos(x, y, z), bsi) + "";

                } else {
                    statusCode = 405;
                    responseString = "Method not allowed. Only POST requests are supported.";
                }
            }
        } catch (CommandSyntaxException e) {
            responseString = "Could not parse block state: " + e.getMessage();
            statusCode = 400;
        }

        //headers
        Headers headers = httpExchange.getResponseHeaders();

        headers.add("Content-Type", "text/raw; charset=UTF-8");


        // body

        byte[] responseBytes = responseString.getBytes(StandardCharsets.UTF_8);

        httpExchange.sendResponseHeaders(statusCode, responseBytes.length);
        OutputStream outputStream = httpExchange.getResponseBody();
        outputStream.write(responseBytes);
        outputStream.close();
    }

    private int setBlock(BlockPos pos, BlockStateInput state) throws CommandSyntaxException {
        ServerWorld serverWorld = mcServer.getWorld(World.OVERWORLD);

        assert serverWorld != null;
        TileEntity tileentity = serverWorld.getTileEntity(pos);
        IClearable.clearObj(tileentity);

        if (!state.place(serverWorld, pos, 2)) {
            throw FAILED_EXCEPTION.create();
        } else {
            serverWorld.func_230547_a_(pos, state.getState().getBlock());
//            (new TranslationTextComponent(
//                    "commands.setblock.success",
//                    pos.getX(), pos.getY(), pos.getZ())).getString();
            return 1;
        }
    }
}
