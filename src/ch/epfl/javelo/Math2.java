package ch.epfl.javelo;

/**
 * Classe fournissant des fonctions afin d'effectuer
 * certains calculs mathématiques
 * @author Jean Perbet (341418)
 * @author Cassio Manuguerra (346232)
 */
public final class Math2 {

    /**
     * Constructeur privé visant à éviter la
     * création d'instances de cette classe
     */
    private Math2() {}

    /**
     *
     * @param x l'abscisse d'un point
     * @param y l'ordonnée d'un point
     * @return le plus petit entier supérieur ou égal à x / y
     */
    public static int ceilDiv(int x, int y){
        return (x + y - 1) / y;
    }

    /**
     * @param x l'abscisse du point dont on recherche l'ordonnée
     * @param y0 l'ordonnée du point de la droite d'abscisse 0
     * @param y1 l'ordonnée du point de la droite d'abscisse 1
     * @return l'ordonnée du point de la droite d'ordonnée x
     * passant par (0, y0) et (1, y1)
     */
    public static double interpolate(double y0, double y1, double x) {
        return Math.fma(y1 - y0, x, y0);
    }

    /**
     * Limite la valeur de v dans l'intervalle allant de min à max
     * @param min la borne min de l'intervalle
     * @param v la valeur à limiteé
     * @param max la borne max de l'intervalle
     * @return la valeur limitée dans l'intervalle
     */
    public static int clamp(int min, int v, int max){
        if(min > max) throw new IllegalArgumentException();
        if(v < min) return min ;
        else if (v > max) return max ;
        else return v ;
    }

    /**
     * La même fonction que celle précédente mais prenant
     * et retournant des double plutôt que des int
     */
    public static double clamp(double min, double v, double max){
        if(min > max) throw new IllegalArgumentException();
        if(v < min) return min ;
        else if (v > max) return max ;
        else return v ;
    }


    /**
     * @param x nombre réel dont on veut le sinus hyperbolique inverse
     * @return le sinus hyperbolique inverse d'un réel x
     */
    public static double asinh(double x){
        return Math.log(x + Math.sqrt(1 + Math.pow(x, 2)));
    }

    /**
     *
     * @param uX première composante du vecteur u
     * @param uY seconde composante du vecteur u
     * @param vX première composante du vecteur v
     * @param uY seconde composante du vecteur v
     * @return le produit scalaire des vecteurs u(uX, uY) et v(vX, vY)
     */
    public static double dotProduct(double uX, double uY, double vX, double vY){
        return uX * vX + uY * vY;
    }

    /**
     * @param uX composante x du vecteur
     * @param uY composante y du vecteur
     * @return la norme au carré du vecteur (uX, uY)
     */
    public static double squaredNorm(double uX, double uY){
        return Math.pow(uX, 2) + Math.pow(uY, 2);
    }

    /**
     *
     * @param uX composante x du vecteur
     * @param uY composante y du vecteur
     * @return la norme du vecteur (uX, uY)
     */
    public static double norm(double uX, double uY ){
        return Math.sqrt(squaredNorm(uX, uY));
    }

    /**
     * @param aX abscisse du point A
     * @param aY ordonnée du point A
     * @param bX abscisse du point B
     * @param bY ordonnée du point B
     * @param pX abscisse du point P
     * @param pY ordonnée du point P
     * @return la longueur de la projection orthogonale du vecteur allant du point A(aX, aY)
     * au point P(pX, pY) sur le vecteur allant du point A(aX, aY) au point B(bX, bY)
     */
    public static double projectionLength(double aX, double aY, double bX, double bY, double pX, double pY){
        return dotProduct(pX - aX, pY - aY, bX - aX, bY - aY)/norm(bX - aX, bY - aY);
    }
}
