package ch.epfl.javelo.data;

import ch.epfl.javelo.Functions;
import ch.epfl.javelo.projection.PointCh;

import java.awt.*;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.DoubleUnaryOperator;

/**
 * Classe immuable représentant le graphe JaVelo dans sa totalité, ainsi
 * que les méthodes nécessaires à y accéder.
 *
 * @author Jean Perbet (341418)
 * @author Cassio Manuguerra (346232)
 */
public final class Graph {

    private final GraphNodes nodes;
    private final GraphSectors sectors;
    private final GraphEdges edges;
    private final List<AttributeSet> attributeSets;

    /**
     * Constructeur public d'un graphe Javelo.
     *
     * @param nodes le graphe des noeuds à implémenter dans le graphe JaVelo
     * @param sectors le graphe des secteurs à implémenter dans le graphe JaVelo
     * @param edges le graphe des arêtes à implémenter dans le graphe JaVelo
     * @param attributeSets les ensembles d'attributs correspondant aux entités précedentes
     */
    public Graph(GraphNodes nodes, GraphSectors sectors, GraphEdges edges, List<AttributeSet> attributeSets){
        this.nodes = nodes ;
        this.sectors = sectors ;
        this.edges = edges ;
        this.attributeSets = List.copyOf(attributeSets) ;
    }

    /**
     * Fonction permettant de charger un graphe JaVelo entier à partir d'un répertoire
     * source comportant les sous-répertoires correpondant aux différentes données
     * du graphe (noeuds, secteurs, arêtes, attributs OSM).
     *
     * @param basePath le répertoire source des données
     * @return un graphe JaVelo contenant toutes les données passées en argument
     * @throws IOException en cas d'erreur d'entrée/sortie, par ex. si l'un des fichiers n'existe pas
     */
    public static Graph loadFrom(Path basePath) throws IOException {

        //Récupération des différents chemins d'accès aux sous-répertoires de basePath.
        Path nodesPath = basePath.resolve("nodes.bin");
        Path sectorsPath = basePath.resolve("sectors.bin");
        Path edgesPath = basePath.resolve("edges.bin");
        Path elevationsPath = basePath.resolve("elevations.bin");
        Path profileIdsPath = basePath.resolve("profile_ids.bin");
        Path attributeSetsPath = basePath.resolve("attributes.bin");

        //Chargement des différents buffers nécessaires à la création des différents sous-graphes.
        IntBuffer nodesBuffer = mappedBuffer(nodesPath).asIntBuffer() ;
        ByteBuffer sectorsBuffer = mappedBuffer(sectorsPath);
        ByteBuffer edgesBuffer = mappedBuffer(edgesPath) ;
        IntBuffer profileIdsBuffer = mappedBuffer(profileIdsPath).asIntBuffer() ;
        ShortBuffer elevationsBuffer = mappedBuffer(elevationsPath).asShortBuffer() ;
        LongBuffer attributeSets = mappedBuffer(attributeSetsPath).asLongBuffer() ;

        //Mise dans une liste de tous les AttributeSet du buffer correspondant
        List<AttributeSet> attributeSetsList = new ArrayList<>();
        for (int i = 0; i < attributeSets.capacity(); i++) {
            attributeSetsList.add(new AttributeSet(attributeSets.get(i)));
        }

        //Création du graphe total JaVelo.
        return new Graph(new GraphNodes(nodesBuffer),
                         new GraphSectors(sectorsBuffer),
                         new GraphEdges(edgesBuffer, profileIdsBuffer, elevationsBuffer),
                         attributeSetsList);
    }

    /**
     * Fonction retournant le nombre de nœuds dans le graphe.
     *
     * @return le nombre de nœuds dans le graphe.
     */
    public int nodeCount(){
        return nodes.count();
    }

    /**
     * Fonction retournant la position du nœud d'identité donnée
     * sous la forme d'un PointCh.
     *
     * @param nodeId l'identité du nœud dont on souhaite connaître la position
     * @return un PointCh dont la position correspond à celle du nœud d'identité donnée
     */
    public PointCh nodePoint(int nodeId){
        return new PointCh(nodes.nodeE(nodeId), nodes.nodeN(nodeId));
    }

    /**
     * Fonction qui retourne le nombre d'arêtes sortant du nœud d'identité donnée.
     *
     * @param nodeId l'identité du nœud dans le graphe JaVelo
     * @return le nombre d'arêtes du nœud d'identité nodeId
     */
    public int nodeOutDegree(int nodeId){
        return nodes.outDegree(nodeId);
    }

    /**
     * Fonction qui retourne l'identité de la edgeIndex-ième
     * arête sortant du nœud d'identité nodeId.
     *
     * @param nodeId l'identité du nœud dans le graphe JaVelo
     * @param edgeIndex l'index de la edgeIndex-ième arête sortant du nœud
     * @return l'identité de la edgeIndex-ième arête sortant du nœud d'identité nodeId.
     */
    public int nodeOutEdgeId(int nodeId, int edgeIndex){
        return nodes.edgeId(nodeId, edgeIndex);
    }

    /**
     * Fonction retournant l'identité du nœud se trouvant le plus proche du point donné, à la distance
     * maximale donnée (en mètres), ou -1 si aucun noeud ne correspond à ces critères.
     *
     * @param point le point dont on veut l'identité du nœud le plus proche
     * @param searchDistance la distance maximale de recherche du noeud autour du point
     * @return l'identité du nœud le plus proche du point donné à la distance maximale donnée ou -1
     */
    public int nodeClosestTo(PointCh point, double searchDistance){
        //Initialisation de closestNodeId à -1 pour retourner cette valeur si aucun nœud ne correspond aux critères donnés.
        int closestNodeId = -1;

        //Mise au carré du paramètre searchDistance pour rendre plus efficace le calcul de distance en évitant de calculer des racines carrées.
        double closestDistance = Math.pow(searchDistance, 2);

        //Liste de tous les secteurs étant compris dans le cercle de rayon searchDistance autour du point passé en paramètre.s
        List<GraphSectors.Sector> sectorsInArea = sectors.sectorsInArea(point, searchDistance);

        //Itération sur tous les secteurs proches, et à l'intérieur sur tous les nœuds de chaque secteur proche
        // pour déterminer la distance la plus faible possible et l'identité du nœud correspondant.
        for (GraphSectors.Sector sector : sectorsInArea) {
            for (int i = sector.startNodeId(); i < sector.endNodeId(); i++) {
                PointCh node = new PointCh(nodes.nodeE(i), nodes.nodeN(i));
                double distance = point.squaredDistanceTo(node);
                if(distance < closestDistance) {
                    closestNodeId = i ;
                    closestDistance = distance;
                }
            }
        }
        return closestNodeId;
    }

    /**
     * Fonction qui retourne l'identité du nœud destination de l'arête d'identité donnée.
     *
     * @param edgeId l'identité de l'arête dont on veut le nœud destination
     * @return l'identité du nœud destionation de l'arête d'identité donnée
     */
    public int edgeTargetNodeId(int edgeId){
        return edges.targetNodeId(edgeId);
    }

    /**
     * Fonction qui retourne vrai si et seulement si l'arête d'identité donnée
     * va dans le sens inverse de la voie OSM dont elle provient.
     *
     * @param edgeId l'arête dont on veut connaître le sens
     * @return vrai si elle va dans le sens inverse de la voie dont elle provient et faux autrement
     */
    public boolean edgeIsInverted(int edgeId){
        return edges.isInverted(edgeId);
    }

    /**
     * Fonction qui retourne l'ensemble des attributs OSM attachés à l'arête d'identité donnée.
     *
     * @param edgeId : l'identité de l'arête donnée
     * @return l'ensemble des attributs OSM attachés à l'arête d'identité donnée.
     */
    public AttributeSet edgeAttributes(int edgeId){
        return attributeSets.get(edges.attributesIndex(edgeId));
    }

    /**
     * Fonction qui retourne la longueur, en mètres, de l'arête d'identité donnée.
     *
     * @param edgeId l'arête dont on veut la longueur
     * @return la longueur de l'arête d'identité donnée
     */
    public double edgeLength(int edgeId){
        return edges.length(edgeId);
    }

    /**
     * Fonction qui retourne le dénivelé positif total de l'arête d'identité donnée.
     *
     * @param edgeId l'arête dont on veut le dénivelé positif total
     * @return le dénivelé positif total de l'arête d'identié donnée
     */
    public double edgeElevationGain(int edgeId){
        return edges.elevationGain(edgeId);
    }

    /**
     * Fonction qui retourne le profil en long de l'arête d'identité donnée, sous la forme d'une fonction.
     * Si l'arête ne possède pas de profil, alors cette fonction doit retourner Double.NaN pour n'importe quel argument.
     *
     * @param edgeId l'arête d'identité donnée
     * @return le profil en long de l'arête d'identité donnée, sous la forme d'une fonction;
     */
    public DoubleUnaryOperator edgeProfile(int edgeId){
        if (!edges.hasProfile(edgeId)) return Functions.constant(Double.NaN) ;
        float[] samples = edges.profileSamples(edgeId);
        return Functions.sampled(samples, edges.length(edgeId));
    }


    /**
     * Méthode privée permettant de mapper un fichier et de le retourner en ByteBuffer.
     *
     * @param path le chemin d'accès au fichier
     * @return le ByteBuffer correspondant au fichier "mappé"
     * @throws IOException en cas d'erreur d'entrée/sortie, par ex. si l'un des fichiers n'existe pas
     */
    private static ByteBuffer mappedBuffer(Path path) throws IOException {
        ByteBuffer mappedBuffer ;
        try (FileChannel channel = FileChannel.open(path)) {
            mappedBuffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
        }
        return mappedBuffer ;
    }
}