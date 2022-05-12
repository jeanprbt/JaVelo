package ch.epfl.javelo.gui;

import ch.epfl.javelo.data.Graph;
import ch.epfl.javelo.projection.PointCh;
import ch.epfl.javelo.projection.PointWebMercator;
import ch.epfl.javelo.routing.RoutePoint;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.geometry.Point2D;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

import java.util.function.Consumer;

/**
 * Classe gérant l'affichage de la carte "annotée", c'est-à-dire le fond de carte au-dessus duquel sont
 * superposés l'itinéraire et les points de passage.
 *
 * @author Jean Perbet (341418)
 * @author Cassio Manuguerra (346232)
 */
public final class AnnotatedMapManager {

    private final StackPane mainPane ;

    private final DoubleProperty mousePositionOnRoute ;
    private final ObjectProperty<Point2D> currentCursorPosition ;
    private final ObjectProperty<MapViewParameters> parameters ;

    public AnnotatedMapManager(Graph graph, TileManager tileManager, RouteBean route, Consumer<String> consumer){

        final int INITIAL_ZOOM_LEVEL = 12 ;
        final int INITIAL_X = 543200 ;
        final int INITIAL_Y = 370650 ;
        final double EARTH_CIRCUMFERENCE_EQUATOR = 40_075_016.686 ;
        final int BASE_ZOOM = 8 ;


        MapViewParameters mapViewParameters =
                new MapViewParameters(INITIAL_ZOOM_LEVEL, INITIAL_X, INITIAL_Y);
        ObjectProperty<MapViewParameters> mapViewParametersProperty =
                new SimpleObjectProperty<>(mapViewParameters);

        WaypointsManager waypointsManager = new WaypointsManager(graph, mapViewParametersProperty, route.getWaypoints(), consumer);
        BaseMapManager baseMapManager = new BaseMapManager(tileManager, waypointsManager, mapViewParametersProperty);
        RouteManager routeManager  = new RouteManager(route, mapViewParametersProperty);

        mainPane = new StackPane(baseMapManager.pane(),
                             routeManager.pane(),
                             waypointsManager.pane());

        mousePositionOnRoute = new SimpleDoubleProperty();
        currentCursorPosition = new SimpleObjectProperty<>();
        parameters = mapViewParametersProperty ;

        mainPane.getStylesheets().add("map.css");

        currentCursorPosition.addListener((o, oldS, newS) -> {
            if(route.getRoute() != null) {
                PointCh cursor = parameters.get().pointAt((int) newS.getX(),
                        (int) newS.getY()).toPointCh();

                RoutePoint closestRoutePoint = route.getRoute().pointClosestTo(cursor);

                double scaleFactor = EARTH_CIRCUMFERENCE_EQUATOR *
                        Math.cos(closestRoutePoint.point().lat()) /
                        Math.scalb(1, BASE_ZOOM + parameters.get().zoomLevel());

                if (closestRoutePoint.distanceToReference() / scaleFactor < 15)
                    mousePositionOnRoute.set(closestRoutePoint.position());
            }
        });


        mainPane.setOnMouseMoved(event -> {
            currentCursorPosition.set(new Point2D(event.getX(), event.getY()));
        });

        mainPane.setOnMouseExited(event -> mousePositionOnRoute.set(Double.NaN));

    }

    /**
     * Méthode retournant le panneau JavaFX affichant le fond de carte.
     *
     * @return le panneau affichant le fond de carte annoté
     */
    public Pane pane() {
        return mainPane;
    }

    /**
     * Méthode retournant une propriété en lecture seule contenant la position de la souris sur l'itinéraire.
     *
     * @return la propriété en lecture seule contenant la position de la souris sur l'itinéraire, en mètres.
     */
    public ReadOnlyDoubleProperty mousePositionOnRouteProperty(){
        return mousePositionOnRoute ;
    }

    //---------------------------------------------- Private ----------------------------------------------//

}
