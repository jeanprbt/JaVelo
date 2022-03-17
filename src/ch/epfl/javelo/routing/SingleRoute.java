package ch.epfl.javelo.routing;

import ch.epfl.javelo.Math2;
import ch.epfl.javelo.Preconditions;
import ch.epfl.javelo.projection.PointCh;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Classe représentant un itinéraire simple reliant un point de
 * départ et un point d'arrivée sans point de passage intermédiaire.
 *
 * @author Jean Perbet (341418)
 * @author Cassio Manuguerra (346232)
 */
public final class SingleRoute implements Route {

    private final List<Edge> edges ;
    private final double[] positions ;

    /**
     * Constructeur public d'un itinéraire simple.
     *
     * @param edges la liste des arêtes constituant l'itinéraire
     * @throws IllegalArgumentException si la liste d'arêtes est vide
     */
    public SingleRoute(List<Edge> edges){
        Preconditions.checkArgument(!edges.isEmpty());
        this.edges = List.copyOf(edges);
        positions = new double[edges.size() + 1];
        for (int i = 1 ; i < edges.size() + 1; i++) positions[i] = positions[i - 1] + edges.get(i - 1).length() ;
    }

    @Override
    public int indexOfSegmentAt(double position) {
        return 0;
    }

    @Override
    public double length() {
        double totalLength = 0 ;
        for (Edge edge : edges) totalLength += edge.length() ;
        return totalLength ;
    }

    @Override
    public List<Edge> edges() {
        return edges ;
    }

    @Override
    public List<PointCh> points() {
        List<PointCh> points = new ArrayList<>();
        for (Edge edge : edges) {
            points.add(edge.fromPoint());
        }
        points.add(edges.get(edges.size() - 1).toPoint());
        return points;
    }

    @Override
    public PointCh pointAt(double position) {
        position = Math2.clamp(0, position, length());
        int bound = Arrays.binarySearch(positions, position);
        if (bound >= 0)  return edges.get(bound).pointAt(0);
        return edges.get(-(bound + 2)).pointAt(position - positions[-(bound + 2)]);
    }

    @Override
    public double elevationAt(double position) {
        position = Math2.clamp(0, position, length());
        int bound = Arrays.binarySearch(positions, position);
        if (bound >= 0) return edges.get(bound).elevationAt(0);
        return edges.get(-(bound + 2)).elevationAt(position - positions[-(bound + 2)]);

    }

    @Override
    public int nodeClosestTo(double position) {
        position = Math2.clamp(0, position, length());
        int bound = Arrays.binarySearch(positions, position);
        if(bound >= 0) return edges.get(bound).fromNodeId() ;
        return position - positions[-(bound + 2)] < edges.get(-(bound + 2)).length()/2.0 ? edges.get(-(bound + 2)).fromNodeId() : edges.get(-(bound + 2)).toNodeId();
    }

    @Override
    public RoutePoint pointClosestTo(PointCh point) {
        RoutePoint routePoint = RoutePoint.NONE;
        // Itération sur les arêtes pour déterminer le projeté orthogonal du point sur l'arête et
        // le point de l'arête correspondant à cette position, puis emploi de la méthode min() de
        // RoutePoint pour trouver le RoutePoint le plus proche du point passé en argument ;
        for (Edge edge : edges) {
            PointCh closestPointOnEdge ;
            double closestPositionOnEdge = edge.positionClosestTo(point) ;
            if (closestPositionOnEdge < 0) closestPointOnEdge = edge.fromPoint();
            else if (closestPositionOnEdge > edge.length()) closestPointOnEdge = edge.toPoint() ;
            else closestPointOnEdge = edge.pointAt(closestPositionOnEdge);
            routePoint = routePoint.min(closestPointOnEdge, closestPositionOnEdge, point.distanceTo(closestPointOnEdge));
        }
        return routePoint;
    }
}

