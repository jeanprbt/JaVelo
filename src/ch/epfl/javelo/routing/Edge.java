package ch.epfl.javelo.routing;

import ch.epfl.javelo.Math2;
import ch.epfl.javelo.data.Graph;
import ch.epfl.javelo.projection.PointCh;

import java.util.function.DoubleUnaryOperator;

/**
 * Enregistrement représentant une arête d'un itinéraire, uniquement destiné à représenter
 * les arêtes d'un itinéraire et non celles d'un graphe.
 *
 * @param fromNodeId l'identité du nœud de départ de l'arête
 * @param toNodeId   l'identité du nœud d'arrivée de l'arête,
 * @param fromPoint  le point de départ de l'arête
 * @param toPoint    le point d'arrivée de l'arête
 * @param length     la longueur de l'arête, en mètres
 * @param profile    le profil en long de l'arête
 * @author Jean Perbet (341418)
 * @author Cassio Manuguerra (346232)
 */
public record Edge(int fromNodeId, int toNodeId, PointCh fromPoint, PointCh toPoint, double length,
                   DoubleUnaryOperator profile) {

    /**
     * Méthode constructrice retournant une instance de Edge dont toues les attributs
     * non donnés en paramètres sont ceux de l'arête d'identité edgeId dans le graphe JaVelo.
     *
     * @param graph      le graphe donné
     * @param edgeId     l'identité de l'arête donnée
     * @param fromNodeId l'identité du nœud de départ de l'arête
     * @param toNodeId   l'identité du nœud d'arrivée de l'arête,
     * @return une instance de Edge dont les attributs fromNodeId et toNodeId sont ceux donnés,
     * les autres étant ceux de l'arête d'identité edgeId dans le graphe Graph
     */
    public static Edge of(Graph graph, int edgeId, int fromNodeId, int toNodeId) {
        return new Edge(fromNodeId, toNodeId, graph.nodePoint(fromNodeId), graph.nodePoint(toNodeId), graph.edgeLength(edgeId), graph.edgeProfile(edgeId));
    }

    /**
     * Fonction qui retourne la position le long de l'arête, en mètres,
     * qui se trouve le plus proche du point donné.
     *
     * @param point le point dont on veut la position la plus proche le long de l'arête
     * @return la position en mètres le long de l'arête la plus proche du point donné
     */
    public double positionClosestTo(PointCh point) {
        return Math2.projectionLength(fromPoint.e(), fromPoint.n(), toPoint.e(), toPoint.n(), point.e(), point.n());
    }

    /**
     * Fonction qui retourne le point se trouvant à la position donnée sur l'arête, exprimée en mètres.
     *
     * @param position la position du point sur l'arête, exprimée en mètres
     * @return le point se trouvant à la position donnée sur l'arête
     */
    public PointCh pointAt(double position) {
        return new PointCh(Math2.interpolate(fromPoint.e(), toPoint.e(), position / length), Math2.interpolate(fromPoint.n(), toPoint.n(), position / length));
    }

    /**
     * Fonction qui retourne l'altitude, en mètres, à la position donnée sur l'arête.
     *
     * @param position la position du point sur l'arête, exprimée en mètres
     * @return l'altitude, en mètres, à la position donnée sur l'arête
     */
    public double elevationAt(double position) {
        return profile.applyAsDouble(position);
    }
}
