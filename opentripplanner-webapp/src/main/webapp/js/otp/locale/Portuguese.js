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

otp.namespace("otp.locale");

/**
  * @class
  */

otp.locale.Portuguese = {

    config : 
    {
        metricsSystem : "international",
        rightClickMsg : "Haz click con el bot\xF3n derecho sobre el mapa para elegir los puntos de origen y destino.",
        attribution   : {
            title   : "License Attribution",
            content : "Disclaimer goes here"  // TODO localize
        }
    },

    contextMenu : 
    {
        fromHere         : "Começar uma viagem aqui",
        toHere           : "Acabar uma viagem aqui",
        intermediateHere : "Add intermediate point",

        centerHere       : "Center map here",
        zoomInHere       : "Zoom in",
        zoomOutHere      : "Zoom out",
        previous         : "Last map position",
        next             : "Next map position"
    },

    // TODO Localize Me
    bikeTriangle : 
    {
        safeName : "Bike friendly",
        safeSym  : "B",

        hillName : "Flat",
        hillSym  : "F",

        timeName : "Quick",
        timeSym  : "Q"
    },

    service : 
    {
        weekdays:  "Weekdays",
        saturday:  "Saturday",
        sunday:    "Sunday",
        schedule:  "Schedule"
    },

    indicators : 
    {
        ok         : "OK",
        date       : "Date",
        loading    : "Loading",
        searching  : "Searching...",
        qEmptyText : "Address, intersection, landmark or Stop ID..."
    },

    buttons: 
    {
        reverse       : "Reverse",
        reverseTip    : "<b>Reverse directions</b><br/>Plan a return trip by reversing this trip's start and end points, and adjusting the time forward.",
        reverseMiniTip: "Reverse directions",

        edit          : "Edit",
        editTip       : "<b>Edit trip</b><br/>Return to the main trip planner input form with the details of this trip.",

        clear         : "Clear",
        clearTip      : "<b>Clear</b><br/>Clear the map and all active tools.",

        fullScreen    : "Full Screen",
        fullScreenTip : "<b>Full Screen</b><br/>Show -or- hide tool panels",

        print         : "Print",
        printTip      : "<b>Print</b><br/>Print friendly version of the trip plan (without map).",

        link          : "Link",
        linkTip      : "<b>Link</b><br/>Show link url for this trip plan.",

        feedback      : "Feedback",
        feedbackTip   : "<b>Feedback</b><br/>Send your thoughts or experiences with the map",

        submit       : "Submit",
        clearButton  : "Clear",
        ok           : "OK",
        cancel       : "Cancel",
        yes          : "Yes",
        no           : "No",
        showDetails  : "Show details...",
        hideDetails  : "Hide details..."
    },

    // note: keep these lower case (and uppercase via template / code if needed)
    directions : 
    {
        southeast:      "southeast",
        southwest:      "southwest",
        northeast:      "northeast",
        northwest:      "northwest",
        north:          "north",
        west:           "west",
        south:          "south",
        east:           "east",
        bound:          "bound",
        left:           "left",
        right:          "right",
        slightly_left:  "slight left",
        slightly_right: "slight right",
        hard_left:      "hard left",
        hard_right:     "hard right",
        'continue':     "continue on",
        to_continue:    "to continue on",
        becomes:        "becomes",
        at:             "at",
        on:             "on",
        to:             "to",
        via:            "via",
        circle_counterclockwise: "take roundabout counterclockwise",
        circle_clockwise:        "take roundabout clockwise"
    },

    // see otp.planner.Templates for use
    instructions :
    {
        walk         : "Walk",
        walk_toward  : "Walk",
        walk_verb    : "Walk",
        bike         : "Bike",
        bike_toward  : "Bike",
        bike_verb    : "Bike",
        drive        : "Drive",
        drive_toward : "Drive",
        drive_verb   : "Drive",
        move         : "Proceed",
        move_toward  : "Proceed",

        transfer     : "transfer",
        transfers    : "transfers",

        continue_as  : "Continues as",
        stay_aboard  : "stay on board",

        depart       : "Depart",
        arrive       : "Arrive",

        start_at     : "Start at",
        end_at       : "End at"
    },

    // see otp.planner.Templates for use
    labels : 
    {
        agency_msg   : "Service run by", // TODO
        agency_msg_tt: "Open agency website in separate window...", // TODO
        about        : "About",
        stop_id      : "Stop ID",
        trip_details : "Trip details",
        fare         : "Fare",
        fare_symbol  : "\u20ac",
        travel       : "Travel",
        valid        : "Valid",
        trip_length  : "Time",
        with_a_walk  : "with a walk of",
        alert_for_rt : "Alert for route"
    },

    // see otp.planner.Templates for use -- one output are the itinerary leg headers
    modes :
    {
        WALK:           "Walk",
        BICYCLE:        "Bicycle",
        CAR:            "Car",
        TRAM:           "Tram",
        SUBWAY:         "Subway",
        RAIL:           "Rail",
        BUS:            "Bus",
        FERRY:          "Ferry",
        CABLE_CAR:      "Cable Car",
        GONDOLA:        "Gondola",
        FUNICULAR:      "Funicular"
    },

    ordinal_exit:
    {
        1:  "to first exit",
        2:  "to second exit",
        3:  "to third exit",
        4:  "to fourth exit",
        5:  "to fifth exit",
        6:  "to sixth exit",
        7:  "to seventh exit",
        8:  "to eighth exit",
        9:  "to ninth exit",
        10: "to tenth exit"
    },

    time:
    {
        minute         : "minuto",
        minutes        : "minutos",
        minute_abbrev  : "min",
        minutes_abbrev : "mins",
        second_abbrev  : "sec",
        seconds_abbrev : "secs",
        format         : "D, j M H:i",
        date_format    : "d-m-Y",
        time_format    : "H:i",
        months         : ['Ene', 'Feb', 'Mar', 'Abr', 'May', 'Jun', 'Jul', 'Ago', 'Sep', 'Oct', 'Nov', 'Dic']
    },

    systemmap :
    {
        labels :
        {
            panelTitle : "System Map"
        }
    },

    tripPlanner :
    {
        // see otp/planner/*.js for where these values are used
        labels : 
        {
            panelTitle    : "Trip Planner",
            tabTitle      : "Plan a Trip",
            inputTitle    : "Trip Details",
            optTitle      : "Trip Preferences (optional)",
            submitMsg     : "Planning Your Trip...",
            optionalTitle : "",
            date          : "Date",
            time          : "Time",
            when          : "When",
            from          : "From",
            fromHere      : "From here",
            intermediate  : "Intermediate Place",
            to            : "To",
            toHere        : "To here",
            minimize      : "Show me the",
            maxWalkDistance: "Maximum walk",
            maxBikeDistance: "Maximum bike",
            arriveDepart  : "Arrive by/Depart at",
            mode          : "Travel by",
            wheelchair    : "Wheelchair accessible trip", 
            go            : "Go",
            planTrip      : "Plan Your Trip",
            newTrip       : "New trip"
        },

        // see otp/config.js for where these values are used
        link : 
        {
            text           : "Link to this trip (OTP)",
            trip_separator : "This trip on other transit planners",
            bike_separator : "On other bike trip planners",
            walk_separator : "On other walking direction planners",
            google_transit : "Google Transit",
            google_bikes   : "Google Bike Directions",
            google_walk    : "Google Walking Directions",
            google_domain  : "http://www.google.com"
        },

        // see otp.planner.Forms for use
        geocoder:
        {
            working      : "Looking up address ....",
            error        : "Did not receive any results",
            msg_title    : "Review trip plan",
            msg_content  : "Please correct errors before planning your trip",
            select_result_title : "Please select a result",
            address_header : "Address"
        },

        error:
        {
            title        : 'Trip Planner Error',
            deadMsg      : "Map Trip Planner is currently not responding. Please wait a few minutes to try again, or try the text trip planner (see link below).",
            geoFromMsg   : "Please select the 'From' location for your trip: ",
            geoToMsg     : "Please select the 'To' location for your trip: "
        },
        
        // default messages from server if a message was not returned
        msgcodes:
        {
            200: "Plan OK",
            500: "Server error",
            400: "Trip out of bounds",
            404: "Path not found",
            406: "No transit times",
            408: "Request timed out",
            413: "Invalid parameter",
            440: "From geocode not found",
            450: "To geocode not found",
            460: "Geocode from and to not found",
            470: "From or to not wheelchair accessible",
            409: "Too close",
            340: "Geocode from ambiguous",
            350: "Geocode to ambiguous",
            360: "Geocode from and to ambiguous"
        },

        options: 
        [
          ['TRANSFERS', 'Fewest transfers'],
          ['QUICK',     'Quick trip'],
          ['SAFE',      'Bike friendly trip'],
          ['TRIANGLE',  'Custom trip...']
        ],
    
        arriveDepart: 
        [
          ['false', 'Depart'], 
          ['true',  'Arrive']
        ],

        maxWalkDistance : 
        [
            ['500',   '500 metros'],
            ['1000',   '1 km'],
            ['5000',   '5 km'],
            ['10000',  '10 km'],
            ['20000',  '20 km']
        ],

        mode : 
        [
            ['TRANSIT,WALK', 'Transit'],
// DO WE REALLY NEED THIS?  ISN'T BUS & TRAIN the same as TRANSIT, WALK
//          ['BUSISH,TRAINISH,WALK', 'Bus & Train'],
            ['BUSISH,WALK', 'Bus only'],
            ['TRAINISH,WALK', 'Train only'],
            ['WALK', 'Walk only'],
            ['BICYCLE', 'Bicycle'],
            ['TRANSIT,BICYCLE', 'Transit & Bicycle']
        ],

        wheelchair :
        [
            ['false', 'Not required'],
            ['true', 'Required']
        ]
    },

    CLASS_NAME : "otp.locale.Portuguese"
};
