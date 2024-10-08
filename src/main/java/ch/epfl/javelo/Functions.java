package ch.epfl.javelo;

import java.util.Arrays;
import java.util.function.DoubleUnaryOperator;

/*
 * Classe fournissant des méthodes pour créer des fonctions mathématiques définies
 * des réels vers les réels, c'est-à-dire R → R.
 *
 * @author Jean Perbet (341418)
 * @author Cassio Manuguerra (346232)
 */


public final class Functions {

    private Functions() {}

    /**
     * Fonction qui retourne une fonction constante dont la valeur est toujours y.
     *
     * @param y la valeur de la fonction constante
     * @return la fonction constante qui retourne toujours y
     */

    public static DoubleUnaryOperator constant(double y) {
        return new Constant(y);
    }

    /**
     * Fonction qui retourne une fonction obtenue par interpolation linéaire entre les échantillons samples,
     * espacés régulièrement et couvrant la plage allant de 0 à xMax.
     *
     * @param samples les ordonnées des échantillons, dont les abscisses sont réparties
     *                régulièrement entre 0 et xMax
     * @param xMax    l'abscisse maximale de la plage
     * @return la fonction obtenue par interpolation linéaire des échantillons samples entre 0 et xMax
     * @throws IllegalArgumentException lorsque samples a moins de deux éléments ou lorsque xMax est négatif ou nul
     */


    public static DoubleUnaryOperator sampled(float[] samples, double xMax) {
        Preconditions.checkArgument(samples.length >= 2 && xMax > 0);
        return new Sampled(Arrays.copyOf(samples, samples.length), xMax);
    }

    //---------------------------------------------- Private ----------------------------------------------//

    private final static record Constant(double y) implements DoubleUnaryOperator {
        @Override
        public double applyAsDouble(double operand) {
            return y;
        }
    }

    private final static record Sampled(float[] samples, double xMax) implements DoubleUnaryOperator {
        @Override
        public double applyAsDouble(double operand) {
            if (operand < 0) return samples[0];
            if (operand >= xMax) return samples[samples.length - 1];

            double spacing = xMax / (samples.length - 1);
            double proportion = (operand % spacing) / spacing;
            int minBorn = (int) Math2.clamp(0, operand / spacing, samples.length - 2) ;
            return Math2.interpolate(samples[minBorn], samples[minBorn + 1], proportion);
        }
    }
}
