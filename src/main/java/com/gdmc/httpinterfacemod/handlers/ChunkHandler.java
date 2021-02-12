package com.gdmc.httpinterfacemod.handlers;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ChunkSerializer;
import net.minecraft.world.server.ServerWorld;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class ChunkHandler extends HandlerBase {
    public ChunkHandler(MinecraftServer mcServer) {
        super(mcServer);
    }

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
                ServerWorld world = mcServer.getWorld(World.OVERWORLD);
                assert world != null;

                ListNBT chunkList = new ListNBT();
                for(int z = chunkZ; z < chunkZ + chunkDZ; z++)
                    for(int x = chunkX; x < chunkX + chunkDX; x++) {
                        Chunk chunk = world.getChunk(x, z);

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