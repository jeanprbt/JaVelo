package ch.epfl.javelo;

/**
 * Classe permettant de vérifier les préconditions pour les méthodes,
 * et de lancer une exception dans le cas ou les préconditions ne sont
 * pas remplies.
 *
 * @author Jean Perbet (341418)
 * @author Cassio Manuguerra (346232)
 */
public final class Preconditions {

    private Preconditions() {}

    /**
     * Méthode vérifiant si la condition passée en argument est vraie ou fausse.
     *
     * @param shouldBeTrue la condition à tester
     * @throws IllegalArgumentException si la condition est fausse
     */
    public static void checkArgument(boolean shouldBeTrue) {
        if (!shouldBeTrue) throw new IllegalArgumentException();
    }
}
