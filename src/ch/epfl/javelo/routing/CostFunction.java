package ch.epfl.javelo.routing;


/**
 * Interface représentant une fonction de coût.
 *
 * @author Jean Perbet (341418)
 * @author Cassio Manuguerra (346232)
 **/
public interface CostFunction {

    /**
     * Fonction qui retourne le facteur par lequel la longueur de l'arête d'identité edgeId et partant du nœud
     * d'identité nodeId, doit être multipliée. Ce facteur doit impérativement être supérieur ou égal à 1.
     *
     * @param nodeId nœud de départ de l'arête
     * @param edgeId identité de l'arête
     * @return le facteur multiplicatif de l'arête d'identité edgeId et partant du nœud nodeId
     */
    double costFactor(int nodeId, int edgeId);
}
