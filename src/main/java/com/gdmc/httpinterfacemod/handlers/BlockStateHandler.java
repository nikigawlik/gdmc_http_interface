package com.gdmc.httpinterfacemod.handlers;

import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.realmsclient.util.JsonUtils;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import net.minecraft.block.BlockState;
import net.minecraft.server.MinecraftServer;
import net.minecraft.state.Property;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import org.apache.commons.lang3.tuple.ImmutablePair;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class BlockStateHandler extends HandlerBase {
    public BlockStateHandler(MinecraftServer mcServer) {
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

        try {
            x = Integer.parseInt(queryParams.getOrDefault("x", "0"));
            y = Integer.parseInt(queryParams.getOrDefault("y", "0"));
            z = Integer.parseInt(queryParams.getOrDefault("z", "0"));
        } catch (NumberFormatException e) {
            responseString = "Could not parse query parameter: " + e.getMessage();
            statusCode = 400;
        }

        // if content type is application/json use that otherwise return text
        String contentType = httpExchange.getRequestHeaders().get("Accept").get(0);
        boolean RETURN_JSON = contentType.equals("application/json");

        // construct response
        if(statusCode == 200) {
            if(method.equals("get")) {
                responseString = getBlock(new BlockPos(x, y, z), RETURN_JSON);

            } else {
                statusCode = 405;
                responseString = "Method not allowed. Only GET requests are supported.";
            }
        }

        //headers
        Headers headers = httpExchange.getResponseHeaders();

        addDefaultHeaders(headers);
        if(RETURN_JSON) {
            headers.add("Content-Type", "application/json");
        } else {
            headers.add("Content-Type", "text/raw; charset=UTF-8");
        }

        // body

        byte[] responseBytes = responseString.getBytes(StandardCharsets.UTF_8);

        httpExchange.sendResponseHeaders(statusCode, responseBytes.length);
        OutputStream outputStream = httpExchange.getResponseBody();
        outputStream.write(responseBytes);
        outputStream.close();
    }

    private String getBlock(BlockPos pos, boolean returnJson) {
        ServerWorld serverWorld = mcServer.getWorld(World.OVERWORLD);

        assert serverWorld != null;
        // TODO: #118 if we ever want to do nbt
//        TileEntity tileentity = serverWorld.getTileEntity(pos);

        BlockState bs = serverWorld.getBlockState(pos);

        String str;
        if(returnJson) {
            JsonObject json = new JsonObject();
            json.add("block", new JsonPrimitive(bs.getBlock().getRegistryName().toString()));

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


/*


 */