package ch.epfl.javelo.routing;

import ch.epfl.javelo.Preconditions;
import ch.epfl.javelo.data.Graph;
import ch.epfl.javelo.projection.PointCh;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Classe représentant un planificateur d'itinéraire.
 *
 * @author Jean Perbet (341418)
 * @author Cassio Manuguerra (346232)
 */
public final class RouteComputer {

    private final Graph graph;
    private final CostFunction costFunction;

    /**
     * Constructeur public d'un planificateur d'itinéraire.
     */
    public RouteComputer(Graph graph, CostFunction costFunction) {
        this.graph = graph;
        this.costFunction = costFunction;
    }

    /**
     * Fonction qui retourne l'itinéraire de coût total minimal allant du nœud d'identité startNodeId au nœud
     * d'identité endNodeId dans le graphe passé au constructeur, ou null si aucun itinéraire n'existe.
     *
     * @param startNodeId l'identité du nœud de départ
     * @param endNodeId   l'identité du nœud d'arrivée
     * @return l'itinéraire optimal entre le nœud de départ et le nœud d'arrivée
     * @throws IllegalArgumentException si le nœud de départ et d'arrivée sont identiques
     */
    public Route bestRouteBetween(int startNodeId, int endNodeId) {
        Preconditions.checkArgument(startNodeId != endNodeId);

        //Enregistrement permettant d'associer à chaque nœud du graphe sa distance au nœud de départ
        record WeightedNode(int nodeId, float distance, float straightDistance, int previousNodeId) implements Comparable<WeightedNode> {
            @Override
            public int compareTo(WeightedNode that) {
                return Double.compare(this.distance + this.straightDistance, that.distance + that.straightDistance);
            }
        }

        PriorityQueue<WeightedNode> toExplore = new PriorityQueue<>();
        List<Edge> edges = new ArrayList<>();
        PointCh endPoint = graph.nodePoint(endNodeId);

        //Association à tous les nœuds du graphe d'une distance infinie
        List<WeightedNode> allNodes = new ArrayList<>();
        for (int i = 0; i < graph.nodeCount(); i++) allNodes.add(new WeightedNode(i, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, -1));

        //Réglage de la distance du nœud de départ et ajout de ce dernier à l'ensemble en_exploration
        allNodes.set(startNodeId, new WeightedNode(startNodeId,0, (float)graph.nodePoint(endNodeId).distanceTo(endPoint), startNodeId));
        toExplore.add(allNodes.get(startNodeId));

        //Application de l'algorithme de Djikstra
        while (!toExplore.isEmpty()) {

            //Choix du nœud dont la distance au nœud de départ est minimale et retrait de la liste en_exploration
            WeightedNode node = toExplore.remove();

            //Ignorance des nœuds que l'on a déjà traités
            if (node.distance == Float.NEGATIVE_INFINITY) continue;

            /* Traitement de la fin de l'algorithme lorsqu'il atteint le nœud d'arrivée : reconstruction de l'itinéraire
            //grâce au previousNodeId de chaque nœud parcouru par Djikstra en cherchant à chaque tour de boucle l'arête sortante
            du nœud étudié arrivant au previousNodeId */
            if (node.nodeId == endNodeId) {
                while (node.nodeId != startNodeId) {
                    float minDistance = Float.POSITIVE_INFINITY, edgeIndex = 0 ;
                    for (int i = 0; i < graph.nodeOutDegree(node.nodeId); i++) {
                        int edgeId = graph.nodeOutEdgeId(node.nodeId, i);
                        float edgeDistance = allNodes.get(graph.edgeTargetNodeId(edgeId)).distance + (float) graph.edgeLength(edgeId);
                        if (edgeDistance < minDistance) {
                            minDistance = edgeDistance;
                            edgeIndex = i;
                        }
                    }
                    edges.add(Edge.of(graph, graph.nodeOutEdgeId(node.nodeId, (int)edgeIndex), node.nodeId, node.previousNodeId));
                    node = allNodes.get(node.previousNodeId);
                }
                return new SingleRoute(edges);
            }

            /* Itération sur l'ensemble des arêtes sortant de node afin de trouver l'arête
            optimale et d'ajouter son nœud d'arrivée à toExplore, et changer la distance
            totale parcourue depuis le nœud de départ jusqu'à ce nœud */
            for (int i = 0; i < graph.nodeOutDegree(node.nodeId); i++) {
                int edgeId = graph.nodeOutEdgeId(node.nodeId, i), nodePrimeId = graph.edgeTargetNodeId(edgeId);
                float d = (float) (node.distance + costFunction.costFactor(node.nodeId, edgeId) * graph.edgeLength(edgeId));
                if (d < allNodes.get(nodePrimeId).distance) {
                    allNodes.set(nodePrimeId, new WeightedNode(nodePrimeId, d, (float)graph.nodePoint(nodePrimeId).distanceTo(endPoint), node.nodeId));
                    toExplore.add(allNodes.get(nodePrimeId));
                }
            }
            allNodes.set(node.nodeId, new WeightedNode(node.nodeId, Float.NEGATIVE_INFINITY, node.previousNodeId));
        }
        return null;
    }
}



