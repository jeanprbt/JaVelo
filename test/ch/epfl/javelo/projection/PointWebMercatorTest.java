package ch.epfl.javelo.projection;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PointWebMercatorTest {
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
        PointWebMercator expected = new PointWebMercator(0.518275214444, 0.353664894749);
        assertEquals(expected, PointWebMercator.of(19, 69561722, 47468099 ));
    }
}