package ch.epfl.javelo.gui;

import ch.epfl.javelo.projection.PointCh;

/**
 * Enregistrement représentant un point de passage de l'itinéraire dans le système de coordonnées suisse.
 *
 * @author Jean Perbet (341418)
 * @author Cassio Manuguerra (346232)
 *
 * @param wayPoint la position du point dans le système de coordonnées suisse
 * @param closestNodeId l'identité du nœud le plus proche du point de passage
 */
public record Waypoint(PointCh wayPoint, int closestNodeId) {

}
