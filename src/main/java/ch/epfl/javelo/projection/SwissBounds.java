package ch.epfl.javelo.projection;

/**
 * Classe donnant les constantes et méthodes liées aux limites de la Suisse.
 *
 * @author Jean Perbet (341418)
 * @author Cassio Manuguerra (346232)
 */
public final class SwissBounds {

    public static final double MIN_E = 2485000; // Plus petite coordonnée EST en Suisse
    public static final double MAX_E = 2834000; // Plus grande coordonnée EST en Suisse
    public static final double MIN_N = 1075000; // Plus petite coordonnée NORD en Suisse
    public static final double MAX_N = 1296000; // Plus grande coordonnée NORD Suisse
    public static final double WIDTH = MAX_E - MIN_E; // Largeur de la Suisse en mètres
    public static final double HEIGHT = MAX_N - MIN_N; // Hauteur de la Suisse en mètres

    private SwissBounds() {}

    /**
     * Méthode pour tester qu'un point se situe dans les limites ci-dessus.
     *
     * @param e la coordonnée est d'un point
     * @param n la coordonnée nord d'un point
     * @return true si le point est situé dans les limites de la Suisse et false sinon
     */
    public static boolean containsEN(double e, double n) {
        return (e >= MIN_E && e <= MAX_E && n >= MIN_N && n <= MAX_N);
    }
}

