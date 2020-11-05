package com.gdmc.httpinterfacemod;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ChunkSerializer;
import net.minecraft.world.server.ServerWorld;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GdmcHttpServer {
    private static HttpServer httpServer;
    private static MinecraftServer mcServer;
    private static final Logger LOGGER = LogManager.getLogger();

    public static void startServer(MinecraftServer mcServer) throws IOException {
        GdmcHttpServer.mcServer = mcServer;
        httpServer = HttpServer.create(new InetSocketAddress(9000), 0);
        httpServer.setExecutor(null); // creates a default executor
        createContexts();
        httpServer.start();
    }

    public static void stopServer() {
        httpServer.stop(5);
    }

    private static void createContexts() {
        httpServer.createContext("/command", new CommandHandler());
        httpServer.createContext("/chunks", new ChunkHandler());

    }

    static class CommandHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
//            //debug
//            Headers reqHeaders = httpExchange.getRequestHeaders();
//            LOGGER.info("Request headers: ");
//            for (Map.Entry<String, List<String>> entry : reqHeaders.entrySet()) {
//                LOGGER.info(entry.getKey() + " " + entry.getValue().toString());
//            }

            // execute command(s)
            InputStream bodyStream = httpExchange.getRequestBody();
            List<String> commands = new BufferedReader(new InputStreamReader(bodyStream))
                    .lines().filter(a -> a.length() > 0).collect(Collectors.toList());

            List<String> outputs = new ArrayList<>();
            for (String command: commands) {
                String result;
                try {
                    result = "" + mcServer.getCommandManager().getDispatcher().execute(command, mcServer.getCommandSource());
                } catch (CommandSyntaxException e) {
                    result = e.getMessage(); // TODO could return an error instead
                    LOGGER.error(e.getMessage());
                }
                outputs.add(result + "");
            }

            String responseString = String.join("\n", outputs);

            //headers
            Headers headers = httpExchange.getResponseHeaders();
            headers.add("Content-Type", "text/raw; charset=UTF-8");

            // body
            byte[] responseBytes = responseString.getBytes(StandardCharsets.UTF_8);
            httpExchange.sendResponseHeaders(200, responseBytes.length);
            OutputStream outputStream = httpExchange.getResponseBody();
            outputStream.write(responseBytes);
            outputStream.close();

            // debug info

//            LOGGER.info("Response headers: ");
//            for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
//                LOGGER.info(entry.getKey() + " " + entry.getValue().toString());
//            }

//            String response = block.getDefaultState().
        }
    }
    static class ChunkHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            String method = httpExchange.getRequestMethod().toLowerCase();

            String responseString = "";
            byte[] responseBytes = new byte[0];

            // look at incoming request
            Map<String, String> queryParams = parseQueryString(httpExchange.getRequestURI().getRawQuery());

            int statusCode = 200;

            int chunkX = 0;
            int chunkZ = 0;
            int chunkDX = 1;
            int chunkDZ = 1;
            try {
                chunkX = Integer.parseInt(queryParams.get("x"));
                chunkZ = Integer.parseInt(queryParams.get("z"));
                if(queryParams.containsKey("dx"))
                    chunkDX = Integer.parseInt(queryParams.get("dx"));
                if(queryParams.containsKey("dz"))
                    chunkDZ = Integer.parseInt(queryParams.get("dz"));
            } catch (NumberFormatException e) {
                responseString = "Could not parse query parameter: " + e.getMessage();
                statusCode = 400;
            }

            // with this header we return pure NBT binary
            String contentType = httpExchange.getRequestHeaders().get("Accept").get(0);
            boolean RETURN_TEXT = !contentType.equals("application/octet-stream");

            // construct response
            if(statusCode == 200) {
                if(method.equals("get")) {
                    ServerWorld world = mcServer.getWorld(World.field_234918_g_);

                    ListNBT chunkList = new ListNBT();
                    for(int z = chunkZ; z < chunkZ + chunkDZ; z++)
                    for(int x = chunkX; x < chunkX + chunkDX; x++) {
                        Chunk chunk = world.getChunk(x, z); // TODO handle when chunk doesn't exist

                        CompoundNBT chunkNBT = ChunkSerializer.write(world, chunk);
                        chunkList.add(chunkNBT);
                    }

                    CompoundNBT bodyNBT = new CompoundNBT();
                    bodyNBT.put("Chunks", chunkList);
                    bodyNBT.putInt("ChunkX", chunkX);
                    bodyNBT.putInt("ChunkZ", chunkZ);
                    bodyNBT.putInt("ChunkDX", chunkDX);
                    bodyNBT.putInt("ChunkDZ", chunkDZ);

                    if(RETURN_TEXT) {
                        responseString = bodyNBT.toString();
                    } else {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        DataOutputStream dos = new DataOutputStream(baos);

                        CompoundNBT containterNBT = new CompoundNBT();
                        containterNBT.put("file", bodyNBT);
                        containterNBT.write(dos);
                        dos.flush();
                        responseBytes = baos.toByteArray();
                    }
                } else {
                    statusCode = 405;
                    responseString = "Method not allowed. Only GET requests are supported.";
                }
            }

            //headers
            Headers headers = httpExchange.getResponseHeaders();
            if(RETURN_TEXT) {
                headers.add("Content-Type", "text/raw; charset=UTF-8");
            } else {
                headers.add("Content-Type", "application/octet-stream");
            }

            // body
            if(RETURN_TEXT) {
                responseBytes = responseString.getBytes(StandardCharsets.UTF_8);
            }
            httpExchange.sendResponseHeaders(statusCode, responseBytes.length);
            OutputStream outputStream = httpExchange.getResponseBody();
            outputStream.write(responseBytes);
            outputStream.close();
        }
    }

    public static Map<String, String> parseQueryString(String qs) {
        Map<String, String> result = new HashMap<>();
        if (qs == null)
            return result;

        int last = 0, next, l = qs.length();
        while (last < l) {
            next = qs.indexOf('&', last);
            if (next == -1)
                next = l;

            if (next > last) {
                int eqPos = qs.indexOf('=', last);
                try {
                    if (eqPos < 0 || eqPos > next)
                        result.put(URLDecoder.decode(qs.substring(last, next), "utf-8"), "");
                    else
                        result.put(URLDecoder.decode(qs.substring(last, eqPos), "utf-8"), URLDecoder.decode(qs.substring(eqPos + 1, next), "utf-8"));
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e); // will never happen, utf-8 support is mandatory for java
                }
            }
            last = next + 1;
        }
        return result;
    }
}



//                    //old chunk palette stuff
//                    ArrayList<String> sectionList = new ArrayList<>();
//                    ListNBT sectionListNBT = new ListNBT();
//                    for(ChunkSection chunkSection : chunk.getSections()) {
//                        if(ChunkSection.isEmpty(chunkSection)) {
//                            sectionList.add("");
//                            continue;
//                        }
//                        PalettedContainer<BlockState> palettedContainer = chunkSection.getData();
//                        CompoundNBT chunkPaletteCompound = new CompoundNBT();
//                        palettedContainer.writeChunkPalette(chunkPaletteCompound, "palette", "data");
//                        sectionList.add(chunkPaletteCompound.toString());
//                    }
