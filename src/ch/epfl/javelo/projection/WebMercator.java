package ch.epfl.javelo.projection;

import ch.epfl.javelo.Math2;

/**
 * Classe permettant de convertir des cordonnées WGS 84 en coordonnées Web Mercator et inversement.
 *
 * @author Jean Perbet (341418)
 * @author Cassio Manuguerra (346232)
 */
public final class WebMercator {

    private WebMercator (){}

    /**
     * Fonction qui retourne la coordonnée x de la projection en Web Mercator
     * d'un point se trouvant à la longitude lon, donnée en radians.
     *
     * @param lon la longitude du point à convertir, exprimée en radians
     * @return la coordonnée x du point de longitude lon en Web Mercator
     */
    public static double x(double lon){
        return (1.0 / (2*Math.PI)) * (lon + Math.PI);
    }

    /**
     * Fonction qui retourne la coordonnée y de la projection en Web Mercator
     * d'un point se trouvant à la latitude lat, donnée en radians.
     *
     * @param lat la latitude du point à convertir, exprimée en radians
     * @return la coordonnée y du point de latitude lat en Web Mercator
     */
    public static double y(double lat){
        return (1.0 / (2*Math.PI) * (Math.PI - Math2.asinh(Math.tan(lat))));
    }

    /**
     * Fonction qui retourne la longitude, exprimée en radians, d'un point dont
     * la projection Web Mercator se trouve à la position x donnée.
     *
     * @param x la position horizontale du point à convertir, exprimée en Web Mercator
     * @return la longitude du point de coordonnée x, exprimée en radians
     */
    public static double lon(double x){
        return 2*Math.PI*x - Math.PI ;
    }

    /**
     * Fonction qui retourne la latitude, exprimée en radians, d'un point dont
     * la projection Web Mercator se trouve à la position y donnée.
     *
     * @param y la coordonnée y du point à convertir, exprimée en Web Mercator
     * @return la latitude du point de coordonnée y, exprimée en radians
     */
    public static double lat(double y){
        return Math.atan(Math.sinh(Math.PI - Math.PI*2*y));
    }
}
