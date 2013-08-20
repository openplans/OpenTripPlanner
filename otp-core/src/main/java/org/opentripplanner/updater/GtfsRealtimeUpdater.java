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

package org.opentripplanner.updater;

import java.io.IOException;
import java.io.InputStream;

import lombok.Setter;

import org.opentripplanner.routing.services.PatchService;
import org.opentripplanner.util.HttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.transit.realtime.GtfsRealtime;

public class GtfsRealtimeUpdater implements GraphUpdaterRunnable {
    private static final Logger log = LoggerFactory.getLogger(GtfsRealtimeUpdater.class);

    private Long lastTimestamp = Long.MIN_VALUE;

    @Setter
    private String url;

    @Setter
    private String defaultAgencyId;

    private PatchService patchService;

    @Setter
    private long earlyStart;

    private UpdateHandler updateHandler = null;

    @Override
    public void setup() {
        if (updateHandler == null) {
            updateHandler = new UpdateHandler();
        }
        updateHandler.setEarlyStart(earlyStart);
        updateHandler.setDefaultAgencyId(defaultAgencyId);
        updateHandler.setPatchService(patchService);
    }

    @Override
    public void run() {
        try {
            InputStream data = HttpUtils.getData(url);
            if (data == null) {
                throw new RuntimeException("Failed to get data from url " + url);
            }
            
            GtfsRealtime.FeedMessage feed = GtfsRealtime.FeedMessage.PARSER.parseFrom(data);
            
            long feedTimestamp = feed.getHeader().getTimestamp();
            if(feedTimestamp <= lastTimestamp) {
                log.info("Ignoring feed with an old timestamp.");
                return;
            }
        
            updateHandler.update(feed);
        
            lastTimestamp = feedTimestamp;
        } catch (IOException e) {
            log.error("Eror reading gtfs-realtime feed from " + url, e);
        }
    }

    @Override
    public void teardown() {
    }

    @Autowired
    public void setPatchService(PatchService patchService) {
        this.patchService = patchService;
    }

    public String toString() {
        return "GtfsRealtimeUpdater(" + url + ")";
    }
}
