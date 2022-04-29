package ch.epfl.javelo.routing;

import ch.epfl.javelo.Preconditions;
import ch.epfl.javelo.data.Graph;
import ch.epfl.javelo.projection.PointCh;

import java.util.*;

/**
 * Classe représentant un planificateur d'itinéraire.
 *
 * @author Jean Perbet (341418)
 * @author Cassio Manuguerra (346232)
 */
public final class RouteComputer {

    private final Graph graph;
    private final CostFunction costFunction;

    private final float ALREADY_HANDLED = Float.NEGATIVE_INFINITY ;

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
                return Float.compare(this.distance, that.distance);
            }
        }

        int[] predecessors = new int[graph.nodeCount()];
        float[] distances = new float[graph.nodeCount()];

        PriorityQueue<WeightedNode> toExplore = new PriorityQueue<>();

        //Point d'arrivée pour le calcul des distances à vol d'oiseau
        PointCh endPoint = graph.nodePoint(endNodeId);

        //Association à tous les nœuds du graphe d'une distance infinie
        Arrays.fill(distances, Float.POSITIVE_INFINITY);

        //Réglage de la distance du nœud de départ et ajout de ce dernier à l'ensemble en_exploration
        distances[startNodeId] = 0;
        toExplore.add(new WeightedNode(startNodeId, distances[startNodeId]));

        //Application de l'algorithme A*
        while (!toExplore.isEmpty()) {

            /* Choix du nœud dont la somme de la distance au nœud de départ et de la distance
            à vol d'oiseau au nœud d'arrivée est minimale et retrait de la liste en_exploration */
            WeightedNode node = toExplore.remove();

            //Ignorance des nœuds que l'on a déjà traités
            if (node.distance == ALREADY_HANDLED) continue;

            //Traitement de la fin de l'algorithme lorsqu'il atteint le nœud d'arrivée grâce à la méthode buildRoute()
            if (node.nodeId == endNodeId)
                return buildRoute(startNodeId, endNodeId, predecessors);

            /* Itération sur l'ensemble des arêtes sortant de node afin de trouver l'arête optimale pour ajouter son nœud d'arrivée
            à toExplore, ajouter node en tant que prédécesseur de ce nœud d'arrivée et changer la somme de la distance totale
            parcourue depuis le nœud de départ jusqu'à ce nœud avec la distance à vol d'oiseau entre ce nœud et le nœud d'arrivée */
            for (int i = 0; i < graph.nodeOutDegree(node.nodeId); i++) {
                int edgeId = graph.nodeOutEdgeId(node.nodeId, i), nodePrimeId = graph.edgeTargetNodeId(edgeId);
                float d = (float) (node.distance - graph.nodePoint(node.nodeId).distanceTo(endPoint)
                        + graph.nodePoint(nodePrimeId).distanceTo(endPoint)
                        + costFunction.costFactor(node.nodeId, edgeId) * graph.edgeLength(edgeId));
                if (d < distances[nodePrimeId]) {
                    distances[nodePrimeId] = d;
                    predecessors[nodePrimeId] = node.nodeId;
                    toExplore.add(new WeightedNode(nodePrimeId, distances[nodePrimeId]));
                }
            }

            //Marquage des nœuds une fois traités
            distances[node.nodeId] = ALREADY_HANDLED;
        }
        return null;
    }

    /**
     * Méthode privée permettant de reconstruire un itinéraire entre les nœuds de départ et d'arrivée,
     * grâce à un tableau donnant pour chaque nœud de l'itinéraire son prédécesseur préalablement calculé.
     *
     * @param startNodeId  le nœud de départ
     * @param endNodeId    le nœud d'arrivée
     * @param predecessors le tableau contenant à l'index i le prédécesseur du nœud d'identité i dans l'itinéraire
     * @return l'itinéraire entre startNodeId et endNodeId grâce au tableau predecessors
     */
    private SingleRoute buildRoute(int startNodeId, int endNodeId, int[] predecessors) {
        List<Edge> edges = new ArrayList<>();
        int nodeId = endNodeId;
        while (nodeId != startNodeId) {
            //À chaque tour de boucle, recherche de l'arête sortante du nœud étudié arrivant à son prédécesseur
            for (int i = 0; i < graph.nodeOutDegree(nodeId); i++) {
                int edgeId = graph.nodeOutEdgeId(nodeId, i);
                if (graph.edgeTargetNodeId(edgeId) == predecessors[nodeId]) {
                    edges.add(Edge.of(graph, edgeId, predecessors[nodeId], nodeId));
                    nodeId = predecessors[nodeId];
                }
            }
        }
        //Inversion des arêtes, car l'itinéraire a été construit en partant de la fin
        Collections.reverse(edges);
        return new SingleRoute(edges);
    }
}

