package ch.epfl.javelo.gui;

import ch.epfl.javelo.Math2;
import ch.epfl.javelo.data.GraphSectors;
import ch.epfl.javelo.projection.SwissBounds;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * Classe gérant l'interaction et l'affichage avec le fond de carte.
 *
 * @author Jean Perbet (341418)
 * @author Cassio Manuguerra (346232)
 */
public final class BaseMapManager {

    private final Pane pane ;
    private boolean redrawNeeded;
    private final int TILE_WIDTH = 256 ;
    private final int TILE_HEIGHT = 256 ;


    public BaseMapManager(TileManager tileManager, ObjectProperty<MapViewParameters> property){
        Pane pane = new Pane();
        Canvas canvas = new Canvas();
        redrawNeeded = false;

        //Redimensionnement automatique du canevas en fonction du panneau qui le contient
        canvas.widthProperty().bind(pane.widthProperty());
        canvas.heightProperty().bind(pane.heightProperty());

        //Récupération du contexte graphique du canevas
        GraphicsContext graphicsContext = canvas.getGraphicsContext2D();
        MapViewParameters parameters = property.get();

        //Initialisation de la liste de tuiles à afficher
        List<TileManager.TileId> tilesToDisplay = new ArrayList<>();

        //Détermination des coordonnées des tuiles.
        int xMin = parameters.x() / TILE_WIDTH ;
        int xMax = xMin + (int) canvas.getWidth() / TILE_WIDTH ;
        int yMin = parameters.y() / TILE_HEIGHT ;
        int yMax = yMin + (int) canvas.getHeight() / TILE_HEIGHT ;

        //Récupération de tous les secteurs dont les coordonnées sont comprises entre les coordonnées calculées précédemment.
        for (int i = yMin; i <= yMax; i++) {
            for (int j = xMin; j <= xMax; j++) {
                tilesToDisplay.add(new TileManager.TileId(parameters.zoomLevel(), j, i));
            }
        }

        for (TileManager.TileId tileId : tilesToDisplay) {
            try {
                graphicsContext.drawImage(tileManager.imageForTileAt(tileId), );
            } catch (IOException e){
                continue;
            }
            System.out.print("");
        }


        canvas.sceneProperty().addListener((p, oldS, newS) -> {
            assert oldS == null;
            newS.addPreLayoutPulseListener(this::redrawIfNeeded);
        });


        this.pane = pane ;

    }

    /**
     * Méthode retournant le panneau JavaFX affichant le fond de carte.
     *
     * @return le panneau affichant le fond de carte
     */
    public Pane pane(){
       return this.pane;
    }

    private void redrawIfNeeded() {
        if (!redrawNeeded) return;
        redrawNeeded = false;

        // … à faire : dessin de la carte
    }

    private void redrawOnNextPulse() {
        redrawNeeded = true;
        Platform.requestNextPulse();
    }
}
