package ch.epfl.javelo.routing;

import ch.epfl.javelo.Math2;
import ch.epfl.javelo.Preconditions;
import ch.epfl.javelo.projection.PointCh;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Classe immuable représentant un itinéraire simple reliant un point de
 * départ et un point d'arrivée sans point de passage intermédiaire.
 *
 * @author Jean Perbet (341418)
 * @author Cassio Manuguerra (346232)
 */
public final class SingleRoute implements Route {

    private final List<Edge> edges;
    private final double[] positions;

    /**
     * Constructeur public d'un itinéraire simple.
     *
     * @param edges la liste des arêtes constituant l'itinéraire
     * @throws IllegalArgumentException si la liste d'arêtes est vide
     */
    public SingleRoute(List<Edge> edges) {
        Preconditions.checkArgument(!edges.isEmpty());
        this.edges = List.copyOf(edges);
        positions = new double[edges.size() + 1];
        for (int i = 1; i < edges.size() + 1; i++)
            positions[i] = positions[i - 1] + edges.get(i - 1).length();
    }

    @Override
    public int indexOfSegmentAt(double position) {
        return 0;
    }

    @Override
    public double length() {
        double totalLength = 0;
        for (Edge edge : edges) totalLength += edge.length();
        return totalLength;
    }

    @Override
    public List<Edge> edges() {
        return new ArrayList<>(edges);
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
        //Traitement de toutes les positions potentiellement passées en argument
        position = Math2.clamp(0, position, length());

        /* Recherche dichotomique de l'index éventuel de la position
        passée en argument dans le tableau des positions */
        int edgeId = Arrays.binarySearch(positions, position);

        //Cas où la position correspond exactement à celle d'un nœud
        if (edgeId == edges.size()) return edges.get(edges.size() - 1).toPoint();
        if (edgeId >= 0) return edges.get(edgeId).fromPoint();

        //Cas où la position correspond au milieu d'une arête
        Edge matchingEdge = edges.get(-edgeId - 2);
        double positionOnEdge = position - positions[-edgeId - 2];
        return matchingEdge.pointAt(positionOnEdge);
    }

    @Override
    public double elevationAt(double position) {
        //Traitement de toutes les positions potentiellement passées en argument
        position = Math2.clamp(0, position, length());

        /* Recherche dichotomique de l'index éventuel de la position
        passée en argument dans le tableau des positions */
        int edgeId = Arrays.binarySearch(positions, position);

        //Cas où la position correspond exactement à celle d'un nœud
        if (edgeId == edges.size()) return edges.get(edges.size() - 1).elevationAt(edges.get(edges.size() - 1).length());
        if (edgeId == 0) return edges.get(edgeId).elevationAt(0);

        //Traitement du cas limite où l'élévation du nœud prise en compte correspond à celle de l'arête suivante, qui est potentiellement sans profil
        if (edgeId > 0) {
            if (Float.isNaN((float) edges.get(edgeId).elevationAt(0))) return edges.get(edgeId - 1).elevationAt(edges.get(edgeId - 1).length());
            else return edges.get(edgeId).elevationAt(0);
        }

        //Cas où la position correspond au milieu d'une arête
        Edge matchingEdge = edges.get(-edgeId - 2);
        double positionOnEdge = position - positions[-edgeId - 2];
        return matchingEdge.elevationAt(positionOnEdge);
    }

    @Override
    public int nodeClosestTo(double position) {
        //Traitement de toutes les positions potentiellement passées en argument
        position = Math2.clamp(0, position, length());

         /* Recherche dichotomique de l'index éventuel de la position
        passée en argument dans le tableau des positions */
        int edgeId = Arrays.binarySearch(positions, position);

        //Cas où la position correspond exactement à celle d'un nœud
        if (edgeId == edges.size()) return edges.get(edges.size() - 1).toNodeId();
        if (edgeId >= 0) return edges.get(edgeId).fromNodeId();

        //Cas où la position correspond au milieu d'une arête
        Edge matchingEdge = edges.get(-edgeId - 2);
        double positionOnEdge = position - positions[-edgeId - 2];
        return positionOnEdge <= matchingEdge.length() / 2.0 ? matchingEdge.fromNodeId() : matchingEdge.toNodeId();
    }

    @Override
    public RoutePoint pointClosestTo(PointCh point) {
        RoutePoint routePoint = RoutePoint.NONE;
        int edgeId = 0;

        /* Itération sur les arêtes pour déterminer le projeté orthogonal du point sur chaque arête
        et le point de l'arête correspondant à cette position, puis emploi de la méthode min() de
        RoutePoint pour trouver le RoutePoint le plus proche du point passé en argument */
        for (Edge edge : edges) {
            PointCh closestPointOnEdge;
            double closestPositionOnEdge = edge.positionClosestTo(point);
            if (closestPositionOnEdge < 0)
                closestPointOnEdge = edge.fromPoint();
            else if (closestPositionOnEdge > edge.length())
                closestPointOnEdge = edge.toPoint();
            else
                closestPointOnEdge = edge.pointAt(closestPositionOnEdge);
            routePoint = routePoint.min(closestPointOnEdge, positions[edgeId++] + Math2.clamp(0, closestPositionOnEdge, edge.length()), point.distanceTo(closestPointOnEdge));
        }
        return routePoint;
    }
}

