package ch.epfl.javelo.gui;

import ch.epfl.javelo.Math2;
import ch.epfl.javelo.gui.TileManager.TileId;
import ch.epfl.javelo.projection.PointWebMercator;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;

import java.io.IOException;


/**
 * Classe gérant l'interaction et l'affichage avec le fond de carte.
 *
 * @author Jean Perbet (341418)
 * @author Cassio Manuguerra (346232)
 */
public final class BaseMapManager {

    private final TileManager tileManager ;
    private final ObjectProperty<MapViewParameters> parameters;

    private final Pane pane ;
    private final Canvas canvas ;
    private final WaypointsManager waypointsManager ;
    private Point2D cursorPosition ;
    private boolean redrawNeeded ;

    public BaseMapManager(TileManager tileManager,
                          WaypointsManager waypointsManager,
                          ObjectProperty<MapViewParameters> property){

        this.tileManager = tileManager ;
        this.waypointsManager = waypointsManager;

        canvas = new Canvas() ;
        pane = new Pane(canvas) ;
        redrawNeeded = true ;
        parameters = property ;

        //Redimensionnement automatique du canevas en fonction du panneau qui le contient
        canvas.widthProperty().bind(pane.widthProperty());
        canvas.heightProperty().bind(pane.heightProperty());

        //Régulation de redrawIfNeeded à une fois par battement
        canvas.sceneProperty().addListener((p, oldS, newS) -> {
            assert oldS == null;
            newS.addPreLayoutPulseListener(this::redrawIfNeeded);
        });

         /* Ajout de listeners aux paramètres de fond de carte et à la taille du
        canevas pour mettre à jour la carte en conséquence */
        installListeners();

        //Installation de tous les gestionnaires d'évènement
        installHandlers();
    }

    /**
     * Méthode retournant le panneau JavaFX affichant le fond de carte.
     *
     * @return le panneau affichant le fond de carte
     */
    public Pane pane(){
       return pane;
    }

    //---------------------------------------------- Private ----------------------------------------------//

    /**
     * Méthode appelant le redessin de la carte sur le prochain battement.
     */
    private void redrawOnNextPulse() {
        redrawNeeded = true;
        Platform.requestNextPulse();
    }

    /**
     * Méthode privée rééffectuant le dessin de la carte si et seulement si redrawNeeded est vrai.
     */
    private void redrawIfNeeded() {
        if (!redrawNeeded) return;
        redrawNeeded = false;

        final int TILE_WIDTH = 256;
        final int TILE_HEIGHT = 256;

        //Récupération du contexte graphique du canevas
        GraphicsContext graphicsContext = canvas.getGraphicsContext2D();

        //Détermination des coordonnées des tuiles.
        int xMin = (int) parameters.get().x() / TILE_WIDTH;
        int xMax = (int) Math.ceil(xMin + canvas.getWidth() / (double) TILE_WIDTH);
        int yMin = (int) parameters.get().y() / TILE_HEIGHT;
        int yMax = (int) Math.ceil(yMin + canvas.getHeight() / (double) TILE_HEIGHT);

        /* Récupération puis dessin des tuiles, et simplement absence de dessin
        si la récupération de la tuile déclenche une IOException */
        for (int i = yMin; i <= yMax; i++) {
            for (int j = xMin; j <= xMax; j++) {
                TileId tileId = new TileId(parameters.get().zoomLevel(), j, i);
                Image tile;
                try {
                    tile = tileManager.imageForTileAt(tileId);
                } catch (IOException e) {
                    continue;
                }
                graphicsContext.drawImage(tile,
                        j * TILE_WIDTH - parameters.get().x(),
                        i * TILE_HEIGHT - parameters.get().y());
            }
        }
    }


    /**
     * Méthode privée permettant d'installer des listeners sur les paramètres de fond de carte et du canvas
     * pour déclencher un redessin au prochain battement.
     */
    private void installListeners(){
        parameters.addListener((o, oldS, newS) -> redrawOnNextPulse());
        canvas.widthProperty().addListener((o, oldS, newS) -> redrawOnNextPulse());
        canvas.heightProperty().addListener((o, oldS, newS) -> redrawOnNextPulse());
    }

    /**
     * Méthode privée permettant d'installer les gestionnaires d'évènement sur les différentes actions possibles de la souris
     * pour redimensionner les mapViewParameters en conséquence.
     */
    private void installHandlers() {

        final int MIN_ZOOM_LEVEL = 8 ;
        final int MAX_ZOOM_LEVEL = 19 ;


        //À chaque fois que la souris est scrollée, ajout de +-1 au niveau de zoom actuel
        SimpleLongProperty minScrollTime = new SimpleLongProperty();
        pane.setOnScroll(event -> {
            if (event.getDeltaY() == 0d) return;
            long currentTime = System.currentTimeMillis();
            if (currentTime < minScrollTime.get()) return;
            minScrollTime.set(currentTime + 200);
            int zoomDelta = (int) Math.signum(event.getDeltaY());

            PointWebMercator currentCursorPosition = PointWebMercator.of(parameters.get().zoomLevel(),
                                                                      parameters.get().x() + event.getX(),
                                                                      parameters.get().y() + event.getY());

            int newZoomLevel = Math2.clamp(MIN_ZOOM_LEVEL, parameters.get().zoomLevel() + zoomDelta, MAX_ZOOM_LEVEL);
            double newX = currentCursorPosition.xAtZoomLevel(newZoomLevel) -  event.getX();
            double newY = currentCursorPosition.yAtZoomLevel(newZoomLevel) - event.getY();
            parameters.set(new MapViewParameters(newZoomLevel, newX, newY));
        });

        //À chaque fois que la souris est pressée, enregistrement de la position actuelle du curseur
        pane.setOnMousePressed(event -> cursorPosition = new Point2D(event.getX(), event.getY()));

        //À chaque fois que la souris est décalée, mise à jour des mapViewParameters en fonction
        pane.setOnMouseDragged(event -> {
            Point2D translation = cursorPosition.subtract(event.getX(), event.getY());
            parameters.set(parameters.get().withMinXY((int) (parameters.get().x() + translation.getX()),
                                                      (int) (parameters.get().y() + translation.getY())));
            cursorPosition = new Point2D(event.getX(), event.getY());
        });

        //À chaque fois que la souris est cliquée, création d'un nouveau waypoint
        pane.setOnMouseClicked(event -> {
            if (event.isStillSincePress()) {
                waypointsManager.addWaypoint(event.getX(), event.getY());
            }
        });
    }
}
