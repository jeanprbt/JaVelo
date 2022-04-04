package ch.epfl.javelo.routing;

import ch.epfl.javelo.Math2;
import ch.epfl.javelo.Preconditions;

import java.util.Arrays;

/**
 * Classe représentant un calculateur de profil en long pour calculer
 * un tel profil sur un itinéraire donné.
 *
 * @author Jean Perbet (341418)
 * @author Cassio Manuguerra (346232)
 */
public final class ElevationProfileComputer {

    private ElevationProfileComputer(){}

    /**
     * Fonction qui retourne le profil en long de l'itinéraire route, en garantissant
     * que l'espacement entre les échantillons du profil est d'au maximum maxStepLength mètres.
     *
     * @param route l'itinéraire dont on veut le profil en long
     * @param maxStepLength l'espacement maximal entre les échantillons du profil
     * @return le profil en long de l'itinéraire route
     * @throws IllegalArgumentException si l'espacement maxStepLength est négatif ou nul
     */
    public static ElevationProfile elevationProfile(Route route, double maxStepLength){
        Preconditions.checkArgument(maxStepLength > 0);

        //Initialisation d'un tableau contenant le bon nombre d'échantillons
        float[] elevationSamples = new float[(int) Math.ceil(route.length()/maxStepLength) + 1] ;

        //L'espacement entre les échantillons contenus de maxStepLength et de la longueur de l'itinéraire
        double stepLength = route.length()/(elevationSamples.length - 1) ;

        //Remplissage du tableau, en comptant les éventuels NaN
        for (int i = 0; i < elevationSamples.length ; i++)
            elevationSamples[i] = (float) route.elevationAt(i * stepLength);

        //Recherche du nombre d'éléments valides au début et à la fin du tableau
        int numberOfFirstInvalidElements = searchNextValidElement(elevationSamples, false);
        int numberOfLastInvalidElements = searchNextValidElement(elevationSamples, true);

        //Retour d'un profil d'altitude 0 si uniquement des NaN
        if (numberOfFirstInvalidElements == elevationSamples.length) {
            Arrays.fill(elevationSamples, 0, elevationSamples.length, 0);
            return new ElevationProfile(route.length(), elevationSamples);
        }

        //Remplissage des premières et dernières valeurs invalides du tableau
        Arrays.fill(elevationSamples, 0, numberOfFirstInvalidElements, elevationSamples[numberOfFirstInvalidElements]);
        Arrays.fill(elevationSamples, elevationSamples.length - 1 - numberOfLastInvalidElements, elevationSamples.length, elevationSamples[elevationSamples.length - 1 - numberOfLastInvalidElements]);

        //Remplissage des éléments invalides du tableau situés entre des éléments valides
        for (int i = numberOfFirstInvalidElements; i < elevationSamples.length - numberOfLastInvalidElements; i++) {
            if(Float.isNaN(elevationSamples[i])){
                double invalidElements = 1 ;
                for (int j = 1; j < elevationSamples.length - 1; j++) {
                    if(!Float.isNaN(elevationSamples[i + j])) break ;
                    else invalidElements++ ;
                }
                for (int j = 0; j < invalidElements; j++)
                    elevationSamples[i + j] = (float) Math2.interpolate(elevationSamples[i - 1], elevationSamples[(int)(i + invalidElements)], (j+1)/(invalidElements+1));
            }
        }

        return new ElevationProfile(route.length(), elevationSamples);
    }

    /**
     * Méthode privée permettant de calculer l'index du premier élément valide (différent de Float.NaN) dans le tableau
     * array, dans l'ordre inverse si inverted est à true et dans l'ordre du tableau si inverted est à false.
     *
     * @param array le tableau à analyser
     * @param inverted vrai si chercher dans l'ordre inverse et faux si chercher dans l'ordre du tableau
     * @return l'index du prochain élément valide
     */
    private static int searchNextValidElement(float[] array, boolean inverted){
        int numberOfInvalidElements = 0  ;
        if (!inverted) {
            while (numberOfInvalidElements < array.length && Float.isNaN(array[numberOfInvalidElements]))
                numberOfInvalidElements++;
        } else {
            while (numberOfInvalidElements < array.length && Float.isNaN(array[array.length - 1 - numberOfInvalidElements]))
                numberOfInvalidElements++;
        }
        return numberOfInvalidElements;
    }
}
