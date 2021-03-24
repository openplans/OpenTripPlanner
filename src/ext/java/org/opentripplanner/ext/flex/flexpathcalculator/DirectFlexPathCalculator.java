package org.opentripplanner.ext.flex.flexpathcalculator;

import org.opentripplanner.common.geometry.SphericalDistanceLibrary;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.routing.graph.Vertex;

/**
 * Calculated driving times and distance based on direct distance and fixed average driving speed.
 */
public class DirectFlexPathCalculator implements FlexPathCalculator<Integer> {
  public static final double FLEX_SPEED = 8.0;

  private static final int DIRECT_EXTRA_TIME = 5 * 60;

  private final double flexSpeed;

  public DirectFlexPathCalculator(Graph graph) {
    this.flexSpeed = graph.flexSpeed;
  }

  @Override
  public FlexPath calculateFlexPath(
      Vertex fromv, Vertex tov, Integer fromStopIndex, Integer toStopIndex
  ) {
    double distance = SphericalDistanceLibrary.distance(fromv.getCoordinate(), tov.getCoordinate());

    return new FlexPath((int) distance, (int) (distance / flexSpeed) + DIRECT_EXTRA_TIME);
  }
}
