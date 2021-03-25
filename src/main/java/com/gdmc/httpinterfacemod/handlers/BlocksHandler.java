package com.gdmc.httpinterfacemod.handlers;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import net.minecraft.block.BlockState;
import net.minecraft.command.CommandSource;
import net.minecraft.command.arguments.BlockPosArgument;
import net.minecraft.command.arguments.BlockStateArgument;
import net.minecraft.command.arguments.BlockStateInput;
import net.minecraft.command.arguments.ILocationArgument;
import net.minecraft.inventory.IClearable;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

//new CachedBlockInfo(p_210438_0_.getSource().getWorld(), BlockPosArgument.getLoadedBlockPos(p_210438_0_, "pos"), true)

public class BlocksHandler extends HandlerBase {
//    private static final SimpleCommandExceptionType FAILED_EXCEPTION = new SimpleCommandExceptionType(new TranslationTextComponent("commands.setblock.failed"));
    private final CommandSource cmdSrc;

    public BlocksHandler(MinecraftServer mcServer) {
        super(mcServer);

        cmdSrc = createCommandSource("GDMC-BlockHandler", mcServer);
    }

    @Override
    public void internalHandle(HttpExchange httpExchange) throws IOException {
        String method = httpExchange.getRequestMethod().toLowerCase();

        // look at incoming request
        Map<String, String> queryParams = parseQueryString(httpExchange.getRequestURI().getRawQuery());

        String responseString = "";

        int statusCode = 200;

        int x = 0;
        int y = 0;
        int z = 0;
        boolean doBlockUpdates = true;
        boolean spawnDrops = false;
        int customFlags = -1; // -1 == no custom flags

        try {
            x = Integer.parseInt(queryParams.getOrDefault("x", "0"));
            y = Integer.parseInt(queryParams.getOrDefault("y", "0"));
            z = Integer.parseInt(queryParams.getOrDefault("z", "0"));
            doBlockUpdates = Boolean.parseBoolean(queryParams.getOrDefault("doBlockUpdates", Boolean.toString(doBlockUpdates)));
            spawnDrops = Boolean.parseBoolean(queryParams.getOrDefault("spawnDrops", Boolean.toString(spawnDrops)));
            // TODO: Uncomment this to enable the customFlags feature!
//            customFlags = Integer.parseInt(queryParams.getOrDefault("customFlags", Integer.toString(customFlags)), 2);
        } catch (NumberFormatException e) {
            responseString = "Could not parse query parameter: " + e.getMessage();
            statusCode = 400;
        }

        if(statusCode == 200) {
            // construct response
            if(method.equals("put")) {
                InputStream bodyStream = httpExchange.getRequestBody();
                List<String> body = new BufferedReader(new InputStreamReader(bodyStream))
                        .lines().collect(Collectors.toList());

                List<String> returnValues = new LinkedList<>();

                int blockFlags = customFlags >= 0? customFlags : getBlockFlags(doBlockUpdates, spawnDrops);

                for(String line : body) {
                    String returnValue;
                    try {
                        StringReader sr = new StringReader(line);
                        ILocationArgument li = null;
                        try {
                            li = BlockPosArgument.blockPos().parse(sr);
                            sr.skip();
                        } catch (CommandSyntaxException e) {
                            sr = new StringReader(line); // TODO maybe delete this
                        }
                        BlockStateInput bsi = BlockStateArgument.blockState().parse(sr);

                        CommandSource cs = cmdSrc.withPos(new Vector3d(x, y, z));

                        int xx, yy, zz;
                        if(li != null) {
                            xx = (int)Math.round(li.getPosition(cs).x);
                            yy = (int)Math.round(li.getPosition(cs).y);
                            zz = (int)Math.round(li.getPosition(cs).z);
                        } else {
                            xx = x;
                            yy = y;
                            zz = z;
                        }

                        returnValue = setBlock(new BlockPos(xx, yy, zz), bsi, blockFlags) + "";
                    } catch (CommandSyntaxException e) {
//                        // could be either from parsing or from placing
//                        responseString = e.getMessage();
//                        statusCode = 400;
                        returnValue = e.getMessage();
                    }
                    returnValues.add(returnValue);
                }
                responseString = String.join("\n", returnValues);
            } else if(method.equals("get")) {
                responseString = getBlock(new BlockPos(x, y, z)) + "";
            } else{
                statusCode = 405;
                responseString = "Method not allowed. Only PUT and GET requests are supported.";
            }
        }

        //headers
        Headers headers = httpExchange.getResponseHeaders();

        addDefaultHeaders(headers);
        headers.add("Content-Type", "text/plain; charset=UTF-8");

        // body

        byte[] responseBytes = responseString.getBytes(StandardCharsets.UTF_8);

        httpExchange.sendResponseHeaders(statusCode, responseBytes.length);
        OutputStream outputStream = httpExchange.getResponseBody();
        outputStream.write(responseBytes);
        outputStream.close();
    }

    private int setBlock(BlockPos pos, BlockStateInput state, int flags) {
        ServerWorld serverWorld = mcServer.getWorld(World.OVERWORLD);

        assert serverWorld != null;
        TileEntity tileentity = serverWorld.getTileEntity(pos);
        IClearable.clearObj(tileentity);

        if (!state.place(serverWorld, pos, flags)) {
            return 0;
        } else {
            return 1;
//            (new TranslationTextComponent(
//                    "commands.setblock.success",
//                    pos.getX(), pos.getY(), pos.getZ())).getString();
        }
    }

    public int getBlockFlags(boolean doBlockUpdates, boolean spawnDrops) {
        /*
            flags:
                * 1 will cause a block update.
                * 2 will send the change to clients.
                * 4 will prevent the block from being re-rendered.
                * 8 will force any re-renders to run on the main thread instead
                * 16 will prevent neighbor reactions (e.g. fences connecting, observers pulsing).
                * 32 will prevent neighbor reactions from spawning drops.
                * 64 will signify the block is being moved.
        */
        // construct flags
        return 2 | ( doBlockUpdates? 1 : (32 | 16) ) | ( spawnDrops? 0 : 32 );
    }

    private String getBlock(BlockPos pos) {
        ServerWorld serverWorld = mcServer.getWorld(World.OVERWORLD);

        assert serverWorld != null;

        BlockState bs = serverWorld.getBlockState(pos);

        return String.valueOf(bs.getBlock().getRegistryName());
    }
}
