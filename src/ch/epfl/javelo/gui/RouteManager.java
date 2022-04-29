package ch.epfl.javelo.gui;

import ch.epfl.javelo.projection.PointCh;
import ch.epfl.javelo.projection.PointWebMercator;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
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
    private final Consumer<String> consumer ;

    public RouteManager(RouteBean route, ReadOnlyObjectProperty<MapViewParameters> parameters, Consumer<String> consumer){
        this.circle = new Circle(5);
        this.polyline = new Polyline();
        this.pane = new Pane(polyline, circle);
        this.route = route ;
        this.parameters = parameters ;
        this.consumer = consumer ;

        circle.setId("highlight");
        polyline.setId("route");
        pane.setPickOnBounds(false);

        route.routeProperty().addListener((observable, oldValue, newValue) -> {
            recreateRoute();
        });

        parameters.addListener(((observable, oldValue, newValue) -> {
            if(newValue.zoomLevel() != oldValue.zoomLevel()) recreateRoute();
            else replaceRoute(oldValue);
        }));

        route.highlightedPositionProperty().addListener(((observable, oldValue, newValue) -> {
            replaceCircle();
        }));
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
            setVisible(false);
        else {
            setVisible(true);
            polyline.getPoints().clear();
            pane.getChildren().remove(polyline);
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
        pane.getChildren().add(polyline);
    }

    private void replaceCircle(){
        pane.getChildren().remove(circle);
        PointCh pointCh = route.getRoute().pointAt(route.highlightedPositionProperty().get());
        circle.setLayoutX(parameters.get().viewX(PointWebMercator.ofPointCh(pointCh)));
        circle.setLayoutY(parameters.get().viewY(PointWebMercator.ofPointCh(pointCh)));
        pane.getChildren().add(circle);
    }


    /**
     * Méthode permettant de rendre visible à la fois le cercle et la polyline.
     *
     * @param b la valeur de visibilité voulue
     */
    private void setVisible(boolean b){
        polyline.setVisible(b);
        circle.setVisible(b);
    }
}
