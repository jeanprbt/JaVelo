package ch.epfl.javelo.projection;

/**
 * Enregistrement représentant un point dans le système de coordonnées suisse.
 *
 * @author Jean Perbet (341418)
 * @author Cassio Manuguerra (346232)
 */
public record PointCh(double e, double n) {

    /**
     * Constructeur compact de l'enregistrement visant à renvoyer une exception pour tout
     * point non compris dans le territoire suisse.
     *
     * @param e la coordonnée E (est) du point
     * @param n la coordonnée N (nord) du point
     * @throws IllegalArgumentException si le point n'est pas compris dans le territoire suisse
     */
    public PointCh {
        if(!SwissBounds.containsEN(e, n)) throw new IllegalArgumentException();
    }

    /**
     * Fonction qui retourne le carré de la distance (en mètres) séparant le récepteur (this)
     * de l'argument that.
     *
     * @param that le PointCh dont on veut le carré de la distance par rapport à this
     * @return le carré de la distance en mètres séparant this de that
     */
    public double squaredDistanceTo(PointCh that){
        return Math.pow(that.e() - e, 2) + Math.pow(that.n() - n, 2);
    }


    /**
     * Fonction qui retourne la distance (en mètres) séparant le récepteur (this) de
     * l'argument that.
     *
     * @param that le PointCh dont on veut la distance par rapport à this
     * @return la distance en mètres séparant this de that
     */
    public double distanceTo(PointCh that){
        return Math.sqrt(squaredDistanceTo(that));
    }

    /**
     * Fonction qui retourne la longitude en coordonnées WGS84 de this (en radians).
     *
     * @return la longitude en coordonnées WGS84 de this
     */
    public double lon(){
        return Ch1903.lon(e, n);
    }

    /**
     * Fonction qui retourne la latitude en coordonnées WGS84 de this (en radians).
     *
     * @return la latitude en coordonnées WGS84 de this
     */
    public double lat(){
        return Ch1903.lat(e, n);
    }

}
