/* 
 Copyright (C) 2016 University of South Florida.
 All rights reserved.

 This program is free software: you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public License
 as published by the Free Software Foundation, either version 3 of
 the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>. 
*/

package org.opentripplanner.updater.vehiclepositions;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import com.fasterxml.jackson.databind.JsonNode;

import org.opentripplanner.updater.JsonConfigurable;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.util.HttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.transit.realtime.GtfsRealtime.FeedEntity;
import com.google.transit.realtime.GtfsRealtime.FeedMessage;
import com.google.transit.realtime.GtfsRealtime.VehiclePosition;

public class GtfsRealtimeHttpVehiclePositionSource implements VehiclePositionSource, JsonConfigurable {
    private static final Logger LOG =
            LoggerFactory.getLogger(GtfsRealtimeHttpVehiclePositionSource.class);

    /**
     * Default agency id that is used for the trip ids in the Vehicle Positions
     */
    public String agencyId;

    public String url;

    @Override
    public void configure(Graph graph, JsonNode config) throws Exception {

        String url = config.path("url").asText();
        if (url == null) {
            throw new IllegalArgumentException("Missing mandatory 'url' parameter");
        }
        this.url = url;
        this.agencyId = config.path("defaultAgencyId").asText();
    }

    @Override
    public List<VehiclePosition> getUpdates() {
        FeedMessage feedMessage = null;
        List<FeedEntity> feedEntityList = null;
        List<VehiclePosition> updates = null;
        try {
            InputStream is = HttpUtils.getData(url);
            if (is != null) {
                feedMessage = FeedMessage.PARSER.parseFrom(is);
                feedEntityList = feedMessage.getEntityList();
                updates = new ArrayList<VehiclePosition>(feedEntityList.size());
                for (FeedEntity feedEntity : feedEntityList) {
                    updates.add(feedEntity.getVehicle());
                }
            }
        } catch (Exception e) {
            LOG.warn("Failed to parse gtfs-rt feed from " + url + ":", e);
        }
        return updates;
    }

    public String toString() {
        return "GtfsRealtimeHttpUpdateStreamer(" + url + ")";
    }
}
