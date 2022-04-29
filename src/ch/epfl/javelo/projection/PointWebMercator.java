package ch.epfl.javelo.projection;

import ch.epfl.javelo.Preconditions;


/**
 * Enregistrement représentant un point dans le système de coordonnées Web Mercator.
 *
 * @author Jean Perbet (341418)
 * @author Cassio Manuguerra (346232)
 */
public record PointWebMercator(double x, double y) {

    private static final int BASE_ZOOM = 8 ;

    /**
     * Constructeur compact de l'enregistrement visant à renvoyer une exception pour tout
     * point non compris dans l'intervalle [0 ; 1].
     *
     * @param x la coordonnée x du point
     * @param y la coordonnée y du point
     * @throws IllegalArgumentException si le point n'est pas dans l'intervalle [0 ; 1]
     */
    public PointWebMercator {
        Preconditions.checkArgument(x >= 0 && y >= 0 && x <= 1 && y <= 1);
    }

    /**
     * Fonction qui retourne le point en système Web Mercator dont
     * les coordonnées sont x et y au niveau de zoom zoomLevel.
     *
     * @param zoomLevel le niveau de zoom du point dont on veut les coordonnées Web Mercator
     * @param x         la coordonnée horizontale au niveau zoomLevel
     * @param y         la coordonnée verticale au niveau zoomLevel
     * @return le point en système Web Mercator dont les coordonnées
     * sont x et y au niveau de zoom zoomLevel
     */
    public static PointWebMercator of(int zoomLevel, double x, double y) {
        return new PointWebMercator(Math.scalb(x, -(BASE_ZOOM + zoomLevel)), Math.scalb(y, -(BASE_ZOOM + zoomLevel)));
    }

    /**
     * Fonction qui retourne le point Web Mercator correspondant au point
     * du système de coordonnées suisse donné.
     *
     * @param pointCh le point exprimé en système de coordonnées suisse.
     * @return le point en système Web Mercator dont les coordonnées
     * sont x et y
     */
    public static PointWebMercator ofPointCh(PointCh pointCh) {
        return new PointWebMercator(WebMercator.x(pointCh.lon()), WebMercator.y(pointCh.lat()));
    }

    /**
     * Fonction qui retourne la coordonnée x d'un point au niveau de zoom donné.
     *
     * @param zoomLevel le niveau de zoom compris entre 1 et 20
     * @return la coordonnée x du point au niveau de zoom donné
     */
    public double  xAtZoomLevel(int zoomLevel) {
        return Math.scalb(x, BASE_ZOOM + zoomLevel);
    }

    /**
     * Fonction qui retourne la coordonnée y d'un point au niveau de zoom donné.
     *
     * @param zoomLevel le niveau de zoom compris entre 1 et 20
     * @return la coordonnée y du point au niveau de zoom donné
     */
    public double yAtZoomLevel(int zoomLevel) {
        return Math.scalb(y, BASE_ZOOM + zoomLevel);
    }

    /**
     * Fonction qui retourne la longitude du point en radians.
     *
     * @return la longitude du point, exprimée en radians
     */
    public double lon() {
        return WebMercator.lon(x);
    }

    /**
     * Fonction qui retourne la latitude du point en radians.
     *
     * @return la latitude du point, exprimée en radians
     */
    public double lat() {
        return WebMercator.lat(y);
    }

    /**
     * Fonction qui retourne le point de coordonnées suisses se trouvant à la même position
     * que le récepteur (this) ou null si ce point n'est pas dans les limites de la Suisse
     * définies par SwissBounds.
     *
     * @return le point, exprimé en coordonnées suisses ou null si ce dernier n'est pas dans le territoire suisse
     */
    public PointCh toPointCh() {
        double e = Ch1903.e(lon(), lat()), n = Ch1903.n(lon(), lat());
        return SwissBounds.containsEN(e, n) ? new PointCh(e, n) : null;
    }

}
