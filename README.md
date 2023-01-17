# This project has moved somewhere else!

Development has kindly been picked up by Niels-NTG and the project now lives [over here](https://github.com/Niels-NTG/gdmc_http_interface).

## Minecraft HTTP Interface Mod (Minecraft 1.16.5)

This repo is based on the [GDMC example mod](https://github.com/Lasbleic/gdmc_java_mod) which is based on the Forge MDK.

## What it's all about

This mod opens an HTTP interface so that other programs (on the same machine) can read and modify the world. It is meant as a tool to be used for the [Generative Design in Minecraft Competition](http://gendesignmc.engineering.nyu.edu/).

When you open a Minecraft world, this mod opens an HTTP Server on localhost:9000. I recommend using Postman or a similar application to test out the http interface. A Python example of how to use the interface can be found [here](https://github.com/nilsgawlik/gdmc_http_client_python).

## Features / HTTP Endpoints

The current endpoints of the interface are 

```
GET,PUT /blocks     Modify blocks in the world
POST    /command    Run Minecraft commands
GET     /chunks     Get raw chunk nbt data
GET     /buildarea  Get the build area defined by the /setbuildarea chat command
```

A detailed documentation of the endpoints can be found [over here](https://github.com/nilsgawlik/gdmc_http_interface/wiki/Interface-Endpoints).

## Installation

Install instructions are [over here](https://github.com/nilsgawlik/gdmc_http_interface/wiki/Installation).
You need to own a copy of Minecraft to use the http interface!
