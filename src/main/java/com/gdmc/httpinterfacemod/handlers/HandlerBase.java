package com.gdmc.httpinterfacemod.handlers;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import net.minecraft.command.CommandSource;
import net.minecraft.command.ICommandSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public abstract class HandlerBase implements HttpHandler {
    MinecraftServer mcServer;
    public HandlerBase(MinecraftServer mcServer) {
        this.mcServer = mcServer;
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        try {
            internalHandle(httpExchange);
        } catch (Exception e) {
            String responseString = String.format("Internal server error: %s", e.toString());
            byte[] responseBytes = responseString.getBytes(StandardCharsets.UTF_8);

            httpExchange.sendResponseHeaders(500, responseBytes.length);
            OutputStream outputStream = httpExchange.getResponseBody();
            outputStream.write(responseBytes);
            outputStream.close();
        }
    }

    protected abstract void internalHandle(HttpExchange httpExchange) throws IOException;

    protected static void addDefaultHeaders(Headers headers) {
        headers.add("Access-Control-Allow-Origin", "*");
        headers.add("Content-Disposition", "inline");
    }

    protected static Map<String, String> parseQueryString(String qs) {
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

    protected static CommandSource createCommandSource(String name, MinecraftServer mcServer) {
        ICommandSource iCmdSrc = new ICommandSource() {
            @Override public void sendMessage(ITextComponent component, UUID senderUUID) { }
            @Override public boolean shouldReceiveFeedback() { return false; }
            @Override public boolean shouldReceiveErrors() { return false; }
            @Override public boolean allowLogging() { return false; }
        };

        return new CommandSource(
                iCmdSrc,
                new Vector3d(0, 0, 0),
                new Vector2f(0, 0),
                Objects.requireNonNull(mcServer.getWorld(World.OVERWORLD)),
                4,
                name,
                new StringTextComponent(name),
                mcServer,
                null
        );
    }
}
