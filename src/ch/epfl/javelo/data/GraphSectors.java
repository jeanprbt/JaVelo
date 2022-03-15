package ch.epfl.javelo.data;

import ch.epfl.javelo.Math2;
import ch.epfl.javelo.projection.PointCh;
import ch.epfl.javelo.projection.SwissBounds;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Enregistrement représentant le tableau des 16384 secteurs du graphe JaVelo
 * sous la forme de son seul attribut : la mémoire tampon buffer.
 *
 * @author Jean Perbet (341418)
 * @author Cassio Manuguerra (346232)
 */
public record GraphSectors(ByteBuffer buffer) {

    private static final int OFFSET_NODE_ID = 0;
    private static final int OFFSET_NODE_NUMBER = OFFSET_NODE_ID + Integer.BYTES ;
    private static final int SECTOR_INTS =  OFFSET_NODE_NUMBER + Short.BYTES ;
    private static final double SECTOR_WIDTH = SwissBounds.WIDTH / 128.0 ;
    private static final double SECTOR_HEIGHT = SwissBounds.HEIGHT / 128.0 ;

    /**
     * Fonction qui retourne la liste de tous les secteurs ayant une intersection avec
     * le carré centré au point donné et de côté égal au double de la distance donnée.
     *
     * @param center le PointCh sur lequel le carré est centré
     * @param distance la distance entre le centre du carré et le milieu de chaque côté
     * @return la liste de tous les secteurs qui intersectent le carré de centre donné et de côté
     * égal au double de la distance donnée
     */
    public List<Sector> sectorsInArea(PointCh center, double distance){
        List<Sector> sectorsList = new ArrayList<>();
        //Détermination des coordonnées des secteurs correspondants aux bords du carré.
        int xMin = (int)(Math2.clamp(0, (center.e() - distance - SwissBounds.MIN_E) / SECTOR_WIDTH, 127));
        int xMax = (int)(Math2.clamp(0, (center.e() + distance - SwissBounds.MIN_E)  / SECTOR_WIDTH, 127));
        int yMin = (int)(Math2.clamp(0, (center.n() - distance - SwissBounds.MIN_N) / SECTOR_HEIGHT,127));
        int yMax = (int)(Math2.clamp(0,(center.n() + distance - SwissBounds.MIN_N) / SECTOR_HEIGHT, 127));

        //Récupération de tous les secteurs dont les coordonnées sont comprises entre les coordonnées calculées précédemment.
        for (int i = yMin; i <= yMax ; i++) {
            for (int j = xMin; j <= xMax ; j++) {
                int sectorIndex = 128 * i + j;
                int firstNode = buffer.getInt(sectorIndex * SECTOR_INTS + OFFSET_NODE_ID);
                int endNode = Short.toUnsignedInt(buffer.getShort(sectorIndex * SECTOR_INTS + OFFSET_NODE_NUMBER)) + firstNode ;
                sectorsList.add(new Sector(firstNode, endNode));
            }
        }

        return sectorsList ;
    }

    /**
     * Enregistrement imbriqué permettant une représentation plus agréable à utiliser,
     * mais moins compacte, des secteurs.
     */
    public record Sector(int startNodeId, int endNodeId){

    }
}
