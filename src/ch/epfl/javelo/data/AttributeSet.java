package ch.epfl.javelo.data;

import ch.epfl.javelo.Preconditions;
import ch.epfl.javelo.projection.SwissBounds;

/**
 * Enregistrement représentant un ensemble d'attributs OpenStreetMap.
 *
 * @author Jean Perbet (341418)
 * @author Cassio Manuguerra (346232)
 */
public record AttributeSet(long bits) {

    /**
     * Constructeur compact qui vérifie que la valeur passée au constructeur
     * ne contienne aucun bit à 1 ne correspondant pas à un attribut valide.
     *
     * @param bits la chaîne de bits représentant le contenu de l'ensemble :
     *             le bit b est à 1 si et seulement si l'attribut b est dans l'ensemble
     * @throws IllegalArgumentException si un bit est à 1 et ne correspond pas à un attribut valide
     */
    public AttributeSet {
        Preconditions.checkArgument(bits < Math.scalb(1, Attribute.COUNT));
    }

    public static AttributeSet of(Attribute... attributes){
        long mask = 0L;
        for (Attribute attribute : attributes) {
            long maskTemp = 1L << attribute.ordinal() ;
            mask = mask | maskTemp ;
        }
        return new AttributeSet(mask);
    }

    /**
     * Fonction qui retourne vrai si et seulement si l'ensemble récepteur
     * (this) contient l'attribut donné.
     *
     * @param attribute l'attribut donné
     * @return true si et seulement si l'ensemble récepteur (this) contient l'attribut donné
     */
    public boolean contains(Attribute attribute){
        long mask = 1L << attribute.ordinal();
        return (mask & bits) == mask;
    }

    /**
     * Fonction qui retourne vrai si et seulement si l'intersection de l'ensemble
     * récepteur (this) avec celui passé en argument (that) n'est pas vide.
     *
     * @param that
     * @return
     */

}
