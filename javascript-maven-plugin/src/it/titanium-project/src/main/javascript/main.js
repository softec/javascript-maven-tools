
// include helper functions
Ti.Geolocation.purpose = "Receive User Location";

// vars
A2B.mapview = null;
A2B.streetLabel = null
A2B.cityCountryLabel = null;
A2B.tableView = null;
A2B.checkInArray = [];

// create window
A2B.win = Ti.UI.createWindow({
	backgroundColor:'#fff'
});

// setup view and check into current location
A2B.createView();
A2B.checkIn();

// retrieve all past checkins
A2B.getCheckIns();

// open window
A2B.win.open();