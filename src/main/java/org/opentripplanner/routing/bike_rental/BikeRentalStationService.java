/* This program is free software: you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public License
 as published by the Free Software Foundation, either version 3 of
 the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>. */

package org.opentripplanner.routing.bike_rental;

import org.opentripplanner.routing.bike_park.BikePark;

import java.io.Serializable;
import java.util.*;

public class BikeRentalStationService implements Serializable {
    private static final long serialVersionUID = -1288992939159246764L;

    private Set<BikeRentalStation> bikeRentalStations = new HashSet<>();

    /* bikeRentalRegions is a map of bike network name to its service area. */
    private Map<String, BikeRentalRegion> bikeRentalRegions = new HashMap<>();

    private Set<BikePark> bikeParks = new HashSet<>();

    public Collection<BikeRentalStation> getBikeRentalStations() {
        return bikeRentalStations;
    }

    public void addBikeRentalStation(BikeRentalStation bikeRentalStation) {
        // Remove old reference first, as adding will be a no-op if already present
        bikeRentalStations.remove(bikeRentalStation);
        bikeRentalStations.add(bikeRentalStation);
    }

    public void removeBikeRentalStation(BikeRentalStation bikeRentalStation) {
        bikeRentalStations.remove(bikeRentalStation);
    }

    public Map<String, BikeRentalRegion> getBikeRentalRegions() {
        return bikeRentalRegions;
    }

    public void addBikeRentalRegion(BikeRentalRegion bikeRentalRegion) {
        bikeRentalRegions.put(bikeRentalRegion.network, bikeRentalRegion);
    }

    public Collection<BikePark> getBikeParks() {
        return bikeParks;
    }

    public void addBikePark(BikePark bikePark) {
        // Remove old reference first, as adding will be a no-op if already present
        bikeParks.remove(bikePark);
        bikeParks.add(bikePark);
    }

    public void removeBikePark(BikePark bikePark) {
        bikeParks.remove(bikePark);
    }
}
