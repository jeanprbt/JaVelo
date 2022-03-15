package ch.epfl.javelo.routing;

import ch.epfl.javelo.projection.PointCh;

import java.util.List;

/**
 * Interface représentant un itinéraire.
 *
 * @author Jean Perbet (341418)
 * @author Cassio Manuguerra (346232)
 */
public interface Route {

    /**
     * Fonction qui retourne l'index du segment à la position donnée (en mètres).
     *
     * @param position la position donnée
     * @return l'index du segment
     */
    int indexOfSegmentAt(double position);

    /**
     * Fonction qui retourne la longueur de l'itinéraire, en mètres.
     *
     * @return la longueur de l'itinéraire, en mètres
     */
    double length();


    /**
     * Fonction qui retourne la totalité des arêtes de l'itinéraire.
     *
     * @return les arêtes de l'itinéraire
     */
    List<Edge> edges();

    /**
     * Fonction qui retourne la totalité des points situés aux extrémités des arêtes de l'itinéraire.
     *
     * @return la totalité des points situés aux extrémités des arêtes de l'itinéraire
     */
    List<PointCh> points();

    /**
     * Fonction qui retourne le point se trouvant à la position
     * donnée le long de l'itinéraire.
     *
     * @param position la position donnée
     * @return le point se trouvant à la position donnée le long de l'itinéraire
     */
    PointCh pointAt(double position);

    /**
     * Fonction qui retourne l'altitude à la position donnée le long de l'itinéraire.
     * @param position la position le long de l'itinéraire
     * @return l'altitude à la position donnée le long de l'itinéraire
     */
    double elevationAt(double position);

    /**
     * Fonction qui retourne l'identité du nœud appartenant à l'itinéraire
     * et se trouvant le plus proche de la position donnée.
     *
     * @param position la position donnée
     * @return l'identité du nœud appartenant à l'itinéraire
     * et se trouvant le plus proche de la position donnée
     */
    int nodeClosestTo(double position);

    /**
     * Fonction qui retourne le point de l'itinéraire se trouvant le
     * plus proche du point de référence donné.
     *
     * @param point le point de référence donné
     * @return le point de l'itinéraire se trouvant le plus proche du point de référence donné.
     *
     */
    RoutePoint pointClosestTo(PointCh point);

}
