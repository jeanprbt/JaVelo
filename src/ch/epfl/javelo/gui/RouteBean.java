package ch.epfl.javelo.gui;

import ch.epfl.javelo.routing.*;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Classe regroupant les propriétés relatives aux points de passage et à l'itinéraire correspondant.
 *
 * @author Jean Perbet (341418)
 * @author Cassio Manuguerra (346232)
 */
public final class RouteBean {

    private final ObservableList<Waypoint> waypoints ;
    private final ObjectProperty<Route> route ;
    private final ObjectProperty<ElevationProfile> elevationProfile ;
    private final DoubleProperty highlightedPosition ;
    private final RouteComputer routeComputer ;
    private final Map<Pair<Integer, Integer>, Route> cacheMemory ;

    public RouteBean(RouteComputer routeComputer) {
        this.routeComputer = routeComputer ;
        this.waypoints = FXCollections.observableArrayList();
        this.route = new SimpleObjectProperty<>();
        this.elevationProfile = new SimpleObjectProperty<>();
        this.highlightedPosition = new SimpleDoubleProperty();
        this.cacheMemory = new LinkedHashMap<>(){
            final int MAX_ENTRIES = 100;
            @Override
            protected boolean removeEldestEntry(Map.Entry eldest) {
                return size() > MAX_ENTRIES;
            }
        };

        this.waypoints.addListener((ListChangeListener<? super Waypoint>) (c -> {
            if(waypoints.size() < 2) {
                route.set(null);
                elevationProfile.set(null);
            } else {
                recomputeRoute();
             }}));
        }

    //Getter et Setter pour la propriété waypoints
    public ObservableList<Waypoint> getWaypoints() {
        return waypoints;
    }

    //Getters pour la propriété route et sa valeur
    public ReadOnlyObjectProperty<Route> routeProperty() {
        return route;
    }
    public Route getRoute() {
        return route.get();
    }

    //Getters et setter pour la propriété highlightedPosition et sa valeur
    public DoubleProperty highlightedPositionProperty(){
        return highlightedPosition ;
    }
    public void setHighlightedPosition(double value){
        highlightedPosition.setValue(value);
    }

    private void recomputeRoute() {
        List<Route> segments = new ArrayList<>();
        boolean segmentIsNull = false ;
        for (int i = 0; i < waypoints.size() - 1; i++) {
            Pair<Integer, Integer> singleRoute = new Pair<>(waypoints.get(i).closestNodeId(), waypoints.get(i+1).closestNodeId());
            if(cacheMemory.containsKey(singleRoute)) {
                segments.add(cacheMemory.get(singleRoute));
            }
            else {
                Route temp = routeComputer.bestRouteBetween(singleRoute.firstElement, singleRoute.secondElement);
                if(temp != null) {
                    segments.add(temp);
                    cacheMemory.put(singleRoute, temp);
                } else {
                    segmentIsNull = true ;
                    route.set(null);
                    elevationProfile.set(null);
                }
            }
        }
        if(!segmentIsNull) {
            route.set(new MultiRoute(segments));
            elevationProfile.set(ElevationProfileComputer.elevationProfile(route.get(), 5));
        }
    }

    /**
     * Enregistrement représentant une paire d'éléments, utile pour le cache mémoire.
     * @param <T> le type du premier élément
     * @param <S> le type du second élément
     * @param firstElement le premier élément
     * @param secondElement le second élément
     */
    private record Pair<T, S>(T firstElement, S secondElement) {}
}
