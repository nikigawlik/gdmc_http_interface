package com.gdmc.httpinterfacemod.handlers;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CommandHandler extends HandlerBase {
    private static final Logger LOGGER = LogManager.getLogger();

    public CommandHandler(MinecraftServer mcServer) {
        super(mcServer);
    }

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