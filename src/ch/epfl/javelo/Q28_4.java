package ch.epfl.javelo;

/**
 * Classe permettant de convertir des nombres entre la
 * représentation Q28.4 et d'autres représentations.
 *
 * @author Jean Perbet (341418)
 */
public final class Q28_4 {

    private Q28_4() {}

    /**
     * Fonction qui retourne la valeur Q28.4 correspondant à l'entier donné.
     *
     * @param i l'entier à convertir
     * @return la valeur Q28.4 correspondante au paramètre
     */
    public static int ofInt(int i) {
        return i << 4;
    }

    /**
     * Fonction qui retourne la valeur de type double égale à la valeur Q28.4 donnée en paramètre.
     *
     * @param q28_4 la valeur à convertir en double
     * @return le double correspondant au paramètre
     */
    public static double asDouble(int q28_4) {
        return Math.scalb((double) q28_4, -4);
    }

    /**
     * Fonction qui retourne la valeur de type float égale à la valeur Q28.4 donnée en paramètre.
     *
     * @param q28_4 la valeur à convertir en float
     * @return le float correspondant au paramètre
     */
    public static float asFloat(int q28_4) {
        return Math.scalb(q28_4, -4);
    }

}
