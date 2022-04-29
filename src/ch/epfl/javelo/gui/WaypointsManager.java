package ch.epfl.javelo.gui;

import ch.epfl.javelo.data.Graph;
import ch.epfl.javelo.projection.PointWebMercator;
import javafx.beans.property.ObjectProperty;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.shape.SVGPath;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.zip.GZIPOutputStream;

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
    private int indexInWaypoints ;

    public WaypointsManager(Graph graph, ObjectProperty<MapViewParameters> property, ObservableList<Waypoint> waypoints, Consumer<String> consumer) {
        this.graph = graph;
        this.parameters = property;
        this.pane = new Pane();
        this.consumer = consumer;
        this.waypoints = waypoints;
        this.indexInWaypoints = 0 ;

        this.waypoints.addListener((ListChangeListener<? super Waypoint>) (c -> recreateWaypoints()));
        this.parameters.addListener((observableValue, oldS, newS) -> replaceWaypoints());

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
        int closestNodeId = graph.nodeClosestTo(parameters.get().pointAt((int)x, (int)y).toPointCh(), 1000);
        try {
            Waypoint waypoint = new Waypoint(graph.nodePoint(closestNodeId), closestNodeId);
            waypoints.add(waypoint);
        } catch(IndexOutOfBoundsException o){
            consumer.accept("Aucune route à proximité !");
        }
    }

    private void addToPane(double x, double y) {

        //Création des chemins SVG représentant les marqueurs
        SVGPath pinOutside = new SVGPath(), pinInside = new SVGPath() ;
        pinOutside.setContent("M-8-20C-5-14-2-7 0 0 2-7 5-14 8-20 20-40-20-40-8-20");
        pinInside.setContent("M0-23A1 1 0 000-29 1 1 0 000-23");
        pinOutside.getStyleClass().add("pin_outside");
        pinInside.getStyleClass().add("pin_inside");

        //Création du marqueur en lui-même contenant les chemins SVG
        Group mark = new Group(pinOutside, pinInside);
        mark.getStyleClass().add("pin");
        if(indexInWaypoints == 0) mark.getStyleClass().add("first");
        else if (indexInWaypoints == waypoints.size() - 1) mark.getStyleClass().add("last");
        else mark.getStyleClass().add("middle");

        mark.setOnMouseClicked(event -> {
            if(event.isStillSincePress()) {
                waypoints.remove(pane.getChildren().indexOf(mark)); //Appel à recreateWayPoints
                pane.getChildren().remove(mark);
            }
        });

        //Positionnement du marqueur sur le panneau
        mark.setLayoutX(x);
        mark.setLayoutY(y);
        pane.getChildren().add(mark);
    }

    private void recreateWaypoints() {
        pane.getChildren().clear();
        indexInWaypoints = 0 ;
        for (Waypoint waypoint : waypoints) {
            addToPane(parameters.get().viewX(PointWebMercator.ofPointCh(waypoint.wayPoint())),
                      parameters.get().viewY(PointWebMercator.ofPointCh(waypoint.wayPoint())));
            indexInWaypoints++ ;
        }
    }

    private void replaceWaypoints() {

    }
}
