package com.gdmc.httpinterfacemod.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import net.minecraft.server.MinecraftServer;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class BuildAreaHandler extends HandlerBase {
    public static class BuildArea {
        private final int xFrom;
        private final int yFrom;
        private final int zFrom;
        private final int xTo;
        private final int yTo;
        private final int zTo;

        public int getxFrom() { return xFrom; }
        public int getyFrom() { return yFrom; }
        public int getzFrom() { return zFrom; }
        public int getxTo() { return xTo; }
        public int getyTo() { return yTo; }
        public int getzTo() { return zTo; }

        public BuildArea(int xFrom, int yFrom, int zFrom, int xTo, int yTo, int zTo) {
            this.xFrom = xFrom;
            this.yFrom = yFrom;
            this.zFrom = zFrom;
            this.xTo = xTo;
            this.yTo = yTo;
            this.zTo = zTo;
        }

        @Override
        public String toString() {
            return "BuildArea{" +
                    "areaX=" + xFrom +
                    ", areaZ=" + zFrom +
                    ", areaSizeX=" + xTo +
                    ", areaSizeZ=" + zTo +
                    '}';
        }
    }

//    private static final Logger LOGGER = LogManager.getLogger();
    private static BuildArea buildArea;

    public static void setBuildArea(int xFrom, int yFrom, int zFrom, int xTo, int yTo, int zTo) {
        buildArea = new BuildArea(xFrom, yFrom, zFrom, xTo, yTo, zTo);
    }

    public BuildAreaHandler(MinecraftServer mcServer) {
        super(mcServer);
        buildArea = null;
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
//            //debug
//            Headers reqHeaders = httpExchange.getRequestHeaders();
//            LOGGER.info("Request headers: ");
//            for (Map.Entry<String, List<String>> entry : reqHeaders.entrySet()) {
//                LOGGER.info(entry.getKey() + " " + entry.getValue().toString());
//            }

        String method = httpExchange.getRequestMethod().toLowerCase();

        //headers
        Headers headers = httpExchange.getResponseHeaders();


        // body
        String responseString;
        int returnCode;
        if(!method.equals("get")) {
            returnCode = 405;
            headers.add("Content-Type", "text/raw; charset=UTF-8");
            responseString = "Please use GET method to request the build area.";
        } else if(buildArea == null) {
            returnCode = 404;
            headers.add("Content-Type", "text/raw; charset=UTF-8");
            responseString = "No build area is specified. Use the buildarea command inside Minecraft to set a build area.";
        } else {
            returnCode = 200;
            headers.add("Content-Type", "application/json; charset=UTF-8");
            responseString = new Gson().toJson(buildArea);
        }
        byte[] responseBytes = responseString.getBytes(StandardCharsets.UTF_8);
        httpExchange.sendResponseHeaders(returnCode, responseBytes.length);
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