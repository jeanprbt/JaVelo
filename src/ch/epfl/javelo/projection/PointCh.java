package ch.epfl.javelo.projection;

/**
 * Enregistrement représentant un point dans le système de coordonnées suisse.
 * @author Jean Perbet (341418)
 * @author Cassio Manuguerra (346232)
 */
public record PointCh(double e, double n) {

    public PointCh {
        if(!SwissBounds.containsEN(e, n)) throw new IllegalArgumentException();
    }

    /**
     * @param that le PointCh dont on veut le carré de la distance par rapport à this
     * @return le carré de la distance en mètres séparant le récepteur (this) de l'argument that
     */
    public double squaredDistanceTo(PointCh that){
        return Math.pow(that.e() - e, 2) + Math.pow(that.n() - n, 2);
    }


    /**
     * @param that le PointCh dont on veut la distance par rapport à this
     * @return la distance en mètres séparant le récepteur (this) de l'argument that
     */
    public double distanceTo(PointCh that){
        return Math.sqrt(squaredDistanceTo(that));
    }

    /**
     * @return la longitude en radians en coordonnées WGS84 de this
     */
    public double lon(){
        return Ch1903.lon(e, n);
    }

    /**
     * @return la latitude en radians en coordonnées WGS84 de this
     */
    public double lat(){
        return Ch1903.lat(e, n);
    }

}
