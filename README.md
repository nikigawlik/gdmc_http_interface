# Minecraft HTTP Interface Mod (Minecraft 1.16.4)

This repo is based on the [GDMC example mod](https://github.com/Lasbleic/gdmc_java_mod) which is based on the Forge MDK.

## What it's all about

(Disclaimer: This mod is in early development)

This mod opens an HTTP interface so that other programs (on the same machine) can read and modify the world. It is meant as a tool to be used for the [Generative Design in Minecraft Competition](http://gendesignmc.engineering.nyu.edu/). Install instructions can be found [further down](#installing-this-mod-with-the-forge-mod-launcher).

When you open a Minecraft world, this mod opens a HTTP Server on localhost:9000. I recommend using Postman or a similar application to test out the http interface. A Python example of how to use the interface can be found [here](https://github.com/nilsgawlik/gdmc_http_client_python). This HTTP Interface currently implements these endpoints:

## Features / HTTP Endpoints

### Send commands to the server

`POST http://localhost:9000/command`

request body: 
```
<command>
<command>
...
```

The request body contains one or multiple commands on separate lines without the slashes, for example:

```
say start
tp @p 0 70 0
setblock 0 69 0 stone
fill -8 68 -8 8 68 8 oak_planks replace
say end
```

These commands are then executed line by line. The response body of the request contains the return values for each command in separate lines. A return value can either be an integer or an error message. For example the request above might return:

```
1
1
1
289
1
```

And on a subsequent call, two of the commands will fail, so the return text will be:

```
1
1
Could not set the block
No blocks were filled
1
```

For commands and their return values consult the [Minecraft commands documentation](https://minecraft.gamepedia.com/Commands#List_and_summary_of_commands).

### Read a block

`GET http://localhost:9000/blocks?x=<int>&y=<int>&z=<int>`

This returns the namespaced id of the block at position x, y, z. 

for example

`GET http://localhost:9000/blocks?x=-354&y=48&z=1023`

could return:
 
`minecraft:stone`

### Set a block

`PUT http://localhost:9000/blocks?x=<int>&y=<int>&z=<int>`

request body: 

`<block>`

for example

`PUT http://localhost:9000/blocks?x=-354&y=67&z=1023`

request body: 
`minecraft:stone`

Sets a block in the Minecraft world, similar to the /setblock command. 

The x, y, z parameters specify the block location. 
The request body specifies the block using Minecraft's <block> argument syntax, it also supports block states:

`minecraft:furnace[facing=north]`

Specifying additional nbt data (like inventory contents) is not supported at the moment. For now, you can use the /command endpoint with Minecraft's /setblock or /data command instead.

More info on the block state syntax can be found [on the Minecraft wiki](https://minecraft.gamepedia.com/Commands#.3Cblock.3E)

#### Setting blocks in bulk

`PUT http://localhost:9000/blocks?x=<int>&y=<int>&z=<int>`

request body: 

```
<x> <y> <z> <block>
<x> <y> <z> <block>
<x> <y> <z> <block>
...
```

for example

`PUT http://localhost:9000/blocks?x=-354&y=67&z=1023`

request body: 
```
~0 ~0 ~1 minecraft:stone
~0 ~0 ~2 minecraft:stone
~0 ~0 ~3 minecraft:stone
~0 ~0 ~3 minecraft:stone
~1 ~0 ~3 minecraft:stone
~2 ~0 ~3 minecraft:stone
```

The /blocks endpoint can also contain multiple lines, with each line corresponding to setting one block. The x, y, and z coordinates can be specified in tilde notation (`~1 ~2 ~3`). In that case they are relative to the position specified in the x, y and z query parameters.

It is completely feasible to send hundreds of blocks at once with this endpoint. Minecraft will then place them as quickly as possible and return the request once it has placed all the blocks.

### Get chunk data

`GET http://localhost:9000/chunks?x=<int>&z=<int>&dx=<int>&dz=<int>`

for example

`GET http://localhost:9000/chunks?x=0&z=0&dx=2&dz=2`

This returns the chunks as an NBT data structure. [The NBT format](https://minecraft.gamepedia.com/NBT_format) is the save format Minecraft uses for most things. There are open source NBT parsers available for different languages including Python and C#.

The query parameters x and z specify the position of the chuck in 'chunk coordinates'. To get a chunk coordinate from a normal world coordinate, simply divide by 16 and round down to the nearest integer.

The query parameters dx and dz specify how many chunks to get. So dx=3 and dz=4 would get you a 3 by 4 area of chunks, so 12 chunks in total.

The chunks within the returned nbt structure are arranged in z, x order, so the chunk at position (x, z) is in the array at position (x + z * dx).

If you set the 'Accept' header of your request to "application/octet-stream" you will get raw binary data. This is what you probably want if you are using an NBT parsing library.

If the Accept header is anything else, you will get a human readable representation, which looks like this:

```
{Chunks:[{Level:{Status:"full",zPos:0,LastUpdate:6560L,Biomes:[I;3,3,3,3,7,7 ...
```

This human readable representation of NBT is defined by Minecraft and used in different places, for example when using NBT data in commands. 

The layout of the chunk save data is not completely trivial to process. An example on how to read this data and extract block and heightmap information you can take a look at [this python script](https://github.com/nilsgawlik/gdmc_http_client_python/blob/master/worldLoader.py).

## Installing this mod with the Forge Mod Launcher

You need to own a copy of Minecraft and have it installed on your machine. 

Get the [Forge Mod Launcher](https://files.minecraftforge.net/) (Download Recommended -> Installer) and install it (client install, but server should work, too). The latest version I tested is 1.16.4-35.1.15, but newer versions should work just fine.

Open your Minecraft Launcher, the Forge Installation should have appeared there.

Open your mod folder. To do this open the Forge Installation you just installed and click the "Mods" button and then the "Open mods folder" button (You can skip this step by just navigating to %APPDATA%/.minecraft/mods).

Download the jar file from [here](https://github.com/nilsgawlik/gdmc_http_interface/releases/tag/v0.3.0alpha) and place it in the mod folder.

Restart Minecraft and launch the Forge Installation again. The mod should now appear in the mod list under "Mods".

When you open a world the HTTP Server will be started automatically

## Running this mod from source

These instructions are adapted from the [Forge installation guide](https://mcforge.readthedocs.io/en/1.14.x/gettingstarted/#getting-started-with-forge)

#### Get the sources

Clone or fork this repository.

#### Choose your IDE:

- Forge explicitly supports developing with Eclipse or IntelliJ environments, but any environment, from Netbeans to vi/emacs, can be made to work.

- For both Intellij IDEA and Eclipse their Gradle integration will handle the rest of the initial workspace setup, this includes downloading packages from Mojang, MinecraftForge, and a few other software sharing sites.
You just have to import the build.gradle in the IDE, and the project will be imported.

- For most, if not all, changes to the build.gradle file to take effect Gradle will need to be invoked to re-evaluate the project, this can be done through Refresh buttons in the Gradle panels of both the previously mentioned IDEs.

I personally would go for IntelliJ.

#### Generating IDE Launch/Run Configurations:

- For Eclipse, run the genEclipseRuns gradle task (gradlew genEclipseRuns). This will generate the Launch Configurations and download any required assets for the game to run. After this has finished refresh your project.

- For IntelliJ, run the genIntellijRuns gradle task (gradlew genIntellijRuns). This will generate the Run Configurations and download any required assets for the game to run. After this has finished, reload the project from disk and click on "Add configuration". Under the "Application" tab, you have a runClient configuration. Select it, and edit your Configurations to fix the “module not specified” error by changing selecting your “main” module. You can now run Minecraft, with the mod loaded in. Make sure to open a Minecraft world before testing the mod.
