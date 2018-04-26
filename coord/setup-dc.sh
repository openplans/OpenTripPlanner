#!/bin/bash

# Get downtown DC road-network data.
curl -L -o coord/dc/district-of-columbia-latest.osm.pbf http://download.geofabrik.de/north-america/us/district-of-columbia-latest.osm.pbf

# Get DC transit network data.
curl -L -o coord/dc/dc.gtfs.zip https://transitfeeds.com/p/wmata/75/latest/download
