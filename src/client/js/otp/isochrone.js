/* This program is free software: you can redistribute it and/or
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

var INIT_LOCATION = new L.LatLng(47.217, -1.557); // Nantes
// var INIT_LOCATION = new L.LatLng(44.840, -0.574); // Bordeaux
var ROUTER_ID = "default";
// var ISOCHRONE_TIMES = [ 1800 ]; // secs
var ISOCHRONE_TIMES = [ 900, 1800, 2700 ]; // secs
// var ISOCHRONE_TIMES = [ 900, 1800, 2700, 3600, 4500 ]; // secs
var DATE = '2014/06/01';
var TIME = '12:00:00';
var MODES = 'WALK,TRANSIT';
var PRECISION = 50; // meters
var OFFROAD = 80; // meters
var MAX_WALK_DISTANCE = 1000;

// Initialize a map
var map = L.map('map', {
    minZoom : 10,
    maxZoom : 18,
}).setView(INIT_LOCATION, 12);

// Add OSM layer
var osmAttrib = 'Map data &copy; 2011 OpenStreetMap contributors';
var osmLayer = new L.TileLayer("http://{s}.mqcdn.com/tiles/1.0.0/osm/{z}/{x}/{y}.png", {
    subdomains : [ "otile1", "otile2", "otile3", "otile4" ],
    maxZoom : 18,
    attribution : osmAttrib
});
map.addLayer(osmLayer);

// Isochrone layer group
var isochrones = new L.LayerGroup([]);
map.addLayer(isochrones);

// Add control
var baseMaps = {
    "OSM" : osmLayer
};
map.addControl(new L.Control.Layers(baseMaps));

// Origin marker icon
function dragEnd(e) {
    isochrones.clearLayers();
    updateIsochrones();
}
var origMarker = L.marker(INIT_LOCATION, {
    draggable : true
}).on('dragend', dragEnd).addTo(map);

function mapClick(e) {
    origMarker.setLatLng(e.latlng);
    isochrones.clearLayers();
    updateIsochrones();
}
map.on('click', mapClick);

function updateIsochrones() {
    var xhr = new XMLHttpRequest();
    var req = '/otp/routers/' + ROUTER_ID + '/isochrone?fromPlace=' + origMarker.getLatLng().lat + ','
            + origMarker.getLatLng().lng + '&date=' + DATE + '&time=' + TIME + '&mode=' + MODES + '&maxWalkDistance='
            + MAX_WALK_DISTANCE + '&precisionMeters=' + PRECISION + '&offRoadDistanceMeters=' + OFFROAD;
    for (var i = 0; i < ISOCHRONE_TIMES.length; i++) {
        req = req + '&cutoffSec=' + ISOCHRONE_TIMES[i];
    }
    xhr.open('GET', req, true);
    xhr.onload = function() {
        var geoJsonData = JSON.parse(this.responseText);
        for (var i = geoJsonData.features.length - 1; i >= 0; i--) {
            var isochrone = L.geoJson(geoJsonData.features[i], {
                style : {
                    color : "#0000FF",
                    weight : 2,
                    dashArray : i % 2 == 1 ? "5,5" : null,
                    fillOpacity : 0.1,
                    fillColor : "#808080"
                }
            });
            isochrones.addLayer(isochrone);
        }
    };
    xhr.send();
};

updateIsochrones();