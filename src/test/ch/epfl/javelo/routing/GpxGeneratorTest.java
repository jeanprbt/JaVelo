package ch.epfl.javelo.routing;

import ch.epfl.javelo.data.Graph;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

class GpxGeneratorTest {

    public static void main(String[] args) throws IOException {
        Graph graph = Graph.loadFrom("lausanne");
        CostFunction cf = new CityBikeCF(graph);
        RouteComputer rc = new RouteComputer(graph, cf);
        Route r = rc.bestRouteBetween(159049, 117669);
        ElevationProfile profile = ElevationProfileComputer.elevationProfile(Objects.requireNonNull(r), 2);

        GpxGenerator.writeGpx(Path.of("./route.gpx"), r, profile);
    }

}