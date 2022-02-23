package ch.epfl.javelo;

/**
 * Classe permettant de vérifier les préconditions pour les méthodes.
 * @author Jean Perbet (341418)
 * @author Cassio Manuguerra (346232)
 */
public final class Preconditions {

    private Preconditions() {}

    /**
     * Méthode vérifiant si la condition passée en argument est vraie ou fausse.
     * @throws IllegalArgumentException si la condition est fausse
     * @param shouldBeTrue la condition à tester
     */
    public static void checkArgument(boolean shouldBeTrue) {
        if (!shouldBeTrue) throw new IllegalArgumentException();
    }
}
