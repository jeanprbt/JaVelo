# JaVelo - Cycling route planner in Switzerland

This project is a semester project for the course **CS108 - Object-Oriented Programming**, given to freshmen in Computer Science faculty at EPFL. 

It's a cycle route planner, allowing the user to select a departure point, an arrival point and several optional intermediate points to determine a route optimised for cycling. The interface features a navigable map of French-speaking Switzerland, using [OpenStreetMap](https://www.openstreetmap.org/#map=6/46.449/2.210) data. Once the route has been determined, it is displayed on this map above the vertical profile, which shows the detailed gradient over the entire route as well as a few statistics such as length and altitude. The route can be exported in GPX format to the user's computer. 

<img width="912" alt="JaVeloScreenshot" src="https://github.com/CassioManuguerra/JaVelo/assets/66010389/32efc181-ac95-4ced-98ec-b1d4e1842d38">
This project uses an in-house implementation of the [A*](https://en.wikipedia.org/wiki/A*_search_algorithm) search algorithm, which is an extension of [Dijkstra's](https://en.wikipedia.org/wiki/Dijkstra%27s_algorithm) algorithm. It relies on [JavaFX](https://openjfx.io) for GUI and [JUnit](https://junit.org/junit5/) for testing. When the user clicks somewhere outside the supported area, a simple error message is shown on the screen. 

## Run instructions
The project uses [Gradle](https://gradle.org) for build and dependency management, which makes run instructions pretty straightforward. Once you cloned the repository, simply run the following command from the `JaVelo/` directory. 

###### macOS
```shell
./gradlew run
```

###### Windows
```shell
gradlew.bat run
```


You can also run the tests or simply build the project using the same `Gradle` wrapper as above, running `./gradlew build` or `./gradlew test`.

If you wish to extend the supported area for other countries, currently used data is located into `src/resources/ch_west/` folder. You must provide the following binary files corresponding to OpenStreetMap data for the zone you wish to use: 
- `attributes.bin`
- `edges.bin`
- `elevations.bin`
- `nodes.bin`
- `profile_ids.bin`
- `sectors.bin`

This project only offers support for French language, including documentation. 
