package ch.epfl.javelo.gui;

import ch.epfl.javelo.data.Graph;
import ch.epfl.javelo.routing.CityBikeCF;
import ch.epfl.javelo.routing.CostFunction;
import ch.epfl.javelo.routing.GpxGenerator;
import ch.epfl.javelo.routing.RouteComputer;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;

public final class JaVelo extends Application {

    private static final int MINIMUM_WINDOW_WIDTH = 800;
    private static final int MINIMUM_WINDOW_HEIGHT = 600;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {

        //Création des éléments indispensables à la création des différents gestionnaires graphiques
        Path cacheBasePath = Path.of("osm-cache");
        String tileServerHost = "tile.openstreetmap.org";
        TileManager tileManager = new TileManager(cacheBasePath, tileServerHost);

        Graph graph = Graph.loadFrom(Path.of("javelo-data"));
        CostFunction costFunction = new CityBikeCF(graph);
        RouteBean routeBean = new RouteBean(new RouteComputer(graph, costFunction));

        //Création des gestionnaires graphiques
        ErrorManager errorManager = new ErrorManager();
        AnnotatedMapManager map = new AnnotatedMapManager(graph, tileManager, routeBean, errorManager::displayError);
        ElevationProfileManager profile = new ElevationProfileManager(routeBean.elevationProfileProperty(),
                                                                      routeBean.highlightedPositionProperty());

        /* Lien entre la position mise en évidence et :
                - la position de la souris sur l'itinéraire si celle-ci est >= 0
                - la position de la souris sur le profil sinon  */
        routeBean.highlightedPositionProperty().bind(Bindings.when(map.mousePositionOnRouteProperty().greaterThanOrEqualTo(0))
                                                             .then(map.mousePositionOnRouteProperty())
                                                             .otherwise(profile.mousePositionOnProfileProperty()));

        //Création du splitPane qui contiendra la carte annotée et le profil
        SplitPane splitPane = new SplitPane(map.pane());
        splitPane.setOrientation(Orientation.VERTICAL);
        SplitPane.setResizableWithParent(profile.pane(), false);

        //Création de la barre de menus permettant d'exporter l'itinéraire au format GPX
        MenuItem menuItem = new MenuItem("Exporter GPX");
        Menu menu = new Menu("Fichier");
        menu.getItems().add(menuItem);
        MenuBar menuBar = new MenuBar(menu);
        menuBar.setUseSystemMenuBar(true);

        //Paramétrage de l'action à effectuer lors du clic sur le sous-menu
        menuItem.setOnAction(event -> {
            try {
                GpxGenerator.writeGpx(Path.of("javelo.gpx"), routeBean.getRoute(), routeBean.getElevationProfile());
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });

        //Autoriser ou non le sous-menu export GPX en fonction de son existence
        menuItem.disableProperty().bind(Bindings.isNull(routeBean.routeProperty()));

        //Listener sur l'itinéraire pour ajouter ou retirer le profil en long du panneau principal en fonction de son existence
        routeBean.routeProperty().addListener((o, oldS, newS) -> {
            if (newS == null)
                splitPane.getItems().remove(profile.pane());
            if (oldS == null && newS != null)
                splitPane.getItems().add(profile.pane());
        });

        /* Création du panneau principal de la scène contenant le splitPane avec la carte annotée et le profil,
        le gestionnaire graphique d'erreurs et la barre de menus  */
        StackPane mainPane = new StackPane(splitPane, errorManager.pane(), menuBar);

        //Lancement de la scène
        stage.setMinWidth(MINIMUM_WINDOW_WIDTH);
        stage.setMinHeight(MINIMUM_WINDOW_HEIGHT);
        stage.setTitle("Javelo");
        stage.setScene(new Scene(mainPane));
        stage.show();
    }
}
