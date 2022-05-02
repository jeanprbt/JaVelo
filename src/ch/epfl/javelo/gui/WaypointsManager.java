package ch.epfl.javelo.gui;

import ch.epfl.javelo.data.Graph;
import ch.epfl.javelo.projection.PointCh;
import ch.epfl.javelo.projection.PointWebMercator;
import javafx.beans.property.ObjectProperty;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
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
    private Point2D initialCursorPosition ;

    public WaypointsManager(Graph graph, ObjectProperty<MapViewParameters> property, ObservableList<Waypoint> waypoints, Consumer<String> consumer) {

        this.graph = graph;
        this.parameters = property;
        this.pane = new Pane();
        this.consumer = consumer;
        this.waypoints = waypoints;
        this.indexInWaypoints = 0 ;

        //Ajout de listeners aux paramètres de fond de carte et à la liste des waypoints
        installListeners();

        //Laisser les gestionnaires d'évènement du fond de carte actifs malgré la superposition avec ceux des waypoints
        pane.setPickOnBounds(false);

        //Création des marqueurs des waypoints initiaux passés en argument
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
    public void addWayPoint(double x, double y) {
        PointCh point = parameters.get().pointAt((int) x, (int) y).toPointCh();
        int closestNodeId = graph.nodeClosestTo(point, 500);
        if (isAlreadyWaypoint(closestNodeId))
            consumer.accept("Il y a déjà un point de passage à cet endroit !");
        else if (closestNodeId == -1)
            consumer.accept("Aucune route à proximité !");
        else {
            Waypoint waypoint = new Waypoint(point, closestNodeId);
            waypoints.add(waypoint); //Appel à recreateWaypoints() pour la gestion graphique grâce à l'observateur sur waypoints
        }
    }

    /**
     * Méthode permettant, à chaque modification de la liste waypoints, de supprimer puis recréer tous les points de passage y
     * figurant toujours avec leurs attributs mis à jour (arrivée, départ ou intermédiaire).
     */
    private void recreateWaypoints() {
        pane.getChildren().clear();
        indexInWaypoints = 0 ;
        for (Waypoint waypoint : waypoints) {
            addToPane(parameters.get().viewX(PointWebMercator.ofPointCh(waypoint.wayPoint())),
                      parameters.get().viewY(PointWebMercator.ofPointCh(waypoint.wayPoint())));
            indexInWaypoints++ ;
        }
    }

    /**
     * Méthode replaçant les points de passages après un changement des paramètres de la carte, au drag ou au scroll
     * de la souris. Elle utilise les anciens paramètres de la carte pour calculer l'ancienne position de chaque
     * marqueur du panneau, puis y applique les méthodes viewX et viewY depuis les nouveaux paramètres de la carte
     * pour avoir leur position mise à jour.
     *
     * @param oldValue : les anciens paramètres de la carte
     */
    private void replaceWaypoints(MapViewParameters oldValue) {
        for (Node mark : pane.getChildren()) {
            mark.setLayoutX(parameters.get().viewX(PointWebMercator.of(oldValue.zoomLevel(),
                         mark.getLayoutX() + oldValue.x(), mark.getLayoutY() + oldValue.y())));
            mark.setLayoutY(parameters.get().viewY(PointWebMercator.of(oldValue.zoomLevel(),
                         mark.getLayoutX() + oldValue.x(), mark.getLayoutY() + oldValue.y())));
        }
    }


    /**
     * Méthode permettant d'ajouter au panneau un nouveau marqueur SVG avec les bons attributs et
     * d'y ajouter un observateur qui l'enlève à chaque clic sur ce dernier ainsi que des gestionnaires
     * d'évènements permettant de le déplacer.
     *
     * @param x la position X dans le panneau du marqueur
     * @param y la position Y dans le panneau du marqueur
     */
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

        //Installation des gestionnaires d'évènement pour le déplacement et la suppression des marqueurs
        installHandlers(mark);

        //Positionnement du marqueur sur le panneau
        mark.setLayoutX(x);
        mark.setLayoutY(y);
        pane.getChildren().add(mark);
    }

    /**
     * Méthode permettant d'installer les gestionnaires d'évènement sur le nœud mark afin
     * de gérer la suppression du marqueur et son déplacement en fonction des actions de souris.
     *
     * @param mark le nœud sur lequel ajouter les gestionnaires d'évènement
     */
    private void installHandlers(Node mark){

        //À chaque fois que la souris est cliquée sur un marqueur, suppression de ce dernier de la liste waypoints et du panneau
        mark.setOnMouseClicked(event -> {
            if(event.isStillSincePress()) {
                waypoints.remove(pane.getChildren().indexOf(mark));
                pane.getChildren().remove(mark);
            }
        });

        //À chaque fois que la souris est pressée, enregistrement de la position actuelle du curseur
        mark.setOnMousePressed(event -> {
            initialCursorPosition = new Point2D(event.getX(), event.getY());
        });

        //À chaque fois que la souris est décalée depuis un marqueur, mise à jour de la position de ce dernier en fonction
        mark.setOnMouseDragged(event -> {
            Point2D translation = initialCursorPosition.subtract(event.getX(), event.getY());
            mark.setLayoutX(mark.getLayoutX() - translation.getX());
            mark.setLayoutY(mark.getLayoutY() - translation.getY());
        });

        /* À chaque fois que la souris est relâchée après avoir été décalée depuis un marqueur, mise à jour du waypoint correspondant :
        son pointCh et son closestNodeId correspondent désormais à la nouvelle position, ce qui entraîne une modification de la liste
        waypoints et l'appel à la méthode recreateWaypoints(). Si aucun nœud n'existe, le waypoint n'est pas modifié et le marqueur est
        simplement replacé à sa position initiale. */
        mark.setOnMouseReleased(event -> {
            if (!event.isStillSincePress()){
                PointCh pointCh = parameters.get().pointAt((int)mark.getLayoutX(), (int)mark.getLayoutY()).toPointCh();
                int closestNodeId = graph.nodeClosestTo(pointCh, 1000);
                if(closestNodeId != -1 && !isAlreadyWaypoint(closestNodeId))
                    waypoints.set(pane.getChildren().indexOf(mark), new Waypoint(pointCh, closestNodeId));
                else if (isAlreadyWaypoint(closestNodeId)){
                    consumer.accept("Il y a déjà un point de passage à cet endroit !");
                    recreateWaypoints();
                }
                else {
                    consumer.accept("Aucune route à proximité !");
                    recreateWaypoints();
                }
            }});
    }

    /**
     * Méthode permettant d'installer des listeners sur la liste des waypoints et sur les paramètres de
     * fond de carte afin de recréer tous les waypoints et leur marqueur.
     */
    private void installListeners(){
        this.waypoints.addListener((ListChangeListener<? super Waypoint>) (c -> recreateWaypoints()));
        this.parameters.addListener((observableValue, oldValue, newValue) -> replaceWaypoints(oldValue));
    }

    /**
     * Méthode permettant de savoir si l'identité donnée correspond déjà à un point de passage existant.
     *
     * @param nodeId : l'identité du nœud dont on veut vérifier la disponibilité
     * @return true s'il y a déjà un point de passage et false sinon
     */
    private boolean isAlreadyWaypoint(int nodeId){
        for (Waypoint waypoint : waypoints) {
            if(nodeId == waypoint.closestNodeId()) return true ;
        }
        return false ;
    }
}
