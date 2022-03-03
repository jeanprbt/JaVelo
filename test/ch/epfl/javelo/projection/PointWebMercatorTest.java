package ch.epfl.javelo.projection;

import org.junit.jupiter.api.Test;

import java.awt.*;
import java.io.PipedOutputStream;

import static org.junit.jupiter.api.Assertions.*;

class PointWebMercatorTest {
    private static final double DELTA = 1e-7;

    @Test
    void pointWebMercatorThrowsOnInvalidCoordinates(){
        assertThrows(IllegalArgumentException.class, () -> {
            new PointWebMercator(-1, 1);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            new PointWebMercator(1, -1);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            new PointWebMercator(2, 1);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            new PointWebMercator(1, 2);
        });
    }

    @Test
    void ofWorksOnNonTrivialValues(){
        PointWebMercator test = PointWebMercator.of(19, 69561722, 47468099);
        assertEquals(0.518275214444, test.x(), DELTA);
        assertEquals(0.353664894749, test.y(), DELTA);
    }

    @Test
    void ofPointChWorksOnNonTrivialValues(){
        PointWebMercator test = new PointWebMercator(0.5182, 0.3536);
        PointCh test2 = new PointCh(Ch1903.e(test.lon(), test.lat()), Ch1903.n(test.lon(), test.lat()));
        assertEquals(test.lon(), PointWebMercator.ofPointCh(test2).lon(), DELTA);
    }

    @Test
    void xAtZoomLevelWorksOnNonTrivialValue(){
        PointWebMercator test = new PointWebMercator(0.518275214444, 0.353664894749);
        assertEquals(6.956172176138647E7, test.xAtZoomLevel(19));
    }

    @Test
    void yAtZoomLevelWorksOnNonTrivialValue(){
        PointWebMercator test = new PointWebMercator(0.518275214444, 0.353664894749);
        assertEquals(4.746809864656991E7, test.yAtZoomLevel(19));
    }

    @Test
    void lonAndLatWorkOnNonTrivialValue(){
        PointWebMercator test = new PointWebMercator(0.518275214444, 0.353664894749);
        assertEquals(Math.toRadians(6.5790772), test.lon(), DELTA); //lon
        assertEquals(Math.toRadians(46.5218976), test.lat(), DELTA); //lat
    }

    @Test
    void toPointChWorkOnNonTrivialValue(){
        PointWebMercator test = new PointWebMercator(0.518275214444, 0.353664894749);
        assertEquals(new PointCh(Ch1903.e(test.lon(), test.lat()), Ch1903.n(test.lon(), test.lat())), test.toPointCh());
    }

    @Test
    void toPointChThrowsWhenNotInSwissBounds(){
        PointCh test2 = null ;
        PointWebMercator test = new PointWebMercator(0,0);
        assertEquals(test2, test.toPointCh());
        PointWebMercator test3 = new PointWebMercator(0.8,0.8);
        assertEquals(test2, test3.toPointCh());
    }
}