# Minecraft HTTP Interface Mod (Minecraft 1.16.3)

This repo is based on the [GDMC example mod](https://github.com/Lasbleic/gdmc_java_mod) which is based on the Forge MDK.

## What it's all about

(Disclaimer: This mod is in early development)

This mod opens an HTTP interface so that other programs (on the same machine) can read and modify the world. It is meant as a tool to be used for the [Generative Design in Minecraft Competition](http://gendesignmc.engineering.nyu.edu/), but is not approved as a submission method, so right now it is only useful for prototyping ideas!

When you open a Minecraft world, this mod opens a HTTP Server on localhost:9000. This HTTP Interface currently supports two endpoints:

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

### Get chunk data

`GET http://localhost:9000/chunks?x=<int>&z=<int>&dx=<int>&dz=<int>`

for example

`GET http://localhost:9000/chunks?x=0&z=0&dx=2&dz=2`

This returns the chunks as an NBT data structure. [The NBT format](https://minecraft.gamepedia.com/NBT_format) is the save format Minecraft uses for most things. There are open source NBT parsers available for different languages including Python and C#.

If you set the 'Accept' header of your request to "application/octet-stream" you will get raw binary data. This is what you probably want if you are using an NBT parsing library.

If the Accept header is anything else, you will get a human readable representation, which looks like this:

```
{Chunks:[{Level:{Status:"full",zPos:0,LastUpdate:6560L,Biomes:[I;3,3,3,3,7,7 ...
```

This human readable representation of NBT is defined by Minecraft and used in different places, for example when using NBT data in commands. 

### Set a block

`POST http://localhost:9000/setblock?x=<int>&y=<int>&z=<int>`

request body: 

`<block>`

for example

`POST http://localhost:9000/setblock?x=-354&y=67&z=1023`

request body: 
`minecraft:stone`

Sets a block in the Minecraft world, similar to the /setblock command. 

The x, y, z parameters specify the block location. 
The request body specifies the block using Minecraft's <block> argument syntax, a more complicated example would be:

`minecraft:furnace[facing=north]{BurnTime:200}`

More info on the block state syntax can be found [on the Minecraft wiki](https://minecraft.gamepedia.com/Commands#.3Cblock.3E)


## Installing this mod with the Forge Mod Launcher

You need to own a copy of Minecraft and have it installed on your machine. 

Get the [Forge Mod Launcher](https://files.minecraftforge.net/) (1.16.4-35.1.15 - Installer) and install it.

Open your Minecraft Launcher, the Forge Installation should have appeared there.

Open the Forge Installation and click the "Mods" button and then the "Open mods folder" button (You can skip this step by just navigating to %APPDATA%/.minecraft/mods).

Download the jar file from [here](https://github.com/nilsgawlik/gdmc_http_interface/releases/tag/v0.2.0alpha) and place it in the mod folder.

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

I personnaly would go for IntelliJ.

#### Generating IDE Launch/Run Configurations:

- For Eclipse, run the genEclipseRuns gradle task (gradlew genEclipseRuns). This will generate the Launch Configurations and download any required assets for the game to run. After this has finished refresh your project.

- For IntelliJ, run the genIntellijRuns gradle task (gradlew genIntellijRuns). This will generate the Run Configurations and download any required assets for the game to run. After this has finished, reload the project from disk and click on "Add configuration". Under the "Application" tab, you have a runClient configuration. Select it, and edit your Configurations to fix the “module not specified” error by changing selecting your “main” module. You can now run Minecraft, with the mod loaded in. Make sure to open a Minecraft world, before testing the mod.

I recommend using Postman or a similar application to test out the http interface. An Python example of how to use the interface can be found [here](https://github.com/nilsgawlik/gdmc_http_client_python)
