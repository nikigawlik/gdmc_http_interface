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
import java.util.concurrent.CompletableFuture;

public class ChunkHandler extends HandlerBase {
    public ChunkHandler(MinecraftServer mcServer) {
        super(mcServer);
    }

    @Override
    public void internalHandle(HttpExchange httpExchange) throws IOException {

        // query parameters
        Map<String, String> queryParams = parseQueryString(httpExchange.getRequestURI().getRawQuery());

        int chunkX;
        int chunkZ;
        int chunkDX;
        int chunkDZ;
        try {
            chunkX = Integer.parseInt(queryParams.getOrDefault("x", "0"));
            chunkZ = Integer.parseInt(queryParams.getOrDefault("z", "0"));
            chunkDX = Integer.parseInt(queryParams.getOrDefault("dx", "1"));
            chunkDZ = Integer.parseInt(queryParams.getOrDefault("dz", "1"));
        } catch (NumberFormatException e) {
            String message = "Could not parse query parameter: " + e.getMessage();
            throw new HandlerBase.HttpException(message, 400);
        }

        String method = httpExchange.getRequestMethod().toLowerCase();
        if(!method.equals("get")) {
            throw new HandlerBase.HttpException("Method not allowed. Only GET requests are supported.", 405);
        }

        // with this header we return pure NBT binary
        // if content type is application/json use that otherwise return text
        Headers requestHeaders = httpExchange.getRequestHeaders();
        String contentType = getHeader(requestHeaders, "Accept", "*/*");
        boolean RETURN_TEXT = !contentType.equals("application/octet-stream");

        // construct response
        ServerWorld world = mcServer.getWorld(World.OVERWORLD);
        assert world != null;

//        CompletableFuture<ListNBT> cfs = CompletableFuture.supplyAsync(() -> {
        ListNBT chunkList = new ListNBT();
        for(int z = chunkZ; z < chunkZ + chunkDZ; z++)
            for(int x = chunkX; x < chunkX + chunkDX; x++) {
                Chunk chunk = world.getChunk(x, z);

                CompoundNBT chunkNBT = ChunkSerializer.write(world, chunk);
                chunkList.add(chunkNBT);
            }
//            return returnList;
//        }, mcServer);

        // block this thread until the above code has run on the main thread
//        ListNBT chunkList = cfs.join();

        CompoundNBT bodyNBT = new CompoundNBT();
        bodyNBT.put("Chunks", chunkList);
        bodyNBT.putInt("ChunkX", chunkX);
        bodyNBT.putInt("ChunkZ", chunkZ);
        bodyNBT.putInt("ChunkDX", chunkDX);
        bodyNBT.putInt("ChunkDZ", chunkDZ);

        String responseString = "";
        byte[] responseBytes = new byte[0];

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

        //headers
        Headers headers = httpExchange.getResponseHeaders();
        if(RETURN_TEXT) {
            headers.add("Content-Type", "text/plain; charset=UTF-8");
        } else {
            headers.add("Content-Type", "application/octet-stream");
        }

        // body
        if(RETURN_TEXT) {
            responseBytes = responseString.getBytes(StandardCharsets.UTF_8);
        }
        httpExchange.sendResponseHeaders(200, responseBytes.length);
        OutputStream outputStream = httpExchange.getResponseBody();
        outputStream.write(responseBytes);
        outputStream.close();
    }
}