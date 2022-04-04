package ch.epfl.javelo.routing;

import ch.epfl.javelo.Preconditions;
import ch.epfl.javelo.data.Graph;
import ch.epfl.javelo.projection.PointCh;

import java.awt.*;
import java.util.*;
import java.util.List;

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
        record WeightedNode(int nodeId, float distance) implements Comparable<WeightedNode> {
            @Override
            public int compareTo(WeightedNode that) {
                return Double.compare(this.distance, that.distance);
            }
        }

        int[] predecessors = new int[graph.nodeCount()];
        float[] straightDistances = new float[graph.nodeCount()];
        PriorityQueue<WeightedNode> toExplore = new PriorityQueue<>();
        List<Edge> edges = new ArrayList<>();
        PointCh endPoint = graph.nodePoint(endNodeId);

        //Association à tous les nœuds du graphe d'une distance infinie
        List<WeightedNode> allNodes = new ArrayList<>();
        for (int i = 0; i < graph.nodeCount(); i++) allNodes.add(new WeightedNode(i, Float.POSITIVE_INFINITY));

        //Réglage de la distance du nœud de départ et ajout de ce dernier à l'ensemble en_exploration
        allNodes.set(startNodeId, new WeightedNode(startNodeId,0));
        toExplore.add(allNodes.get(startNodeId));

        //Application de l'algorithme de Djikstra
        while (!toExplore.isEmpty()) {

            /* Choix du nœud dont la somme de la distance au nœud de départ et de la distance
            à vol d'oiseau au nœud d'arrivée est minimale et retrait de la liste en_exploration */
            WeightedNode node = toExplore.remove();

            //Ignorance des nœuds que l'on a déjà traités
            if (node.distance == Float.NEGATIVE_INFINITY) continue;

            /* Traitement de la fin de l'algorithme lorsqu'il atteint le nœud d'arrivée : reconstruction de l'itinéraire
            grâce au previousNodeId de chaque nœud parcouru par Djikstra en cherchant à chaque tour de boucle l'arête sortante
            du nœud étudié arrivant au previousNodeId */
            if (node.nodeId == endNodeId) {
                while (node.nodeId != startNodeId) {
                    for (int i = 0; i < graph.nodeOutDegree(node.nodeId); i++) {
                        int edgeId = graph.nodeOutEdgeId(node.nodeId, i);
                        if(graph.edgeTargetNodeId(edgeId) == predecessors[node.nodeId]) {
                            edges.add(Edge.of(graph, edgeId, predecessors[node.nodeId], node.nodeId));
                            node = allNodes.get(predecessors[node.nodeId]);
                        }
                    }
                }
                return new SingleRoute(invertList(edges));
            }

            /* Itération sur l'ensemble des arêtes sortant de node afin de trouver
            l'arête optimale et d'ajouter son nœud d'arrivée à toExplore, et changer
            la somme de la distance  totale parcourue depuis le nœud de départ jusqu'à
            ce nœud avec la distance à vol d'oiseau entre ce nœud et le nœud d'arrivée */
            for (int i = 0; i < graph.nodeOutDegree(node.nodeId); i++) {
                int edgeId = graph.nodeOutEdgeId(node.nodeId, i), nodePrimeId = graph.edgeTargetNodeId(edgeId);
                straightDistances[nodePrimeId] = (float)graph.nodePoint(nodePrimeId).distanceTo(endPoint);
                float d = (float) (node.distance - straightDistances[node.nodeId] + straightDistances[nodePrimeId] + costFunction.costFactor(node.nodeId, edgeId) * graph.edgeLength(edgeId));
                if (d < allNodes.get(nodePrimeId).distance) {
                    allNodes.set(nodePrimeId, new WeightedNode(nodePrimeId, d));
                    predecessors[nodePrimeId] = node.nodeId ;
                    toExplore.add(allNodes.get(nodePrimeId));
                }
            }
            allNodes.set(node.nodeId, new WeightedNode(node.nodeId, Float.NEGATIVE_INFINITY));
        }
        return null;
    }

    /**
     * Fonction privée permettant d'inverser une liste.
     *
     * @param list la liste que l'on veut inverser
     * @return la liste inversée
     */
    private <E> List<E> invertList(List<E> list){
        List<E> invertedList = new ArrayList<>(list) ;
        for (int i = 0; i < list.size(); i++) invertedList.set(i, list.get(list.size()-1-i));
        return invertedList;
    }
}

