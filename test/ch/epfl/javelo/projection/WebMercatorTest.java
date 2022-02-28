package ch.epfl.javelo.projection;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class WebMercatorTest {
    private static final double DELTA = 1e-7;

    @Test
    void xWorksOnNonTrivialValue(){
        assertEquals(0.518275214444, WebMercator.x(Math.toRadians(6.5790772)), DELTA);
    }

    @Test
    void yWorksOnNonTrivialValue(){
        assertEquals(0.353664894749, WebMercator.y(Math.toRadians(46.5218976)), DELTA);
    }

    @Test
    void lonWorksOnNonTrivialValue(){
        assertEquals(Math.toRadians(6.5790772), WebMercator.lon(0.518275214444), DELTA);
    }

    @Test
    void latWorksOnNonTrivialValue(){
        assertEquals(Math.toRadians(46.5218976), WebMercator.lat(0.353664894749), DELTA);
    }
}
