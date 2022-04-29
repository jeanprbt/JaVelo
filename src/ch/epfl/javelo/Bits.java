package ch.epfl.javelo;

/**
 * Classe permettant d'extraire une séquence de bits d'un vecteur de 32 bits.
 *
 * @author Jean Perbet (341418)
 * @author Cassio Manuguerra (346232)
 */
public final class Bits {

    private static final int VECTOR_LENGTH = Integer.SIZE;

    private Bits() {}

    /**
     * Fonction qui extrait du vecteur de 32 bits passé en argument la plage de length
     * bits commençant au bit d'index start, qu'elle interprète comme une valeur signée
     * en complément à deux, ou qui lève une IllegalArgumentException si la plage est invalide.
     *
     * @param value  le vecteur de 32 bits
     * @param start  le bit de départ de la plage à extraire
     * @param length la longueur de la plage à extraire
     * @return la valeur signée en complément à 2 correspondant à la plage donnée en argument
     * @throws IllegalArgumentException si la plage est invalide
     */
    public static int extractSigned(int value, int start, int length) {
        Preconditions.checkArgument(start >= 0 && length >= 0 && start + length <= VECTOR_LENGTH);
        //Décalage à gauche puis à droite (arithmétique)
        return (value << VECTOR_LENGTH - start - length) >> VECTOR_LENGTH - length;
    }

    /**
     * Fonction qui fait la même chose que la méthode précédente, à deux différences près :
     * d'une part, la valeur extraite est interprétée de manière non signée et d'autre part,
     * l'exception IllegalArgumentException est également levée si length vaut 32.
     *
     * @param value  le vecteur de 32 bits
     * @param start  le bit de départ de la plage à extraire
     * @param length la longueur de la plage à extraire
     * @return la valeur non signée correspondant à la plage donnée en argument
     * @throws IllegalArgumentException si la plage est invalide ou si length vaut 32
     */
    public static int extractUnsigned(int value, int start, int length) {
        Preconditions.checkArgument(start >= 0 && length >= 0 && start + length <= VECTOR_LENGTH && length < VECTOR_LENGTH);
        //Décalage à gauche puis à droite (logique)
        return (value << VECTOR_LENGTH - start - length) >>> VECTOR_LENGTH - length;
    }
}
