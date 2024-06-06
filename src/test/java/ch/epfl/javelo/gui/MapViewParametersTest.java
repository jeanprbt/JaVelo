package ch.epfl.javelo.gui;

import ch.epfl.javelo.projection.PointWebMercator;
import javafx.geometry.Point2D;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MapViewParametersTest {

    @Test
    void methodsDoWellConversion(){
        MapViewParameters params = new MapViewParameters(10, 135735, 92337);
        assertEquals(new Point2D(135735, 92337), params.topLeft());
    }

}