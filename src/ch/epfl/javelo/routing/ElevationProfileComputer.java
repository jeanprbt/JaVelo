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
        float[] elevationSamples = new float[(int) Math.ceil(route.length()/maxStepLength) + 1] ;
        int arrayIndex = 0 ;

        //Remplissage du tableau avec les NaN
        for (int i = 0; i < route.length(); i += maxStepLength) elevationSamples[arrayIndex++] = (float) route.elevationAt(i);

        //Recherche du nombre d'éléments valide au début et à la fin du tableau
        int numberOfFirstInvalidElements = searchNextValidElement(elevationSamples, 0, false);
        int numberOfLastInvalidElements = searchNextValidElement(elevationSamples, 0, true);

        //Retour d'un profil d'altitude 0 si uniquement des NaN
        if (numberOfFirstInvalidElements == elevationSamples.length) {
            Arrays.fill(elevationSamples, 0, elevationSamples.length, 0);
            return new ElevationProfile(route.length(), elevationSamples);
        }

        //Remplissage des premières et dernières valeurs invalides du tableau
        Arrays.fill(elevationSamples, 0, numberOfFirstInvalidElements, elevationSamples[numberOfFirstInvalidElements]);
        Arrays.fill(elevationSamples, elevationSamples.length - 1 - numberOfLastInvalidElements, elevationSamples.length - 1, elevationSamples[elevationSamples.length - 1 - numberOfLastInvalidElements]);

        //Remplissage des éléments invalides du tableau situés entre des éléments valides
        for (int i = numberOfFirstInvalidElements; i < elevationSamples.length - numberOfLastInvalidElements; i++) {
            if(Float.isNaN(elevationSamples[i])){
                int nextValidElement = searchNextValidElement(elevationSamples, i, false);
                elevationSamples[i] = (float) Math2.interpolate(elevationSamples[i - 1], elevationSamples[nextValidElement], maxStepLength / (double) nextValidElement - (i - 1));
            }
        }

        return new ElevationProfile(route.length(), elevationSamples);
    }

    /**
     * Méthode privée permettant de calculer l'index du premier élément valide (différent de Float. NaN)
     * dans le tableau array, à partir de l'index start, et dans l'ordre inverse si inverted est à true
     * et dans l'ordre du tableau si inverted est à false.
     *
     * @param array le tableau à analyser
     * @param start l'index à partir duquel chercher le prochain élément valide
     * @param inverted vrai si chercher dans l'ordre inverse et faux si chercher dans l'ordre du tableau
     * @return l'index du prochain élément valide
     */
    private static int searchNextValidElement(float[] array, int start, boolean inverted){
        int numberOfInvalidElements = 0  ;
        if (!inverted) {
            while (numberOfInvalidElements < array.length && Float.isNaN(array[start + numberOfInvalidElements])) numberOfInvalidElements++;
        } else {
            while(Float.isNaN(array[start + array.length - 1 - numberOfInvalidElements])) numberOfInvalidElements++ ;
        }
        return numberOfInvalidElements + start ;
    }
}
