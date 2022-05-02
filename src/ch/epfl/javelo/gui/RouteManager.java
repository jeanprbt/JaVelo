package ch.epfl.javelo.gui;

import ch.epfl.javelo.projection.PointCh;
import ch.epfl.javelo.projection.PointWebMercator;
import ch.epfl.javelo.routing.RoutePoint;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.geometry.Point2D;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polyline;

import java.util.function.Consumer;

/**
 * Classe gérant une partie de l'interaction et l'affichage avec l'itinéraire.
 *
 * @author Jean Perbet (341418)
 * @author Cassio Manuguerra (346232)
 */
public final class RouteManager {

    private final Pane pane ;
    private final Circle circle ;
    private final Polyline polyline ;
    private final RouteBean route ;
    private final ReadOnlyObjectProperty<MapViewParameters> parameters;

    public RouteManager(RouteBean route, ReadOnlyObjectProperty<MapViewParameters> parameters, Consumer<String> consumer){
        this.circle = new Circle(5);
        this.polyline = new Polyline();
        this.pane = new Pane(polyline, circle);
        this.route = route ;
        this.parameters = parameters ;

        circle.setVisible(false);
        circle.setId("highlight");
        polyline.setId("route");
        pane.setPickOnBounds(false);

        route.routeProperty().addListener((observable, oldValue, newValue) -> {
            recreateRoute();
            replaceCircle();
        });

        route.highlightedPositionProperty().addListener((observable, oldValue, newValue) -> replaceCircle());

        parameters.addListener(((observable, oldValue, newValue) -> {
            if(newValue.zoomLevel() != oldValue.zoomLevel()) recreateRoute();
            else replaceRoute(oldValue);
        }));


        circle.setOnMouseClicked(event -> {
            Point2D clickInPane = pane.localToParent(circle.getCenterX(), circle.getCenterY());
            PointWebMercator clickInMercator = parameters.get().pointAt((int)clickInPane.getX(), (int)clickInPane.getY());
            RoutePoint clickInRoute = route.getRoute().pointClosestTo(clickInMercator.toPointCh());
            int clickNodeId = route.getRoute().nodeClosestTo(route.getHighlightedPosition());
            if (isNotAlreadyWaypoint(clickNodeId))
                route.getWaypoints().add(route.getRoute().indexOfSegmentAt(route.getHighlightedPosition()) + 1,
                                                new Waypoint(clickInRoute.point(), clickNodeId));
            else consumer.accept("Un point de passage est déjà présent à cet endroit !");
        });
    }

    /**
     * Méthode retournant le panneau JavaFX contenant l'itinéraire.
     *
     * @return le panneau contenant l'itinéraire
     */
    public Pane pane() {
        return pane ;
    }

    /**
     * Méthode permettant d'afficher et de construire ou de rendre invisible
     * la polyLine correspondant à l'itinéraire donné par la propriété route.
     */
    private void recreateRoute(){
        if(route.getRoute() == null)
            polyline.setVisible(false);
        else {
            polyline.setVisible(true);
            polyline.getPoints().clear();
            addToPane();
        }
    }

    /**
     * Méthode replaçant la polyline après un changement des paramètres x ou y de la carte, au drag de la souris.
     * Elle utilise les anciens paramètres de la carte pour calculer l'ancienne position de l'itinéraire, puis y applique
     * les méthodes viewX et viewY depuis les nouveaux paramètres de la carte pour avoir sa position mise à jour.
     *
     * @param oldValue : les anciens paramètres de la carte
     */
    private void replaceRoute(MapViewParameters oldValue){
        polyline.setLayoutX(polyline.getLayoutX() + oldValue.x() - parameters.get().x());
        polyline.setLayoutY(polyline.getLayoutY() + oldValue.y() - parameters.get().y());
    }

    /**
     * Méthode permettant de replacer le cercle et de gérer sa visibilité lorsque l'itinéraire ou les paramètres
     * de fond de carte changent.
     */
    private void replaceCircle(){
        if(route.getRoute() == null){
            circle.setVisible(false);
        } else {
            circle.setVisible(true);
            PointCh point = route.getRoute().pointAt(route.getHighlightedPosition());
            circle.setCenterX(parameters.get().viewX(PointWebMercator.ofPointCh(point)));
            circle.setCenterY(parameters.get().viewY(PointWebMercator.ofPointCh(point)));
        }
    }

    /**
     * Méthode permettant d'ajouter au panneau une nouvelle polyLine correspondant
     * aux paramètres de carte et à l'itinéraire actuels.
     */
    private void addToPane() {
        for (PointCh point : route.getRoute().points()) {
            double x = PointWebMercator.ofPointCh(point).xAtZoomLevel(parameters.get().zoomLevel());
            double y = PointWebMercator.ofPointCh(point).yAtZoomLevel(parameters.get().zoomLevel());
            polyline.getPoints().addAll(x, y);
        }
        polyline.setLayoutX(-parameters.get().x());
        polyline.setLayoutY(-parameters.get().y());
    }

    /**
     * Méthode permettant de savoir si l'identité donnée correspond déjà à un point de passage existant.
     *
     * @param nodeId : l'identité du noeud dont on veut vérifier la disponibilité
     * @return false s'il y a déjà un point de passage et true sinon
     */
    private boolean isNotAlreadyWaypoint(int nodeId){
        for (Waypoint waypoint : route.getWaypoints()) {
            if(nodeId == waypoint.closestNodeId()) return false ;
        }
        return true ;
    }
}
