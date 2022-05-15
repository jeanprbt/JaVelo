package ch.epfl.javelo.gui;

import ch.epfl.javelo.routing.*;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import java.util.*;

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

        this.routeComputer = routeComputer;

        waypoints = FXCollections.observableArrayList();
        route = new SimpleObjectProperty<>();
        elevationProfile = new SimpleObjectProperty<>();
        highlightedPosition = new SimpleDoubleProperty();

        /* Cache mémoire permettant d'enregistrer les itinéraires simples déjà calculés, dans la limite de 100 itinéraires.
        Cela permet de limiter le coût mémoire lors du recalcul d'itinéraire, qui est une opération parfois coûteuse */
        cacheMemory = new LinkedHashMap<>(20, 0.75f, true);

        /* Vérification à chaque changement de la liste des points de passage qu'il y ait au moins deux points
         dans la liste et appel de la méthode recomputeRoute() le cas échéant */
        waypoints.addListener((ListChangeListener<? super Waypoint>) (c -> {
            if (waypoints.size() < 2) {
                route.set(null);
                elevationProfile.set(null);
            } else recomputeRoute();
        }));
    }

    //Getter pour la propriété waypoints
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

    //Getters pour la propriété contenant le profil et sa valeur
    public ReadOnlyObjectProperty<ElevationProfile> elevationProfileProperty(){
        return elevationProfile ;
    }
    public ElevationProfile getElevationProfile(){
        return elevationProfile.get();
    }

    //Getters et setter pour la propriété highlightedPosition et sa valeur
    public DoubleProperty highlightedPositionProperty(){
        return highlightedPosition ;
    }
    public double getHighlightedPosition() {
        return highlightedPosition.get();
    }
    public void setHighlightedPosition(double value){
        highlightedPosition.setValue(value);
    }

    /**
     * Méthode permettant l'index du segment contenant la position passée en argument sur l'itinéraire, en ne prenant
     * pas en compte les segments vides (deux points de passage non consécutifs au même endroit, pour les boucles par ex.).
     *
     * @param position la position sur l'itinéraire dont on veut l'index du segment la contenant
     * @return l'index du segment contenant la position passée en argument en ignorant les segments vides
     */
    public int indexOfNonEmptySegmentAt(double position) {
        int index = route.get().indexOfSegmentAt(position);
        for (int i = 0; i <= index; i += 1) {
            int n1 = waypoints.get(i).closestNodeId();
            int n2 = waypoints.get(i + 1).closestNodeId();
            if (n1 == n2) index += 1;
        }
        return index;
    }

    //---------------------------------------------- Private ----------------------------------------------//

    /**
     * Méthode privée permettant, à chaque changement de la liste des points de passage, de recalculer l'itinéraire multiple
     * fait de tous les itinéraires simples entre chaque paire de points de passage consécutifs dans la liste.
     */
    private void recomputeRoute() {

        List<Route> segments = new ArrayList<>();
        boolean aSegmentIsNull = false ;

        /* Parcours de la liste des waypoints : à chaque waypoint, création d'une paire entre celui-ci et le suivant et calcul
        de l'itinéraire simple les séparant. Si tous les itinéraires simples ainsi calculés sont non nuls, création d'un
        itinéraire multiple les rassemblant tous et ajout de celui-ci dans la propriété route. */
        for (int i = 0; i < waypoints.size() - 1; i++) {
            Pair<Integer, Integer> singleRoute = new Pair<>(waypoints.get(i).closestNodeId(), waypoints.get(i+1).closestNodeId());

            //Si deux points de passage successifs sont associés au même nœud, aucune tentative de calcul d'itinéraire n'est faite
            if(singleRoute.firstElement.equals(singleRoute.secondElement)) continue;

            if(cacheMemory.containsKey(singleRoute))
                segments.add(cacheMemory.get(singleRoute));
            else {
                Route temp = routeComputer.bestRouteBetween(singleRoute.firstElement, singleRoute.secondElement);
                if(temp != null) {
                    segments.add(temp);
                    //Contrôle du grossissement du cache mémoire en supprimant son élément le plus ancien
                    if(cacheMemory.size() > MAX_ENTRIES) cacheMemory.remove(cacheMemory.keySet().iterator().next());
                    cacheMemory.put(singleRoute, temp);
                } else {
                    aSegmentIsNull = true ;
                    route.set(null);
                    elevationProfile.set(null);
                    break ;
                }
            }
        }
        if(!aSegmentIsNull && !segments.isEmpty()) {
            route.set(new MultiRoute(segments));
            elevationProfile.set(ElevationProfileComputer.elevationProfile(route.get(), 50));
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
