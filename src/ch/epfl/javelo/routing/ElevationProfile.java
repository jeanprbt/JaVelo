package ch.epfl.javelo.routing;

import ch.epfl.javelo.Functions;
import ch.epfl.javelo.Preconditions;

import java.util.Arrays;
import java.util.DoubleSummaryStatistics;

/**
 * Classe immuable représentant le profil en long d'un itinéraire simple ou multiple.
 *
 * @author Jean Perbet (341418)
 * @author Cassio Manuguerra (346232)
 */
public final class ElevationProfile {

    private final double length;
    private final float[] elevationSamples;

    /**
     * Constructeur public du profil en long d'un itinéraire simple ou multiple.
     *
     * @param length la longueur du profil
     * @param elevationSamples le tableau d'échantillons répartis régulièrement sur la longueur du profil
     * @throws IllegalArgumentException si la longueur est négative ou nulle ou si le tableau d'échantillons contient moins de deux éléments
     */
    public ElevationProfile(double length, float[] elevationSamples) {
        Preconditions.checkArgument(length > 0 && elevationSamples.length >= 2);
        this.length = length;
        this.elevationSamples = Arrays.copyOf(elevationSamples, elevationSamples.length);
    }

    /**
     * Fonction qui retourne la longueur du profil, en mètres.
     *
     * @return la longueur du profil, en mètres
     */
    public double length(){
        return length;
    }

    /**
     * Fonction qui retourne l'altitude minimum du profil en mètres.
     *
     * @return l'altitude minimum du profil en mètres
     */
    public double minElevation(){
        return stats(elevationSamples).getMin() ;
    }

    /**
     * Fonction qui retourne l'altitude maximum du profil en mètres.
     *
     * @return l'altitude maximum du profil en mètres
     */
    public double maxElevation(){
        return stats(elevationSamples).getMax();
    }

    /**
     * Fonction qui retourne le dénivelé positif total du profil en mètres.
     *
     * @return le dénivelé positif total du profil en mètres
     */
    public double totalAscent(){
        return computeElevationDifference(true, elevationSamples);
    }

    /**
     * Fonction qui retourne le dénivelé négatif total du profil en mètres.
     *
     * @return le dénivelé négatif total du profil en mètres
     */
    public double totalDescent(){
        return computeElevationDifference(false, elevationSamples);
    }

    /**
     * Fonction qui retourne l'altitude du profil à la position donnée,
     * qui n'est pas forcément comprise entre zéro et la longueur du profil :
     * le premier échantillon est retourné lorsque la position est négative,
     * le dernier lorsqu'elle est supérieure à la longueur.
     *
     * @param position la position donnée
     * @return l'altitude du profil à la position donnée
     */
    public double elevationAt(double position){
        return Functions.sampled(elevationSamples, length).applyAsDouble(position) ;
    }

    //La redéfinition de la méthode equals() est ici uniquement utile à des fins de tests unitaires.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ElevationProfile that = (ElevationProfile) o;
        return Double.compare(that.length, length) == 0 && Arrays.equals(elevationSamples, that.elevationSamples);
    }

    /**
     * Méthode privée retournant une classe statistique comprenant toutes les
     * valeurs contenues dans le tableau passé en paramètre.
     *
     * @param array le tableau sur lequel on veut des statistiques
     * @return un DoubleSummaryStatistics contenant toutes les valeurs du tableau passé en paramètre
     */
    private DoubleSummaryStatistics stats (float[] array){
        DoubleSummaryStatistics s = new DoubleSummaryStatistics();
        for (float v : array) s.accept(v);
        return s ;
    }

    /**
     * Méthode privée qui calcule le dénivelé positif ou négatif d'un profil.
     *
     * @param array le tableau donnant le profil dont on cherche le dénivelé positif ou négatif
     * @param positive vrai si on cherche le dénivelé positif et faux si on cherche le dénivelé négatif
     * @return le dénivelé positif ou négatif dans le profil donné par array
     */
    private double computeElevationDifference(boolean positive, float[] array){
        double elevationDifference = 0, currentElevation = array[0];
        for (float v : array) {
            if(positive && v > currentElevation) elevationDifference += v - currentElevation ;
            if(!positive && v < currentElevation) elevationDifference += currentElevation - v;
            currentElevation = v;
        }
        return elevationDifference ;
    }
}
