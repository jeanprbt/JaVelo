package ch.epfl.javelo.routing;

import ch.epfl.javelo.data.Graph;
import ch.epfl.javelo.projection.PointCh;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class RouteComputerTest {

    private Graph graph;

    private RouteComputer newLausanneRouteComputer() {
        if (graph == null) {
            try {
                graph = Graph.loadFrom("lausanne");
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        var cf = new CityBikeCF(graph);
        return new RouteComputer(graph, cf);
    }

    @Test
    void routeComputerThrowsOnIdenticalStartAndEndNodes() {
        assertThrows(IllegalArgumentException.class, () -> {
            var rc = newLausanneRouteComputer();
            rc.bestRouteBetween(2022, 2022);
        });
    }

    @Test
    void routeComputerReturnsNullForUnreachableNodes() {
        var rc = newLausanneRouteComputer();
        assertNull(rc.bestRouteBetween(149195, 153181));
    }

    @Test
    void routeComputerComputesCorrectRouteForGivenExample() {
        var rc = newLausanneRouteComputer();
        var route = rc.bestRouteBetween(159049, 117669);
        assertNotNull(route);

        var actualLength = route.length();
        var expectedLength = 9588.5625;
        assertEquals(expectedLength, actualLength, 1);

        var actualPointsCount = route.points().size();
        var expectedPointsCount = 576;
        assertEquals(expectedPointsCount, actualPointsCount);

        var actualEdgesCount = route.edges().size();
        var expectedEdgesCount = 575;
        assertEquals(expectedEdgesCount, actualEdgesCount);

        var actualPointAt8k = route.pointAt(8000);
        var actualPointAt8kE = actualPointAt8k.e();
        var actualPointAt8kN = actualPointAt8k.n();
        var expected8kE = 2538281.263888889;
        var expected8kN = 1153278.236111111;
        assertEquals(expected8kE, actualPointAt8kE, 1);
        assertEquals(expected8kN, actualPointAt8kN, 1);
    }

    @Test
    void routeComputerComputesCorrectRouteForOtherExample() {
        var rc = newLausanneRouteComputer();
        var route = rc.bestRouteBetween(210641, 43713);
        assertNotNull(route);

        var actualLength = route.length();
        var expectedLength = 38612.75;
        assertEquals(expectedLength, actualLength, 1);

        var actualPointsCount = route.points().size();
        var expectedPointsCount = 1590;
        assertEquals(expectedPointsCount, actualPointsCount);

        var actualEdgesCount = route.edges().size();
        var expectedEdgesCount = 1589;
        assertEquals(expectedEdgesCount, actualEdgesCount);

        var actualPointAt20k = route.pointAt(20000);
        var actualPointAt20kE = actualPointAt20k.e();
        var actualPointAt20kN = actualPointAt20k.n();
        var expected20kE = 2533955.1600274723;
        var expected20kN = 1153195.4842032967;
        assertEquals(expected20kE, actualPointAt20kE, 1);
        assertEquals(expected20kN, actualPointAt20kN, 1);
    }

        public static void main(String[] args) throws IOException {
            Graph g = Graph.loadFrom("ch_west");
            CostFunction cf = new CityBikeCF(g);
            RouteComputer rc = new RouteComputer(g, cf);
            long t0 = System.nanoTime();
            Route r = rc.bestRouteBetween(2046055, 2694240);
            //Route r = rc.bestRouteBetween(210641, 43713);
            //Route r = rc.bestRouteBetween(159049, 117669);
            System.out.printf("Itinéraire calculé en %d ms\n",
                    (System.nanoTime() - t0) / 1_000_000);
            KmlPrinter.write("javelo.kml", r);
        }

    private final class KmlPrinter {
        private static final String KML_HEADER =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<kml xmlns=\"http://www.opengis.net/kml/2.2\"\n" +
                        "     xmlns:gx=\"http://www.google.com/kml/ext/2.2\">\n" +
                        "  <Document>\n" +
                        "    <name>JaVelo</name>\n" +
                        "    <Style id=\"byBikeStyle\">\n" +
                        "      <LineStyle>\n" +
                        "        <color>a00000ff</color>\n" +
                        "        <width>4</width>\n" +
                        "      </LineStyle>\n" +
                        "    </Style>\n" +
                        "    <Placemark>\n" +
                        "      <name>Path</name>\n" +
                        "      <styleUrl>#byBikeStyle</styleUrl>\n" +
                        "      <MultiGeometry>\n" +
                        "        <LineString>\n" +
                        "          <tessellate>1</tessellate>\n" +
                        "          <coordinates>";

        private static final String KML_FOOTER =
                "          </coordinates>\n" +
                        "        </LineString>\n" +
                        "      </MultiGeometry>\n" +
                        "    </Placemark>\n" +
                        "  </Document>\n" +
                        "</kml>";

        public static void write(String fileName, Route route)
                throws IOException {
            try (PrintWriter w = new PrintWriter(fileName)) {
                w.println(KML_HEADER);
                for (PointCh p : route.points())
                    w.printf(Locale.ROOT,
                            "            %.5f,%.5f\n",
                            Math.toDegrees(p.lon()),
                            Math.toDegrees(p.lat()));
                w.println(KML_FOOTER);
            }
        }
    }
}

