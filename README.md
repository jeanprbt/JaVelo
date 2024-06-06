# JaVelo - Cycling route planner in Switzerland

This project is a semester project for the course **CS108 - Object-Oriented Programming**, given to freshmen in Computer Science faculty at EPFL.

It's a cycle route planner, allowing the user to select a departure point, an arrival point and several optional intermediate points to determine a route optimised for cycling. The interface features a navigable map of French-speaking Switzerland, using [OpenStreetMap](https://www.openstreetmap.org/#map=6/46.449/2.210) data. Once the route has been determined, it is displayed on this map above the vertical profile, which shows the detailed gradient over the entire route as well as a few statistics such as length and altitude. The route can be exported in GPX format.

<img width="912" alt="JaVeloScreenshot" src="https://github.com/CassioManuguerra/JaVelo/assets/66010389/32efc181-ac95-4ced-98ec-b1d4e1842d38">

This project uses an in-house implementation of the [A*](https://en.wikipedia.org/wiki/A*_search_algorithm) search algorithm, which is an extension of [Dijkstra's](https://en.wikipedia.org/wiki/Dijkstra%27s_algorithm) algorithm. It relies on [JavaFX](https://openjfx.io) for GUI and [JUnit](https://junit.org/junit5/) for testing.

Map is displayed using tiles fetched from OpenStreetMap's [API](https://wiki.openstreetmap.org/wiki/Raster_tile_providers) at runtime. In order to streamline the procedure, they are cached in folder `.javelo-cache/` in user's home directory. When the user clicks somewhere outside the supported area, a simple error message is shown on the screen.

## Run instructions

You can check in the `Releases` tab for the latest version of the project, which includes a standalone executable for Apple Silicon Macs. 

Otherwise, the project uses [Gradle](https://gradle.org) for build and dependency management, which makes run instructions pretty straightforward. Once you cloned the repository, simply run the following command from the `JaVelo/` directory.

###### macOS
```shell
./gradlew run
```

###### Windows
```shell
gradlew.bat run
```


You can also run the tests or simply build the project using the same `Gradle` wrapper as above, running `./gradlew build` or `./gradlew test`.

If you wish to extend the supported area for other countries, currently used data is located into `src/main/resources/ch_west/` folder. You must provide the following binary files corresponding to OpenStreetMap data for the zone you wish to use:
- `attributes.bin`
- `edges.bin`
- `elevations.bin`
- `nodes.bin`
- `profile_ids.bin`
- `sectors.bin`

This project only offers support for French language, including documentation.

## Packaging
This project can be packaged to the OS of your choice, either in a fat JAR or in a `*.dmg` (`macOS`), `*.rpm`, `*.deb` (`Linux`), `*.exe` (`Windows`) executable.

The packaging process uses instructions from this [repository](https://github.com/heshanthenura/JavaPackageDemo/blob/master/README.md).

First, create a standalone fat JAR which ships all required dependencies using the custom Gradle task of `build.gradle`. It can be found under `/build/libs` after running the following command.
```sh
./gradlew customFatJar
```

Then, use this JAR and `jpackage` utility to create an executable, which will appear in the project's working directory.
##### macOS
```shell
jpackage --input build/libs --name JaVelo --main-jar JaVeloJAR-1.0-SNAPSHOT.jar --main-class ch.epfl.javelo.Launcher --type dmg --icon src/main/resources/logo.icns
```
##### Linux
It is necessary to install `fakeroot`.
```
apt-get install fakeroot -y
```
###### Debian
```shell
jpackage --input build/libs --name JaVelo --main-jar JaVeloJAR-1.0-SNAPSHOT.jar --main-class ch.epfl.javelo.Launcher --type deb --icon src/main/resources/logo.icns 
```
###### RedHat
```shell
sudo yum install rpm-build
jpackage --input build/libs --name JaVelo --main-jar JaVeloJAR-1.0-SNAPSHOT.jar --main-class ch.epfl.javelo.Launcher --type rpm --icon src/main/resources/logo.icns 
```
##### Windows
It is necessary to install the [WiX toolset](https://wixtoolset.org/docs/wix3/).
```shell
jpackage --input build/libs --name JaVelo --main-jar JaVeloJAR-1.0-SNAPSHOT.jar --main-class ch.epfl.javelo.Launcher --type msi --win-dir-chooser --icon src/main/resources/logo.icns 
```
