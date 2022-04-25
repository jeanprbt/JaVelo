package ch.epfl.javelo.gui;

import ch.epfl.javelo.Preconditions;
import ch.epfl.javelo.projection.PointWebMercator;
import javafx.geometry.Point2D;

/**
 * Enregistrement représentant les paramètres du fond de carte présenté dans l'interface graphique.
 *
 * @author Jean Perbet (341418)
 * @author Cassio Manuguerra (346232)
 */
public record MapViewParameters(int zoomLevel, int x, int y) {

    /**
     * Constructeur public de MapViewParameters vérifiant la validité des arguments.
     *
     * @param x         la coordonnée x du coin haut-gauche de la portion de carte affichée
     * @param y         la coordonnée y du coin haut-gauche de la portion de carte affichée
     * @param zoomLevel le niveau de zoom
     */
    public MapViewParameters {
        Preconditions.checkArgument(zoomLevel >= 0 && x >= 0 && y >= 0);
    }

    /**
     * Méthode qui retourne les coordonnées du coin haut-gauche.
     *
     * @return les coordonnées du coin haut-gauche sous la forme d'un Point2D, format utilisé par JavaFX
     */
    public Point2D topLeft(){
        return new Point2D(x, y);
    }

    /**
     * Méthode qui retourne une instance de MapViewParameters identique au récepteur, si ce n'est que
     * les coordonnées du coin haut-gauche sont celles passées en arguments.
     *
     * @param x la coordonnée x à attribuer au coin haut-gauche
     * @param y la coordonnée y à attribuer au coin haut-gauche
     * @return une instance de MapViewParameters identique au récepteur, si ce n'est que
     * les coordonnées du coin haut-gauche sont celles passées en arguments à la méthode
     */
    public MapViewParameters withMinXY(int x, int y){
        return new MapViewParameters(zoomLevel, x, y);
    }

    /**
     * Méthode qui retourne un PointWebMercator correspondant aux coordonnées passées en arguments
     * exprimées par rapport au coin haut-gauche.
     *
     * @param x la coordonnée x du point en prenant comme 0 le coin haut-gauche
     * @param y la coordonnée y du point en prenant comme 0 le coin haut-gauche
     * @return le PointWebMercator correspondant par rapport au coin haut-gauche
     */
    public PointWebMercator pointAt(int x, int y){
        return PointWebMercator.of(zoomLevel, x + this.x, y + this.y);
    }

    /**
     * Méthode qui retourne la coordonnée x du PointWebMercator exprimée par rapport au coin haut-gauche.
     *
     * @param point le point dont on veut la coordonnée x exprimée par rapport à la vue actuelle
     * @return la coordonnée x du point par rapport au coin haut-gauche
     */
    public double viewX(PointWebMercator point){
        return point.xAtZoomLevel(zoomLevel) - this.x ;
    }

    /**
     * Méthode qui retourne la coordonnée y du PointWebMercator exprimée par rapport au coin haut-gauche.
     *
     * @param point le point dont on veut la coordonnée y
     * @return la coordonnée y du point par rapport au coin haut-gauche
     */
    public double viewY(PointWebMercator point) {
        return point.yAtZoomLevel(zoomLevel) - this.y ;
    }

}
