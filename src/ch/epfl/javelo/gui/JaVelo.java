package ch.epfl.javelo.gui;

import ch.epfl.javelo.data.Graph;
import ch.epfl.javelo.routing.CityBikeCF;
import ch.epfl.javelo.routing.CostFunction;
import ch.epfl.javelo.routing.GpxGenerator;
import ch.epfl.javelo.routing.RouteComputer;
import javafx.application.Application;
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
        Graph graph = Graph.loadFrom(Path.of("javelo-data"));
        Path cacheBasePath = Path.of("osm-cache");
        String tileServerHost = "tile.openstreetmap.org";

        TileManager tileManager = new TileManager(cacheBasePath, tileServerHost);
        CostFunction costFunction = new CityBikeCF(graph);

        RouteBean routeBean = new RouteBean(new RouteComputer(graph, costFunction));

        ErrorManager errorManager = new ErrorManager();
        AnnotatedMapManager map = new AnnotatedMapManager(graph, tileManager, routeBean, errorManager::displayError);
        ElevationProfileManager profile = new ElevationProfileManager(routeBean.elevationProfileProperty(),
                routeBean.highlightedPositionProperty());



        SplitPane splitPane = new SplitPane(map.pane());
        splitPane.setOrientation(Orientation.VERTICAL);
        SplitPane.setResizableWithParent(profile.pane(), false);


        routeBean.routeProperty().addListener((o, oldS, newS) -> {
            if (newS == null){
                splitPane.getItems().remove(profile.pane());
            }
            if (oldS == null && newS != null) {
                splitPane.getItems().add(profile.pane());
            }
        });


        MenuItem menuItem = new MenuItem("Exporter GPX");
        menuItem.setDisable(routeBean.getRoute() == null);
        menuItem.setOnAction(event -> {
            try {
                GpxGenerator.writeGpx(Path.of("javelo.gpx"), routeBean.getRoute(), routeBean.getElevationProfile());
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });

        Menu menu = new Menu("Fichier");
        menu.getItems().add(menuItem);

        MenuBar menuBar = new MenuBar(menu);
        menuBar.setUseSystemMenuBar(true);

        StackPane mainPane = new StackPane(splitPane, menuBar, errorManager.pane());

        stage.setMinWidth(MINIMUM_WINDOW_WIDTH);
        stage.setMinHeight(MINIMUM_WINDOW_HEIGHT);
        stage.setTitle("Javelo");
        stage.setScene(new Scene(mainPane));
        stage.show();

    }
}
