package ch.epfl.javelo.gui;

import ch.epfl.javelo.Preconditions;
import javafx.scene.image.Image;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Classe représentant un gestionnaire de tuiles OSM, dont le rôle est d'obtenir les tuiles
 * depuis un serveur de tuile et de les stocker dans un cache disque et un cache mémoire.
 *
 * @author Jean Perbet (341418)
 * @author Cassio Manuguerra (346232)
 */
public final class TileManager {

    private final Path diskCachePath ;
    private final String tileServerName ;
    private final Map<TileId, Image> cacheMemory ;

    public TileManager(Path diskCachePath, String tileServerName){
        this.diskCachePath = diskCachePath ;
        this.tileServerName = tileServerName ;
        this.cacheMemory = new LinkedHashMap<>(100, 0.75f, true) {
            private static final int MAX_ENTRIES = 100;
            @Override
            protected boolean removeEldestEntry(Map.Entry eldest) {
                return size() > MAX_ENTRIES;
            }
        };
    }

    /**
     * Méthode permettant de retourner l'image correspondant à la tuile d'identité donnée en paramètre.
     *
     * @param tileId l'identité de la tuile dont on cherche l'image
     * @return l'image correspondant à la tuile tileId
     */
   public Image imageForTileAt(TileId tileId) throws IOException{

        Path tilePath = Path.of(diskCachePath.toString() + "/" + tileId.zoomLevel + "/" + tileId.x + "/" + tileId.y + ".png") ;

        //Si la tuile est déjà dans le cache mémoire
        if(cacheMemory.containsKey(tileId)) return cacheMemory.get(tileId);

        //Si la tuile n'est pas dans le cache mémoire, mais déjà dans le cache disque
        if(Files.exists(tilePath)) return getImageFromDisk(tilePath, tileId);

        //Si la tuile n'est pas dans le cache mémoire ni dans le cache disque, téléchargement de la tuile depuis le serveur
        downloadImageFromServer(tilePath, tileId);

        return getImageFromDisk(tilePath, tileId);
   }

    /**
     * Méthode permettant d'extraire une image du cache disque et de la mettre dans le cache mémoire.
     *
     * @param tilePath le chemin d'accès à la tuile dans le cache disque
     * @param tileId l'identité de la tuile dans le cache mémoire
     * @return l'image de la tuile d'identité tileId
     * @throws IOException en cas d'erreur avec les fichiers
     */
   private Image getImageFromDisk(Path tilePath, TileId tileId) throws IOException{
       try(InputStream is = new FileInputStream(tilePath.toString())){
           cacheMemory.put(tileId, new Image(is));
           return cacheMemory.get(tileId);
       }
   }

    /**
     * Méthode permettant de télécharger une image depuis le serveur et de les mettre dans le cache disque.
     *
     * @param tilePath le chemin d'accès que l'on veut donner à la tuile dans le cache disque
     * @param tileId l'identité de la tuile permettant de déduire son URL pour les requêtes
     * @throws IOException en cas d'erreur avec les fichiers
     */
   private void downloadImageFromServer(Path tilePath, TileId tileId) throws IOException {
       URL u = new URL("https://" + tileServerName + "/" + tileId.zoomLevel + "/" + tileId.x + "/" + tileId.y + ".png");
       URLConnection c = u.openConnection();
       c.setRequestProperty("User-Agent", "JaVelo");
       Files.createDirectories(Path.of(diskCachePath + "/" + tileId.zoomLevel + "/" + tileId.x + "/"));
       Files.createFile(tilePath);
       try(InputStream is = c.getInputStream();
           OutputStream os = new FileOutputStream(tilePath.toString())){
           is.transferTo(os);
       }
   }

    /**
     * Enregistrement représentant l'identité d'une tuile OSM.
     *
     * @param zoomLevel le niveau de zoom de la tuile
     * @param x         l'index X de la tuile
     * @param y         l'index Y de la tuile
     */
    public record TileId(int zoomLevel, int x, int y) {

        /**
         * Constructeur public d'un tileId vérifiant la validité de ses arguments.
         *
         * @param zoomLevel le niveau de zoom de la tuile
         * @param x la coordonnée X de la tuile
         * @param y la coordonnée Y de la tuile
         */
        public TileId {
            Preconditions.checkArgument(isValid(zoomLevel, x, y));
        }

        /**
         * Méthode retournant vrai si et seulement si les arguments donnés constituent une identité de tuile valide.
         *
         * @param zoomLevel le niveau de zoom de la tuile
         * @param x         l'index X de la tuile
         * @param y         l'index Y de la tuile
         * @return vrai si et seulement si les arguments donnés constituent une identité de tuile valide
         */
        public static boolean isValid(int zoomLevel, int x, int y) {
            return zoomLevel >= 0 && x >= 0 && y >= 0 && x < Math.pow(2, zoomLevel) && y < Math.pow(2, zoomLevel);
        }
    }
}
