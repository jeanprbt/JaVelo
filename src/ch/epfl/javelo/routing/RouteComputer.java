package ch.epfl.javelo.routing;

import ch.epfl.javelo.Preconditions;
import ch.epfl.javelo.data.Graph;

import java.util.ArrayList;
import java.util.Arrays;
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
    RouteComputer(Graph graph, CostFunction costFunction) {
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

        record WeightedNode(int nodeId, float distance, int previousNodeId) implements Comparable<WeightedNode> {

            @Override
            public int compareTo(WeightedNode that) {
                return Float.compare(this.distance, that.distance);
            }

            /**
             * Constructeur de copie pour un WeightedNode changeant uniquement la distance
             * @param distance la nouvelle distance
             * @return un nouveau WeightNode identique sauf pour la distance
             */
            public WeightedNode copy(float distance){
                return new WeightedNode(nodeId(), distance, previousNodeId());
            }
        }

        PriorityQueue<WeightedNode> toExplore = new PriorityQueue<>();
        List<Edge> edges = new ArrayList<>();
        List<WeightedNode> allNodes = new ArrayList<>();
        for (int i = 0; i < graph.nodeCount(); i++) allNodes.add(new WeightedNode(i, Float.POSITIVE_INFINITY, -1));

        toExplore.add(new WeightedNode(startNodeId, 0, 0));

        while(!toExplore.isEmpty()){
            WeightedNode node = toExplore.remove();

            if(node.distance == Float.NEGATIVE_INFINITY) continue ;

            if(node.nodeId == endNodeId){
                while(node.nodeId != startNodeId){
                    float minDistance = Float.POSITIVE_INFINITY;
                    int edgeIndex = -1 ;
                    for (int i = 0; i < graph.nodeOutDegree(node.nodeId); i++) {
                        int edgeId = graph.nodeOutEdgeId(node.nodeId, i);
                        float edgeDistance = (float) costFunction.costFactor(node.nodeId, edgeId) * (node.distance + (float) graph.edgeLength(edgeId));
                        if(edgeDistance < minDistance){
                            minDistance = edgeDistance;
                            edgeIndex = i ;
                        }
                    }
                    edges.add(Edge.of(graph, graph.nodeOutEdgeId(node.nodeId, edgeIndex) ,node.nodeId, node.previousNodeId));
                    node = allNodes.get(node.previousNodeId);
                }
                return new SingleRoute(edges);
            }

            for (int i = 0; i < graph.nodeOutDegree(node.nodeId); i++) {
                int edgeId = graph.nodeOutEdgeId(node.nodeId, i), nodePrimeId = graph.edgeTargetNodeId(edgeId) ;
                float nodePrimeDistance = (float) costFunction.costFactor(node.nodeId, edgeId) * (node.distance + (float) graph.edgeLength(edgeId)) ;
                if(nodePrimeDistance < allNodes.get(nodePrimeId).distance){
                    allNodes.set(nodePrimeId, new WeightedNode(nodePrimeId, nodePrimeDistance, node.nodeId)) ;
                    toExplore.add(new WeightedNode(nodePrimeId, nodePrimeDistance, node.nodeId)) ;
                }
            }
            allNodes.set(node.nodeId, node.copy(Float.NEGATIVE_INFINITY)) ;
        }

        return null;
    }
}
