package ch.epfl.javelo.routing;

import ch.epfl.javelo.Math2;
import ch.epfl.javelo.Preconditions;
import ch.epfl.javelo.projection.PointCh;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe représentant un itinéraire multiple, c.-à-d. composé d'une séquence d'itinéraires contigus nommés segments,
 * pouvant être des itinéraires simples ou des itinéraires multiples eux-mêmes.
 *
 * @author Jean Perbet (341418)
 * @author Cassio Manuguerra (346232)
 */
public final class MultiRoute implements Route {

    private final List<Route> segments ;

    /**
     * Constructeur public d'un itinéraire multiple.
     *
     * @param segments la liste des segments constituant l'itinéraire
     * @throws IllegalArgumentException si la liste de segments est vide
     */
    public MultiRoute(List<Route> segments){
        Preconditions.checkArgument(!segments.isEmpty());
        this.segments = List.copyOf(segments);
    }

    @Override
    public int indexOfSegmentAt(double position) {
        position = Math2.clamp(0, position, length());
        int indexOfSegment = 0 ;
        for (Route segment : segments) {
            if(position - segment.length() <= 0) {
                indexOfSegment += segment.indexOfSegmentAt(position);
                return indexOfSegment;
            }
            indexOfSegment += segment.indexOfSegmentAt(segment.length()) + 1;
            position -= segment.length() ;
        }
        return 0 ;
    }

    @Override
    public double length() {
        double totalLength = 0 ;
        for (Route segment : segments) totalLength += segment.length();
        return totalLength ;
    }

    @Override
    public List<Edge> edges() {
        List<Edge> edges = new ArrayList<>() ;
        for (Route segment : segments) edges.addAll(segment.edges());
        return edges ;
    }

    @Override
    public List<PointCh> points() {
        List<PointCh> points = new ArrayList<>();
        for (Route segment : segments) {
            points.addAll(segment.points());
            points.remove(points.size() - 1);
        }
        points.add(segments.get(segments.size() - 1).pointAt(segments.get(segments.size() - 1).length()));
        return points ;
    }

    @Override
    public PointCh pointAt(double position) {
        position = Math2.clamp(0, position, length());
        for (Route segment : segments) {
            if(position - segment.length() <= 0) return segment.pointAt(position);
            position -= segment.length();
        }
        return null;
    }

    @Override
    public double elevationAt(double position) {
        position = Math2.clamp(0, position, length());
        for (Route segment : segments) {
            if(position - segment.length() <= 0) return segment.elevationAt(position);
            position -= segment.length() ;
        }
        return 0;
    }

    @Override
    public int nodeClosestTo(double position) {
        position = Math2.clamp(0, position, length());
        for (Route segment : segments) {
            if(position - segment.length() <= 0) return segment.nodeClosestTo(position);
            position -= segment.length() ;
        }
        return 0;
    }

    @Override
    public RoutePoint pointClosestTo(PointCh point) {
        double totalPosition = 0;
        RoutePoint routePoint = RoutePoint.NONE ;
        for (Route segment : segments) {
            RoutePoint closestPoint = segment.pointClosestTo(point);
            totalPosition += closestPoint.position();
            routePoint = routePoint.min(closestPoint.point(), totalPosition, closestPoint.distanceToReference());
        }
        return routePoint ;
    }
}
