package ch.epfl.javelo.routing;

import ch.epfl.javelo.projection.PointCh;

/**
 * Enregistrement représentant le point d'un itinéraire le plus proche d'un point
 * de référence donné qui se trouve dans le voisinage de l'itinéraire.
 *
 * @author Jean Perbet (341418)
 * @author Cassio Manuguerra (346232)
 *
 * @param point le point sur l'itinéraire
 * @param position la position du point le long de l'itinéraire, en mètres
 * @param distanceToReference la distance, en mètres, entre le point et la référence
 */
public record RoutePoint(PointCh point, double position, double distanceToReference) {
    /**
     * Constante représentant un point inexistant
     */
    public static final RoutePoint NONE = new RoutePoint(null, Double.NaN, Double.POSITIVE_INFINITY);

    /**
     * Fonction retournant un point identique à this mais dont la position est décalée
     * de la différence donnée, pouvant être positive ou négative.
     *
     * @param positionDifference la différence, positive ou négative, de position le long de l'itinéraire
     * @return le point décalé le long de l'itinéraire de positionDifference
     */
    public RoutePoint withPositionShiftedBy(double positionDifference){
        return new RoutePoint(point, position + positionDifference, distanceToReference);
    }

    /**
     * Fonction qui retourne this si sa distance au point de référence est inférieure
     * ou égale à celle de that, et that autrement.
     *
     * @param that le point auquel on veut comparer la distance au point de référence
     * @return this si sa distance au point de référence est inférieure ou égale à celle de that, et that autrement
     */
    public RoutePoint min(RoutePoint that){
        return distanceToReference <= that.distanceToReference ? this : that;
    }

    /**
     * Fonction qui retourne this si sa distance à la référence est inférieure ou égale à thatDistanceToReference,
     * et une nouvelle instance de RoutePoint dont les attributs sont les arguments passés à min sinon.
     *
     * @param thatPoint la position du point de référence du that, exprimée en système de coordonnées suisses
     * @param thatPosition la position le long de l'itinéraire du that
     * @param thatDistanceToReference la distance au point de référence du that
     * @return this si sa distance au point de référence est inférieure ou égale à thatDistanceToReference et une
     * nouvelle instance de RoutePoint dont les attributs sont les arguments passés à min sinon.
     */
    public RoutePoint min(PointCh thatPoint, double thatPosition, double thatDistanceToReference){
        return distanceToReference <= thatDistanceToReference ? this : new RoutePoint(thatPoint, thatPosition, thatDistanceToReference);
    }
}
