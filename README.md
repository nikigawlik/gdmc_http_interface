# Minecraft HTTP Interface Mod (Minecraft 1.16.3)

This repo is based on the [GDMC example mod](https://github.com/Lasbleic/gdmc_java_mod) which is based on the Forge MDK.

When you open a Minecraft world, this mod opens a HTTP Server on localhost:9000. This HTTP Interface currently supports two endpoints:

## Features / HTTP Endpoints

### Send commands to the server

`http://localhost:9000/command`

The request body contains one or multiple commands on separate lines without the slashes, for example:

```
say start

tp @p 0 70 0
setblock 0 69 0 stone

fill -8 68 -8 8 68 8 oak_planks replace

say end
```

[Minecraft commands documentation](https://minecraft.gamepedia.com/Commands#List_and_summary_of_commands)

### Get chunk data

`http://localhost:9000/chunks?x=<int>&z=<int>&dx=<int>&dz=<int>`

for example

`http://localhost:9000/chunks?x=0&z=0&dx=2&dz=2`

This returns the chunks as an NBT data structure. [The NBT format](https://minecraft.gamepedia.com/NBT_format) is the save format Minecraft uses for most things. There are open source NBT parsers available for different languages including Python and C#.

If you set the 'Accept' header of your request to "application/octet-stream" you will get raw binary data. This is what you probably want.

If the Accept header is anything else, you will get a json-like human readable representation, which looks like this:

```
{Chunks:[{Level:{Status:"full",zPos:0,LastUpdate:6560L,Biomes:[I;3,3,3,3,7,7 ...
```

This human readable representation of NBT is defined by Minecraft and used in different places, for example when using NBT data in commands. 

## Installing this mod

!TODO!

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

I recommend using Postman or a similar application to test out the http interface. An Python example of how to use the interface can be found here: !TODO!
