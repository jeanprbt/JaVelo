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
        final int MAX_DISTANCE_CURSOR = 15 ;

        mousePositionOnRoute = new SimpleDoubleProperty();
        currentCursorPosition = new SimpleObjectProperty<>();

        //Création des paramètres de fond de carte initiaux
        MapViewParameters mapViewParameters = new MapViewParameters(INITIAL_ZOOM_LEVEL, INITIAL_X, INITIAL_Y);
        parameters = new SimpleObjectProperty<>(mapViewParameters);

        //Création des gestionnaires graphiques permettant d'avoir un panneau final contenant tous les sous-panneaux
        WaypointsManager waypointsManager = new WaypointsManager(graph, parameters, route.getWaypoints(), consumer);
        BaseMapManager baseMapManager = new BaseMapManager(tileManager, waypointsManager,parameters);
        RouteManager routeManager  = new RouteManager(route, parameters);
        mainPane = new StackPane(baseMapManager.pane(),
                                 routeManager.pane(),
                                 waypointsManager.pane());

        /* Ajout des gestionnaires d'évènement sur le panneau pour mettre à jour la position du curseur et celle de la souris
        sur l'itinéraire en conséquence */
        mainPane.setOnMouseMoved(event -> currentCursorPosition.set(new Point2D(event.getX(), event.getY())));
        mainPane.setOnMouseExited(event -> currentCursorPosition.set(null));


        /* Création du lien entre la position de souris sur le profil et :
                - la position du curseur
                - les paramètres de fond de carte
                - l'itinéraire */
        mousePositionOnRoute.bind(Bindings.createDoubleBinding(
                () -> {
                    /* Si l'itinéraire est nul ou la souris n'est pas dans le panneau,
                     la position de la souris sur l'itinéraire est nulle (Double.NaN) */
                    if (route.getRoute() == null || currentCursorPosition.get() == null) return Double.NaN;

                    //Calcul du pointCh correspondant à la position de la souris
                    PointCh cursor = parameters.get().pointAt((int) currentCursorPosition.get().getX(),
                                                              (int) currentCursorPosition.get().getY()).toPointCh();

                    //Calcul du point de l'itinéraire le plus proche de la position de la souris et de son équivalent en Web Mercator
                    RoutePoint closestRoutePoint =  route.getRoute().pointClosestTo(cursor);
                    PointWebMercator closestPointMercator = PointWebMercator.ofPointCh(closestRoutePoint.point());

                    //Conversion du point le plus proche en termes de pixels dans l'écran
                    Point2D closestPoint2D = new Point2D(parameters.get().viewX(closestPointMercator),
                                                         parameters.get().viewY(closestPointMercator));

                    //Comparaison de la distance entre le curseur et l'itinéraire
                    if(currentCursorPosition.get().distance(closestPoint2D) <= MAX_DISTANCE_CURSOR)
                        return closestRoutePoint.position();
                    return Double.NaN;

                }, currentCursorPosition, parameters, route.routeProperty()));

        mainPane.getStylesheets().add("map.css");
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
}
