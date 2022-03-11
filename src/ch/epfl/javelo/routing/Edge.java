package ch.epfl.javelo.routing;

import ch.epfl.javelo.data.Graph;
import ch.epfl.javelo.projection.PointCh;

import java.util.function.DoubleUnaryOperator;

/**
 * Enregistrement représentant une arête d'un itinéraire, uniquement destiné à représenter
 * les arêtes d'un itinéraire et non celles d'un graphe.
 *
 * @author Jean Perbet (341418)
 * @author Cassio Manuguerra (346232)
 *
 * @param fromNodeId l'identité du nœud de départ de l'arête
 * @param toNodeId l'identité du nœud d'arrivée de l'arête,
 * @param fromPoint le point de départ de l'arête
 * @param toPoint le point d'arrivée de l'arête
 * @param length la longueur de l'arête, en mètres
 * @param profile le profil en long de l'arête
 *
 */
public record Edge(int fromNodeId, int toNodeId, PointCh fromPoint, PointCh toPoint, double length, DoubleUnaryOperator profile) {

    /**
     * Méthode constructrice retournant une instance de Edge dont toues les attributs
     * non donnés en paramètres sont ceux de l'arête d'identité edgeId dans le graphe JaVelo.
     *
     * @param graph le graphe donné
     * @param edgeId l'identité de l'arête donnée
     * @param fromNodeId l'identité du nœud de départ de l'arête
     * @param toNodeId l'identité du nœud d'arrivée de l'arête,
     * @return une instance de Edge dont les attributs fromNodeId et toNodeId sont ceux donnés,
     * les autres étant ceux de l'arête d'identité edgeId dans le graphe Graph.
     */
    public static Edge of(Graph graph, int edgeId, int fromNodeId, int toNodeId){
        return null ;
    }
}
