package ch.epfl.javelo.gui;

import ch.epfl.javelo.data.Graph;
import ch.epfl.javelo.projection.PointCh;
import ch.epfl.javelo.projection.PointWebMercator;
import javafx.beans.property.ObjectProperty;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.shape.SVGPath;

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
    private final ObservableList<Waypoint> waypoints ;

    private int indexInWaypoints ;
    private Point2D cursorPosition;

    private final int SEARCH_DISTANCE = 500 ;

    public WaypointsManager(Graph graph,
                            ObjectProperty<MapViewParameters> property,
                            ObservableList<Waypoint> waypoints,
                            Consumer<String> consumer) {

        this.graph = graph;
        this.waypoints = waypoints;

        parameters = property;
        pane = new Pane();
        indexInWaypoints = 0;

        //Laisser les gestionnaires d'évènement du fond de carte actifs malgré la superposition avec ceux des waypoints
        pane.setPickOnBounds(false);

        //Ajout de listeners aux paramètres de fond de carte et à la liste des waypoints
        installListeners();
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
    public void addWaypoint(double x, double y) {

        PointCh waypointPosition = parameters.get().pointAt((int) x, (int) y).toPointCh();
        int closestNodeId = graph.nodeClosestTo(waypointPosition, SEARCH_DISTANCE);

        if (closestNodeId == -1) {
            consumer.accept("Aucune route à proximité");
        }
        else {
            Waypoint waypoint = new Waypoint(waypointPosition, closestNodeId);
            waypoints.add(waypoint);  //Appel à recreateWaypoints() pour la gestion graphique grâce à l'observateur sur waypoints
        }
    }

    //---------------------------------------------- Private ----------------------------------------------//

    /**
     * Méthode permettant, à chaque modification de la liste waypoints, de supprimer puis recréer tous les
     * points de passage y figurant toujours avec leurs attributs mis à jour (arrivée, départ ou intermédiaire).
     */
    private void updateWaypoints() {
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
            PointWebMercator oldMarkPosition = PointWebMercator.of(oldValue.zoomLevel(),
                                                                oldValue.x() + mark.getLayoutX(),
                                                                oldValue.y() + mark.getLayoutY());
            mark.setLayoutX(parameters.get().viewX(oldMarkPosition));
            mark.setLayoutY(parameters.get().viewY(oldMarkPosition));
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

        //Ajout du marqueur au panneau
        pane.getChildren().add(mark);
    }

    /**
     * Méthode privée permettant d'installer des listeners sur la liste des waypoints et sur les paramètres de
     * fond de carte afin de recréer tous les waypoints et leur marqueur si ces derniers changent.
     */
    private void installListeners(){
        waypoints.addListener((ListChangeListener<? super Waypoint>) (c -> updateWaypoints()));
        parameters.addListener((observableValue, oldValue, newValue) -> replaceWaypoints(oldValue));
    }

    /**
     * Méthode privée permettant d'installer les gestionnaires d'évènement sur le nœud mark afin
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
            cursorPosition = new Point2D(event.getX(), event.getY());
        });

        //À chaque fois que la souris est décalée depuis un marqueur, mise à jour de la position de ce dernier en fonction
        mark.setOnMouseDragged(event -> {
            Point2D translation = cursorPosition.subtract(event.getX(), event.getY());
            mark.setLayoutX(mark.getLayoutX() - translation.getX());
            mark.setLayoutY(mark.getLayoutY() - translation.getY());
        });

        /* À chaque fois que la souris est relâchée après avoir été décalée depuis un marqueur, mise à jour du waypoint
         correspondant : son pointCh et son closestNodeId correspondent désormais à la nouvelle position, ce qui entraîne
         une modification de la liste waypoints et l'appel à la méthode recreateWaypoints(). Si aucun nœud n'existe,
         le waypoint n'est pas modifié et le marqueur est simplement replacé à sa position initiale. */
        mark.setOnMouseReleased(event -> {
            if (!event.isStillSincePress()) {

                PointCh pointCh = parameters.get().pointAt((int) mark.getLayoutX(), (int) mark.getLayoutY()).toPointCh();
                int closestNodeId = graph.nodeClosestTo(pointCh, SEARCH_DISTANCE);

                if (closestNodeId == -1){
                    consumer.accept("Aucune route à proximité !");
                    updateWaypoints();
                } else {
                    waypoints.set(pane.getChildren().indexOf(mark), new Waypoint(pointCh, closestNodeId));
                }
            }});
    }
}
