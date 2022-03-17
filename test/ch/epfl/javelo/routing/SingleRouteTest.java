package ch.epfl.javelo.routing;

import ch.epfl.javelo.Functions;
import ch.epfl.javelo.projection.PointCh;
import ch.epfl.javelo.projection.SwissBounds;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleUnaryOperator;

import static org.junit.jupiter.api.Assertions.*;

class SingleRouteTest {
    @Test
    void constructorThrowsOnEmptyArgument(){
        assertThrows(IllegalArgumentException.class, () ->{
            new SingleRoute(List.of());
        });
    }

    @Test
    void lengthWorksProperly(){
        List<Edge> list = new ArrayList<>();
        DoubleUnaryOperator profile = Functions.constant(0);
        list.add(new Edge(0, 1, new PointCh(SwissBounds.MIN_E, SwissBounds.MIN_N), new PointCh(SwissBounds.MIN_E + 10, SwissBounds.MIN_N), 10, profile));
        list.add(new Edge(1, 2, new PointCh(SwissBounds.MIN_E + 10, SwissBounds.MIN_N), new PointCh(SwissBounds.MIN_E + 13, SwissBounds.MIN_N + 4), 5, profile));
        list.add(new Edge(2, 3, new PointCh(SwissBounds.MIN_E + 13, SwissBounds.MIN_N + 4), new PointCh(SwissBounds.MIN_E + 13, SwissBounds.MIN_N + 10), 6, profile));
        SingleRoute route = new SingleRoute(list);
        assertEquals(21, route.length());
    }

    @Test
    void pointsWorksProperly(){
        List<Edge> list = new ArrayList<>();
        DoubleUnaryOperator profile = Functions.constant(0);
        list.add(new Edge(0, 1, new PointCh(SwissBounds.MIN_E, SwissBounds.MIN_N), new PointCh(SwissBounds.MIN_E + 10, SwissBounds.MIN_N), 10, profile));
        list.add(new Edge(1, 2, new PointCh(SwissBounds.MIN_E + 10, SwissBounds.MIN_N), new PointCh(SwissBounds.MIN_E + 13, SwissBounds.MIN_N + 4), 5, profile));
        list.add(new Edge(2, 3, new PointCh(SwissBounds.MIN_E + 13, SwissBounds.MIN_N + 4), new PointCh(SwissBounds.MIN_E + 13, SwissBounds.MIN_N + 10), 6, profile));
        SingleRoute route = new SingleRoute(list);
        List<PointCh> points = new ArrayList<>();
        points.add(new PointCh(SwissBounds.MIN_E, SwissBounds.MIN_N));
        points.add(new PointCh(SwissBounds.MIN_E + 10, SwissBounds.MIN_N));
        points.add(new PointCh(SwissBounds.MIN_E + 13, SwissBounds.MIN_N + 4));
        points.add(new PointCh(SwissBounds.MIN_E + 13, SwissBounds.MIN_N + 10));
        assertEquals(points, route.points());
    }

    @Test
    void pointAtWorksProperly(){
        List<Edge> list = new ArrayList<>();
        DoubleUnaryOperator profile = Functions.constant(0);
        list.add(new Edge(0, 1, new PointCh(SwissBounds.MIN_E, SwissBounds.MIN_N), new PointCh(SwissBounds.MIN_E + 10, SwissBounds.MIN_N), 10, profile));
        list.add(new Edge(1, 2, new PointCh(SwissBounds.MIN_E + 10, SwissBounds.MIN_N), new PointCh(SwissBounds.MIN_E + 13, SwissBounds.MIN_N + 4), 5, profile));
        list.add(new Edge(2, 3, new PointCh(SwissBounds.MIN_E + 13, SwissBounds.MIN_N + 4), new PointCh(SwissBounds.MIN_E + 13, SwissBounds.MIN_N + 10), 6, profile));
        SingleRoute route = new SingleRoute(list);

        PointCh point1 = new PointCh(SwissBounds.MIN_E + 4, SwissBounds.MIN_N);
        assertEquals(point1, route.pointAt(4));

        PointCh point2 = new PointCh(SwissBounds.MIN_E, SwissBounds.MIN_N);
        assertEquals(point2, route.pointAt(0));
        assertEquals(point2, route.pointAt(-10));

        PointCh point3 = new PointCh(SwissBounds.MIN_E + 13, SwissBounds.MIN_N + 10);
        assertEquals(point3, route.pointAt(1000));
        assertEquals(point3, route.pointAt(21));

        PointCh point4 = new PointCh(SwissBounds.MIN_E + 13, SwissBounds.MIN_N + 5);
        assertEquals(point4, route.pointAt(16));
    }

    @Test
    void elevationAtWorksProperly(){
        List<Edge> list = new ArrayList<>();
        DoubleUnaryOperator profile = Functions.constant(0);
        list.add(new Edge(0, 1, new PointCh(SwissBounds.MIN_E, SwissBounds.MIN_N), new PointCh(SwissBounds.MIN_E + 10, SwissBounds.MIN_N), 10, profile));
        list.add(new Edge(1, 2, new PointCh(SwissBounds.MIN_E + 10, SwissBounds.MIN_N), new PointCh(SwissBounds.MIN_E + 13, SwissBounds.MIN_N + 4), 5, profile));
        list.add(new Edge(2, 3, new PointCh(SwissBounds.MIN_E + 13, SwissBounds.MIN_N + 4), new PointCh(SwissBounds.MIN_E + 13, SwissBounds.MIN_N + 10), 6, profile));
        SingleRoute route = new SingleRoute(list);
    }

    @Test
    void nodeClosestToWorksProperly(){
        List<Edge> list = new ArrayList<>();
        DoubleUnaryOperator profile = Functions.constant(0);
        list.add(new Edge(0, 1, new PointCh(SwissBounds.MIN_E, SwissBounds.MIN_N), new PointCh(SwissBounds.MIN_E + 10, SwissBounds.MIN_N), 10, profile));
        list.add(new Edge(1, 2, new PointCh(SwissBounds.MIN_E + 10, SwissBounds.MIN_N), new PointCh(SwissBounds.MIN_E + 13, SwissBounds.MIN_N + 4), 5, profile));
        list.add(new Edge(2, 3, new PointCh(SwissBounds.MIN_E + 13, SwissBounds.MIN_N + 4), new PointCh(SwissBounds.MIN_E + 13, SwissBounds.MIN_N + 10), 6, profile));
        SingleRoute route = new SingleRoute(list);


        assertEquals(2, route.nodeClosestTo(14));
        assertEquals(2, route.nodeClosestTo(17));
        assertEquals(0, route.nodeClosestTo(-10));
        assertEquals(3, route.nodeClosestTo(1234));
        assertEquals(1, route.nodeClosestTo(12.5));
    }



}