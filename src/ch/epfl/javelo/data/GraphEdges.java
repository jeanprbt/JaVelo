package ch.epfl.javelo.data;

import ch.epfl.javelo.Bits;
import ch.epfl.javelo.Math2;
import ch.epfl.javelo.Q28_4;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.Arrays;

import static ch.epfl.javelo.Q28_4.ofInt;

/**
 * Enregistrement représentant le tableau de toutes les arêtes du graphe JaVelo
 * sous la forme de sa mémoire tampon edgesBuffer
 *
 * @author Jean Perbet (341418)
 * @author Cassio Manuguerra (346232)
 */
public record GraphEdges(ByteBuffer edgesBuffer, IntBuffer profileIds, ShortBuffer elevations) {

    private static final int OFFSET_EDGE_DIRECTION_AND_ID = 0;
    private static final int OFFSET_LENGTH = OFFSET_EDGE_DIRECTION_AND_ID + Integer.BYTES;
    private static final int OFFSET_ELEVATION = OFFSET_LENGTH + Short.BYTES;
    private static final int OFFSET_ATTRIBUTE_SET_ID = OFFSET_ELEVATION + Short.BYTES;
    private static final int EDGE_INTS = OFFSET_ATTRIBUTE_SET_ID + Short.BYTES;

    private static final int OFFSET_FIRST_SAMPLE_ID = 0 ;
    private static final int FIRST_SAMPLE_ID_LENGTH = 30 ;
    private static final int OFFSET_PROFILE = 30 ;
    private static final int PROFILE_LENGTH = 2 ;

    private static final int TYPE_2_SAMPLES = 2 ;
    private static final int TYPE_3_SAMPLES = 4 ;

    /**
     * Fonction qui retourne vrai si et seulement si l'arête d'identité donnée
     * va dans le sens inverse de la voie OSM dont elle provient.
     *
     * @param edgeId l'arête dont on veut connaître le sens
     * @return vrai si elle va dans le sens inverse de la voie dont elle provient et faux autrement
     */
    public boolean isInverted(int edgeId) {
        return edgesBuffer.getInt(EDGE_INTS * edgeId + OFFSET_EDGE_DIRECTION_AND_ID) < 0;
    }

    /**
     * Fonction qui retourne l'identité du nœud destination de l'arête d'identité donnée.
     *
     * @param edgeId l'arête dont on veut connaître le sens
     * @return l'identité du nœud destination de l'arête donnée
     */
    public int targetNodeId(int edgeId) {
        int nodeId = edgesBuffer.getInt(EDGE_INTS * edgeId + OFFSET_EDGE_DIRECTION_AND_ID);
        return nodeId < 0 ? ~nodeId : nodeId ;
    }

    /**
     * Fonction qui retourne la longueur de l'arête d'identité donnée.
     *
     * @param edgeId l'arête dont on veut connaître la longueur
     * @return la longueur de l'arête d'identité donnée
     */
    public double length(int edgeId) {
        return Q28_4.asDouble(Short.toUnsignedInt(edgesBuffer.getShort(EDGE_INTS * edgeId + OFFSET_LENGTH)));
    }

    /**
     * Fonction qui retourne le dénivelé positif de l'arête d'identité donnée.
     *
     * @param edgeId l'arête dont on veut connaître le dénivelé positif
     * @return le dénivelé positif de l'arête d'identité donnée
     */
    public double elevationGain(int edgeId) {
        return Q28_4.asDouble(Short.toUnsignedInt(edgesBuffer.getShort(EDGE_INTS * edgeId + OFFSET_ELEVATION)));
    }

    /**
     * Fonction qui retourne vrai ssi l'arête d'identité donnée possède un profil.
     *
     * @param edgeId l'arête dont on veut savoir si elle possède un profil
     * @return vrai ssi l'arête d'identité donnée possède un profil et faux sinon
     */
    public boolean hasProfile(int edgeId) {
        return Bits.extractUnsigned(profileIds.get(edgeId), OFFSET_PROFILE, PROFILE_LENGTH) != 0;
    }

    /**
     * Fonction qui retourne le tableau des échantillons du profil de
     * l'arête d'identité donnée, qui est vide si l'arête ne possède pas de profil.
     *
     * @param edgeId l'arête dont on veut savoir si elle possède un profil
     * @return le tableau de tous les échantillons du profil en long de l'arête d'identité donnée
     */
    public float[] profileSamples(int edgeId) {
        //Retourne un tableau vide dans le cas d'une absence de profil.
        if (!hasProfile(edgeId)) return new float[]{};

        //Récupération des différentes données propres à l'arête d'identité edgeId : nb d'échantillons, type de profil et index du premier échantillon.
        float[] profileSamples = new float[1 + Math2.ceilDiv(Short.toUnsignedInt(edgesBuffer.getShort(EDGE_INTS * edgeId + OFFSET_LENGTH)), ofInt(2))];
        int profileType = Bits.extractUnsigned(profileIds.get(edgeId), OFFSET_PROFILE, PROFILE_LENGTH);
        int firstSampleId = Bits.extractUnsigned(profileIds.get(edgeId), OFFSET_FIRST_SAMPLE_ID, FIRST_SAMPLE_ID_LENGTH);
        profileSamples[0] = Q28_4.asFloat(Short.toUnsignedInt(elevations.get(firstSampleId)));


        switch (profileType){
            //Si le profil est de type 1 on retourne seulement les différentes altitudes, données au format 12.4, suivant le premier échantillon de l'arête.
            case 1 :
                for (int i = 1; i < profileSamples.length; i++)
                    profileSamples[i] = Q28_4.asFloat(Short.toUnsignedInt(elevations.get(firstSampleId + i)));
                break;

            /* Si le profil est de type 2 ou 3, on cherche le nombre de différences d'altitudes (donc d'échantillons) empaquetées dans chaque short,
            et on parcourt le nombre de shorts correspondant au nombre d'échantillons. À chaque itération on boucle sur le short étudié pour le diviser
            afin de récupérer la différence d'altitude et d'ajouter un nouvel échantillon au tableau jusqu'à ce que celui-ci soit rempli. */
            case 2 :
            case 3 :
                float currentSample = profileSamples[0];
                int samplesPerShort = profileType == 2 ? TYPE_2_SAMPLES : TYPE_3_SAMPLES, arrayIndex = 1;
                for (int i = 1; i <= Math2.ceilDiv(profileSamples.length - 1, samplesPerShort); i++) {
                    short toExtract = elevations.get(firstSampleId + i);
                    for (int j = samplesPerShort - 1; j >= 0 && arrayIndex < profileSamples.length; j--) {
                        currentSample += Q28_4.asFloat(Bits.extractSigned(toExtract, (Short.SIZE / samplesPerShort) * j, (Short.SIZE / samplesPerShort))); //8 ou 4 selon le profil
                        profileSamples[arrayIndex++] = currentSample;
                    }
                }
                break;

        }
        return isInverted(edgeId) ? invertArray(profileSamples) : profileSamples;
    }

    /**
     * Fonction qui retourne l'identité de l'ensemble d'attributs attaché
     * à l'arête d'identité donnée.
     *
     * @param edgeId l'identité de l'arête donnée
     * @return l'identité de l'ensemble d'attributs attaché à l'arête d'identité donnée.
     */
    public int attributesIndex(int edgeId) {
        return Short.toUnsignedInt(edgesBuffer.getShort(EDGE_INTS * edgeId + OFFSET_ATTRIBUTE_SET_ID));
    }


    /**
     * Fonction privée permettant d'inverser un tableau d'échantillons dans le cas
     * où ce dernier correspond à une voie dans le sens inverse du sens OSM.
     *
     * @param array le tableau que l'on veut inverser
     * @return le tableau inversé
     */
    private float[] invertArray(float[] array) {
        float[] invertedArray = Arrays.copyOf(array, array.length);
        for (int i = 0; i < array.length / 2; i++){
            invertedArray[i] = array[array.length - 1 - i];
            invertedArray[array.length - 1 - i] = array[i];
        }
        return invertedArray;
    }
}
