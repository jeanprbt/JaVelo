package ch.epfl.javelo.projection;

/**
 * Classe permettant de convertir des cordonnées WGS 84 en coordonnées suisses et inversement.
 *
 * @author Jean Perbet (341418)
 * @author Cassio Manuguerra (346232)
 */
public final class Ch1903 {

    private Ch1903() {}

    /**
     * Fonction qui retourne la coordonnée E (est) dans le système Ch1903 du point de latitude lat
     * et de longitude lon dans le système de coordonnées WGS84.
     *
     * @param lon la longitude du point à convertir, en radians
     * @param lat la latitude du point à convertir, en radians
     * @return la coordonnée E (est) dans le système CH1903 du point de latitude lat et
     * de longitude lon dans le système de coordonnées WGS84
     */
    public static double e(double lon, double lat) {

        double lonDegrees = Math.toDegrees(lon);
        double latDegrees = Math.toDegrees(lat);

        double lon1 = 1e-4 * (3600 * lonDegrees - 26_782.5);
        double lat1 = 1e-4 * (3600 * latDegrees - 169_028.66);

        return 2_600_072.37 + 211_455.93 * lon1 - 10_938.51 * lon1 * lat1 -
                0.36 * lon1 * Math.pow(lat1, 2) - 44.54 * Math.pow(lon1, 3);
    }

    /**
     * Fonction qui retourne la coordonnée N (nord) dans le système Ch1903 du point de latitude lat
     * et de longitude lon dans le système de coordonnées WGS84.
     *
     * @param lon la longitude du point à convertir, en radians
     * @param lat la latitude du point à convertir, en radians
     * @return la coordonnée N (nord) dans le système CH1903 du point de latitude lat et
     * de longitude lon dans le système de coordonnées WGS84
     */
    public static double n(double lon, double lat) {

        double lonDegrees = Math.toDegrees(lon);
        double latDegrees = Math.toDegrees(lat);

        double lon1 = 1e-4 * (3600 * lonDegrees - 26_782.5);
        double lat1 = 1e-4 * (3600 * latDegrees - 169_028.66);

        return 1_200_147.07 + 308_807.95 * lat1 + 3745.25 * Math.pow(lon1, 2) + 76.63 * Math.pow(lat1, 2) -
                194.56 * Math.pow(lon1, 2) * lat1 + 119.79 * Math.pow(lat1, 3);
    }

    /**
     * Fonction qui retourne la longitude dans le système WGS84 du point de coordonnée e (est) et
     * de coordonnée n (nord) dans le système de coordonnées CHh1903, en radians.
     *
     * @param e la coordonnée E du point à convertir
     * @param n la coordonnée N du point à convertir
     * @return la longitude dans le système WGS84 du point de coordonnée e (est) et
     * de coordonnée n (nord) dans le système CH1903
     */
    public static double lon(double e, double n) {

        double x = 1e-6 * (e - 2_600_000);
        double y = 1e-6 * (n - 1_200_000);

        double lon0 = 2.6779094 + 4.728982 * x + 0.791484 * x * y + 0.1306 * x * Math.pow(y, 2) - 0.0436 * Math.pow(x, 3);

        return Math.toRadians(lon0 * (100 / 36.0));
    }

    /**
     * Fonction qui retourne la latitude dans le système WGS84 du point de coordonnée e (est) et
     * de coordonnée n (nord) dans le système de coordonnées Ch1903, en radians.
     *
     * @param e la coordonnée E du point à convertir
     * @param n la coordonnée N du point à convertir
     * @return la latitude dans le système WGS84 du point de coordonnée e (est) et
     * de coordonnée n (nord) dans le système CH1903
     */
    public static double lat(double e, double n) {

        double x = 1e-6 * (e - 2_600_000);
        double y = 1e-6 * (n - 1_200_000);

        double lat0 = 16.9023892 + 3.238272 * y - 0.270978 * Math.pow(x, 2) - 0.002528 * Math.pow(y, 2) -
                      0.0447 * Math.pow(x, 2) * y - 0.0140 * Math.pow(y, 3);

        return Math.toRadians(lat0 * (100 / 36.0));
    }
}
