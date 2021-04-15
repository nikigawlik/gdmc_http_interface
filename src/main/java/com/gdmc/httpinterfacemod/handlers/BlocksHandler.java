package com.gdmc.httpinterfacemod.handlers;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
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
import net.minecraft.state.Property;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import org.apache.commons.lang3.tuple.ImmutablePair;

import javax.annotation.Nullable;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
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

        // query parameters
        Map<String, String> queryParams = parseQueryString(httpExchange.getRequestURI().getRawQuery());
        int x;
        int y;
        int z;
        boolean includeState;
        boolean doBlockUpdates;
        boolean spawnDrops;
        int customFlags; // -1 == no custom flags

        try {
            x = Integer.parseInt(queryParams.getOrDefault("x", "0"));
            y = Integer.parseInt(queryParams.getOrDefault("y", "0"));
            z = Integer.parseInt(queryParams.getOrDefault("z", "0"));

            includeState = Boolean.parseBoolean(queryParams.getOrDefault("includeState", "false"));

            doBlockUpdates = Boolean.parseBoolean(queryParams.getOrDefault("doBlockUpdates", "true"));
            spawnDrops = Boolean.parseBoolean(queryParams.getOrDefault("spawnDrops", "false"));
            customFlags = Integer.parseInt(queryParams.getOrDefault("customFlags", "-1"), 2);
        } catch (NumberFormatException e) {
            String message = "Could not parse query parameter: " + e.getMessage();
            throw new HandlerBase.HttpException(message, 400);
        }

        // if content type is application/json use that otherwise return text
        Headers reqestHeaders = httpExchange.getRequestHeaders();
        String contentType = getHeader(reqestHeaders, "Accept", "*/*");
        boolean returnJson = contentType.equals("application/json") || contentType.equals("text/json");

        // construct response
        String method = httpExchange.getRequestMethod().toLowerCase();
        String responseString;

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
                    returnValue = e.getMessage();
                }
                returnValues.add(returnValue);
            }
            if(!returnJson) {
                responseString = String.join("\n", returnValues);
            } else {
                JsonObject json = new JsonObject();
                JsonArray resultsArray = new JsonArray();

                for(String s : returnValues) {
                    resultsArray.add(s);
                }

                json.add("results", resultsArray);
                responseString = new Gson().toJson(json);
            }
        } else if(method.equals("get")) {
            if(includeState) {
                responseString = getBlockWithState(new BlockPos(x, y, z), returnJson);
            } else {
                responseString = getBlock(new BlockPos(x, y, z), returnJson) + "";
            }
        } else{
            throw new HandlerBase.HttpException("Method not allowed. Only PUT and GET requests are supported.", 405);
        }

        //headers
        Headers headers = httpExchange.getResponseHeaders();
        addDefaultHeaders(headers);

        if(returnJson) {
            headers.add("Content-Type", "application/json; charset=UTF-8");
        } else {
            headers.add("Content-Type", "text/plain; charset=UTF-8");
        }

        resolveRequest(httpExchange, responseString);
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

    private String getBlock(BlockPos pos, boolean returnJson) {
        ServerWorld serverWorld = mcServer.getWorld(World.OVERWORLD);

        assert serverWorld != null;

        BlockState bs = serverWorld.getBlockState(pos);

        String str;
        if(returnJson) {
            JsonObject json = new JsonObject();

            ResourceLocation rl = bs.getBlock().getRegistryName();
            assert rl != null;
            json.add("id", new JsonPrimitive(rl.toString()));

            str = new Gson().toJson(json);
        } else {
            str = Objects.requireNonNull(bs.getBlock().getRegistryName()).toString();
        }

        return str;
    }

    private String getBlockWithState(BlockPos pos, boolean returnJson) {
        ServerWorld serverWorld = mcServer.getWorld(World.OVERWORLD);

        assert serverWorld != null;
        // TODO: #118 if we ever want to do nbt
//        TileEntity tileentity = serverWorld.getTileEntity(pos);

        BlockState bs = serverWorld.getBlockState(pos);

        String str;
        if(returnJson) {
            JsonObject json = new JsonObject();

            ResourceLocation rl = bs.getBlock().getRegistryName();
            assert rl != null;
            json.add("id", new JsonPrimitive(rl.toString()));

            JsonObject state = new JsonObject();
            // put state values into the state object
            bs.getValues().entrySet().stream()
                    .map(propertyToStringPairFunction)
                    .filter(Objects::nonNull)
                    .forEach(pair -> state.add(pair.getKey(), new JsonPrimitive(pair.getValue())));

            json.add("state", state);
            str = new Gson().toJson(json);
        } else {
            str = String.valueOf(bs.getBlock().getRegistryName()) +
                    '[' +
                    bs.getValues().entrySet().stream().map(propertyToStringFunction).collect(Collectors.joining(",")) +
                    ']';
        }
        return str;
    }

    // function that converts a bunch of Property/Comparable pairs into strings that look like 'property=value'
    private static final Function<Map.Entry<Property<?>, Comparable<?>>, String> propertyToStringFunction =
            new Function<Map.Entry<Property<?>, Comparable<?>>, String>() {
                public String apply(@Nullable Map.Entry<Property<?>, Comparable<?>> element) {
                    if (element == null) {
                        return "<NULL>";
                    } else {
                        Property<?> property = element.getKey();
                        return property.getName() + "=" + this.valueToName(property, element.getValue());
                    }
                }

                private <T extends Comparable<T>> String valueToName(Property<T> property, Comparable<?> propertyValue) {
                    return property.getName((T)propertyValue);
                }
            };

    // function that converts a bunch of Property/Comparable pairs into String/String pairs
    private static final Function<Map.Entry<Property<?>, Comparable<?>>, Map.Entry<String, String>> propertyToStringPairFunction =
            new Function<Map.Entry<Property<?>, Comparable<?>>, Map.Entry<String, String>>() {
                public Map.Entry<String, String> apply(@Nullable Map.Entry<Property<?>, Comparable<?>> element) {
                    if (element == null) {
                        return null;
                    } else {
                        Property<?> property = element.getKey();
                        return new ImmutablePair<String, String>(property.getName(), this.valueToName(property, element.getValue()));
                    }
                }

                private <T extends Comparable<T>> String valueToName(Property<T> property, Comparable<?> propertyValue) {
                    return property.getName((T)propertyValue);
                }
            };
}
