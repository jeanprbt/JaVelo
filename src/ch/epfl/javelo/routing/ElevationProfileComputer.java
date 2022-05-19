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

    private ElevationProfileComputer() {}

    /**
     * Fonction qui retourne le profil en long de l'itinéraire route, en garantissant
     * que l'espacement entre les échantillons du profil est d'au maximum maxStepLength mètres.
     *
     * @param route         l'itinéraire dont on veut le profil en long
     * @param maxStepLength l'espacement maximal entre les échantillons du profil
     * @return le profil en long de l'itinéraire route
     * @throws IllegalArgumentException si l'espacement maxStepLength est négatif ou nul
     */
    public static ElevationProfile elevationProfile(Route route, double maxStepLength) {
        Preconditions.checkArgument(maxStepLength > 0);

        //Initialisation d'un tableau contenant le bon nombre d'échantillons
        float[] elevationSamples = new float[(int) Math.ceil(route.length() / maxStepLength) + 1];

        //L'espacement entre les échantillons compte tenu de maxStepLength et de la longueur de l'itinéraire
        double stepLength = route.length() / (elevationSamples.length - 1.0);

        //Remplissage du tableau, en comptant les éventuels NaN
        for (int i = 0; i < elevationSamples.length; i++)
            elevationSamples[i] = (float) route.elevationAt(i * stepLength);


        //Recherche du nombre d'éléments invalides au début et à la fin du tableau
        int startInvalids = invalidElements(elevationSamples, false);
        int endInvalids = invalidElements(elevationSamples, true);

        //Retour d'un profil d'altitude 0 si uniquement des NaN
        if (startInvalids == elevationSamples.length) {
            Arrays.fill(elevationSamples, 0, elevationSamples.length, 0);
            return new ElevationProfile(route.length(), elevationSamples);
        }

        //Remplissage des premières et dernières valeurs invalides du tableau
        Arrays.fill(elevationSamples, 0, startInvalids, elevationSamples[startInvalids]);
        Arrays.fill(elevationSamples, elevationSamples.length - 1 - endInvalids, elevationSamples.length,
                elevationSamples[elevationSamples.length - 1 - endInvalids]);

        //Remplissage des éléments invalides du tableau situés entre des éléments valides
        fillMiddleNaN(elevationSamples);

        return new ElevationProfile(route.length(), elevationSamples);
    }

    /**
     * Méthode privée permettant de remplacer dans le tableau elevationSamples les valeurs valant Float.NaN
     * par des valeurs calculées par interpolation linéaire entre leurs voisins valides les plus proches : le
     * tableau doit au préalable avoir au moins ses deux extrémités de valides.
     *
     * @param elevationSamples le tableau à analyser
     */
    private static void fillMiddleNaN(float[] elevationSamples) {
        for (int i = 0; i < elevationSamples.length; i++) {
            if (Float.isNaN(elevationSamples[i])) {
                //Recherche du nombre d'éléments invalides consécutifs dans le tableau
                double invalidElements = 1;
                for (int j = 1; j < elevationSamples.length - 1; j++) {
                    if (!Float.isNaN(elevationSamples[i + j])) break;
                    else invalidElements++;
                }
                //Remplacement des éléments invalides consécutifs par interpolation linéaire
                for (int j = 0; j < invalidElements; j++)
                    elevationSamples[i + j] = (float) Math2.interpolate(elevationSamples[i - 1],
                            elevationSamples[(int) (i + invalidElements)], (j + 1) / (invalidElements + 1));
            }
        }
    }

    /**
     * Méthode privée permettant de calculer le nombre d'éléments invalides avant le premier élément valide à partir d'une
     * extrémité du tableau array, en itérant à partir de la fin si inverted est à true et à partir du début si inverted est à false.
     *
     * @param array    le tableau à analyser
     * @param inverted vrai si chercher dans l'ordre inverse et faux si chercher dans l'ordre du tableau
     * @return l'index du prochain élément valide
     */
    private static int invalidElements(float[] array, boolean inverted) {
        int invalidElements = 0;
        if (!inverted) {
            while (invalidElements < array.length && Float.isNaN(array[invalidElements]))
                invalidElements++;
        } else {
            while (invalidElements < array.length && Float.isNaN(array[array.length - 1 - invalidElements]))
                invalidElements++;
        }
        return invalidElements;
    }
}
