package ch.epfl.javelo.gui;

import ch.epfl.javelo.Math2;
import ch.epfl.javelo.gui.TileManager.TileId;
import ch.epfl.javelo.projection.PointWebMercator;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;

import java.io.IOException;
import java.util.*;


/**
 * Classe gérant l'interaction et l'affichage avec le fond de carte.
 *
 * @author Jean Perbet (341418)
 * @author Cassio Manuguerra (346232)
 */
public final class BaseMapManager {

    private final Pane pane ;
    private final Canvas canvas ;
    private final TileManager tileManager ;
    private boolean redrawNeeded ;
    private MapViewParameters parameters ;
    private final WaypointsManager waypointsManager ;
    private Point2D cursorPosition ;

    public BaseMapManager(TileManager tileManager, WaypointsManager waypointsManager, ObjectProperty<MapViewParameters> property){

        this.canvas = new Canvas() ;
        this.pane = new Pane(canvas) ;
        this.redrawNeeded = true ;
        this.parameters = property.get() ;
        this.tileManager = tileManager ;
        this.waypointsManager = waypointsManager;


        //Redimensionnement automatique du canevas en fonction du panneau qui le contient
        canvas.widthProperty().bind(pane.widthProperty());
        canvas.heightProperty().bind(pane.heightProperty());

        canvas.sceneProperty().addListener((p, oldS, newS) -> {
            assert oldS == null;
            newS.addPreLayoutPulseListener(this::redrawIfNeeded);
        });

        //Ajout des gestionnaires d'évènement au panneau pour toutes les actions possibles de souris
        pane.setOnScroll(event -> {
            System.out.println("ça scrolle");
            PointWebMercator currentCursorPosition = parameters.pointAt((int)event.getX(), (int)event.getY());
            int newZoomLevel = Math2.clamp(8, (int)event.getDeltaY() + parameters.zoomLevel(), 19) ;
            int newX = (int) (currentCursorPosition.xAtZoomLevel(newZoomLevel) - event.getX() - parameters.topLeft().getX());
            int newY = (int) (currentCursorPosition.yAtZoomLevel(newZoomLevel) - event.getY() - parameters.topLeft().getY());
            parameters = new MapViewParameters(newZoomLevel, newX, newY);
        });

        pane.setOnMousePressed(event -> {
            cursorPosition = new Point2D(event.getX(), event.getY());
        });

        pane.setOnMouseDragged(event -> {
            Point2D translation = cursorPosition.subtract(new Point2D(event.getX(), event.getY()));
            parameters = new MapViewParameters(parameters.zoomLevel(),
                    (int) (parameters.x() + translation.getX()),
                    (int) (parameters.y() + translation.getY()));
            cursorPosition = new Point2D(event.getX(), event.getY());
        });

        pane.setOnMouseClicked(event -> {
            if (event.isStillSincePress())
            this.waypointsManager.addWayPoint(event.getX(), event.getY());
        });

    }

    /**
     * Méthode retournant le panneau JavaFX affichant le fond de carte.
     *
     * @return le panneau affichant le fond de carte
     */
    public Pane pane(){
       return this.pane;
    }

    /**
     * Méthode rééffectuant le dessin si et seulement si redrawNeeded est vrai.
     */
    private void redrawIfNeeded() {
        if (!redrawNeeded) return;
        redrawNeeded = false;

        final int TILE_WIDTH = 256;
        final int TILE_HEIGHT = 256;

        //Récupération du contexte graphique du canevas
        GraphicsContext graphicsContext = canvas.getGraphicsContext2D();

        //Détermination des coordonnées des tuiles.
        int xMin = parameters.x() / TILE_WIDTH;
        int xMax = (int) Math.ceil(xMin + canvas.getWidth() / (double) TILE_WIDTH);
        int yMin = parameters.y() / TILE_HEIGHT;
        int yMax = (int) Math.ceil(yMin + canvas.getHeight() / (double) TILE_HEIGHT);

        //Récupération puis dessin des tuiles, et simplement absence de dessin si la tuile déclenche une IOException
        for (int i = yMin; i <= yMax; i++) {
            for (int j = xMin; j <= xMax; j++) {
                TileId tileId = new TileId(parameters.zoomLevel(), j, i);
                Image tile;
                try {
                    tile = tileManager.imageForTileAt(tileId);
                } catch (IOException e) {
                    continue;
                }
                graphicsContext.drawImage(tile,
                        j * TILE_WIDTH - parameters.x(),
                        i * TILE_HEIGHT - parameters.y());
            }
        }
        redrawOnNextPulse();
    }


    /**
     * Méthode appelant le redessin de la carte sur le prochain pulse.
     */
    private void redrawOnNextPulse() {
        redrawNeeded = true;
        Platform.requestNextPulse();
    }
}
