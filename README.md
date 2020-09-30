# Template mod for GDMC - 1.15.2

This repo is just the Forge MDK with a different example mod.
This template mod register, parse and execute the command "/buildsettlement x1 y1 z1 x2 y2 z2", printing a "Hello World!" plus coordinates.
It also provides some basic functions considered useful to implement a village generator.

---

## Setup the dev environment

These instructions are adapted from the Forge installation guide : [Forge Setup](https://mcforge.readthedocs.io/en/1.14.x/gettingstarted/#getting-started-with-forge)

#### Get the sources

On GitHub, click on the green "Use Template" button to create a new repository, and clone it.

Otherwise, you can also directly download the code ("Code" button, then download zip), and extract it to the folder you choose.

#### Choose your IDE:

- Forge explicitly supports developing with Eclipse or IntelliJ environments, but any environment, from Netbeans to vi/emacs, can be made to work.

- For both Intellij IDEA and Eclipse their Gradle integration will handle the rest of the initial workspace setup, this includes downloading packages from Mojang, MinecraftForge, and a few other software sharing sites.
You just have to import the build.gradle in the IDE, and the project will be imported.

- For most, if not all, changes to the build.gradle file to take effect Gradle will need to be invoked to re-evaluate the project, this can be done through Refresh buttons in the Gradle panels of both the previously mentioned IDEs.

I personnaly would go for IntelliJ.

#### Generating IDE Launch/Run Configurations:

- For Eclipse, run the genEclipseRuns gradle task (gradlew genEclipseRuns). This will generate the Launch Configurations and download any required assets for the game to run. After this has finished refresh your project.

- For IntelliJ, run the genIntellijRuns gradle task (gradlew genIntellijRuns). This will generate the Run Configurations and download any required assets for the game to run. After this has finished, reload the project from disk and click on "Add configuration". Under the "Application" tab, you have a runClient configuration. Select it, and edit your Configurations to fix the “module not specified” error by changing selecting your “main” module. You can now run Minecraft, with the mod loaded in, and test the command "buildSettlement x1 y1 z1 x2 y2 z2".

---


## Start creating your mod

#### Edit the mod packaging

Do not hesitate to refactor all the packaging structure, made here for the example, and to rename the mod's main class ```GdmcExample```

> **Important :** Please change the MOD_ID variable, that should match the string in the ```@Mod``` annotation above the ```GdmcExample``` class declaration, and also the modid in mods.toml ! Otherwise, your mod might collide with other.

#### Edit the mods.toml file

This file contains all the metadata of your mod, like its description, its name, its version, etc... Feel free to modify all of this.

#### Start coding

Once you are ready to code, just code, everything is made for you!
Delete the 'TODO' in the```buildSettlement()``` method, in the class ```BuildSettlementCommand``` and start developping your algorithm.
Don't hesitate to take a look at our [wiki](https://github.com/Lasbleic/gdmc_java_mod/wiki/)!



### Happy GDMC Competition!



