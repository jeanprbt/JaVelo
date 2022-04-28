package ch.epfl.javelo.gui;

import ch.epfl.javelo.data.Graph;
import ch.epfl.javelo.projection.PointWebMercator;
import javafx.beans.property.ObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.layout.Pane;
import javafx.scene.shape.SVGPath;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Classe gérant l'interaction et l'affichage avec les points de passage.
 *
 * @author Jean Perbet (341418)
 * @author Cassio Manuguerra (346232)
 */
public final class WaypointsManager {

    private final Graph graph;
    private final ObjectProperty<MapViewParameters> parameters ;
    private final Pane pane ;
    private final Consumer<String> consumer;
    private final ObservableList<Waypoint> waypoints ;
    private final Map<Group, Waypoint> marks ;

    public WaypointsManager(Graph graph, ObjectProperty<MapViewParameters> property, ObservableList<Waypoint> waypoints, Consumer<String> consumer) {
        this.graph = graph;
        this.parameters = property;
        this.pane = new Pane();
        this.consumer = consumer;
        this.marks = new HashMap<>();
        this.waypoints = waypoints;

        this.waypoints.addListener((ListChangeListener<? super Waypoint>) (c -> {
            recreateWaypoints();
        }));

        pane.setPickOnBounds(false);
        recreateWaypoints();

    }

    /**
     * Méthode retournant le panneau JavaFX contenant les point de passage.
     *
     * @return le panneau contenant les points de passage
     */
    public Pane pane(){
        return pane ;
    }

    /**
     * Méthode permettant d'ajouter un point de passage au nœud du graphe qui en est le plus proche.
     *
     * @param x la coordonnée X du point de passage
     * @param y la coordonnée Y du point de passage
     */
    public void addWayPoint(double x, double y){

        //Création des chemins SVG représentant les marqueurs
        SVGPath pinOutside = new SVGPath(), pinInside = new SVGPath() ;
        pinOutside.setContent("M-8-20C-5-14-2-7 0 0 2-7 5-14 8-20 20-40-20-40-8-20");
        pinInside.setContent("M0-23A1 1 0 000-29 1 1 0 000-23");
        pinOutside.getStyleClass().add("pin_outside");
        pinInside.getStyleClass().add("pin_inside");

        //Création du marqueur en lui-même contenant les chemins SVG
        Group mark = new Group(pinOutside, pinInside);
        mark.getStyleClass().add("pin");
        if(waypoints.isEmpty()) mark.getStyleClass().add("first");
        else if (waypoints.size() > 1) mark.getStyleClass().add("middle");
        else mark.getStyleClass().add("last");

        mark.setOnMouseClicked(event -> {
            if(event.isStillSincePress()) {
                pane.getChildren().remove(mark);
                waypoints.remove(marks.get(mark));
                marks.remove(mark);
            }
        });


        //Positionnement du marqueur sur le panneau
        mark.setLayoutX(x);
        mark.setLayoutY(y);
        pane.getChildren().add(mark);

        int closestNodeId = graph.nodeClosestTo(parameters.get().pointAt((int)x, (int)y).toPointCh(), 1000);
        if (closestNodeId == -1) consumer.accept("Aucune route à proximité !");
        Waypoint waypoint = new Waypoint(graph.nodePoint(closestNodeId), closestNodeId) ;
        waypoints.add(waypoint);
        marks.put(mark, waypoint);
    }

    private void recreateWaypoints() {
        pane.getChildren().removeAll();
        for (Waypoint waypoint : waypoints) {
            addWayPoint(parameters.get().viewX(PointWebMercator.ofPointCh(waypoint.wayPoint())),
                        parameters.get().viewY(PointWebMercator.ofPointCh(waypoint.wayPoint())));
        }

    }

    private void placeWaypoints() {

    }
}
