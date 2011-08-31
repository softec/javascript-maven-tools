// namespace
var A2B = {};

//
// Create main view
//
A2B.createView = function() {
	// create map view
	A2B.mapview = Titanium.Map.createView({
		mapType: Titanium.Map.STANDARD_TYPE,
		animate:true,
		regionFit:true,
		userLocation:false,
		top:0,
		left:0
	});
	A2B.win.add(A2B.mapview);

	// map view click event listener
	A2B.mapview.addEventListener('click', function(evt) {
		if (evt.clicksource == 'rightButton') {
			if (A2B.checkInArray.length != 0) {
				A2B.showCheckInDetail();
			}
		}
	});

	// add top grey view
	var locationView = Ti.UI.createView({
		backgroundImage:'images/gray_top_BG.png',
		height:83,
		width:320,
		top:0,
		left:0
	});
	A2B.win.add(locationView);

	// add current location labels
	var headerLabel = Ti.UI.createLabel({
		top:5,
		left:10,
		height:20,
		width:200,
		shadowColor:'#646464',
		shadowOffset: {
			x:1,
			y:1
		},
		text:'Current Location',
		font: {
			fontSize:12,
			fontWeight:'bold',
			fontFamily:'Helvetica Neue'
		},
		color:'#ccc'
	});
	locationView.add(headerLabel);

	A2B.streetLabel = Ti.UI.createLabel({
		top:22,
		left:10,
		height:20,
		width:195,
		shadowColor:'#555',
		shadowOffset: {
			x:1,
			y:1
		},
		font: {
			fontSize:13,
			fontWeight:'bold',
			fontFamily:'Helvetica Neue'
		},
		color:'#fff',
		text:'Street'
	});
	locationView.add(A2B.streetLabel);

	A2B.cityCountryLabel = Ti.UI.createLabel({
		top:40,
		left:10,
		height:20,
		width:195,
		shadowColor:'#555',
		shadowOffset: {
			x:1,
			y:1
		},
		font: {
			fontSize:13,
			fontWeight:'bold',
			fontFamily:'Helvetica Neue'
		},
		color:'#fff',
		text:'City, Country'
	});
	locationView.add(A2B.cityCountryLabel);

	// check-in button
	var checkInButton = Ti.UI.createButton({
		backgroundImage:'images/red_button_BG.png',
		height:31,
		width:100,
		color:'#fff',
		title:'Check-In',
		font: {
			fontSize:14,
			fontFamily:'Helvetica Neue',
			fontWeight:'bold'
		},
		right:10,
		top:20
	});
	locationView.add(checkInButton);

	// get current location and do a check-in
	checkInButton.addEventListener('click', function(){
		A2B.checkIn();
	});
};

//
// check-in
//
A2B.checkIn = function(longitude,latitude) {

	// get current location
	Titanium.Geolocation.getCurrentPosition( function(e) {
		if (!e.success) {
			alert('Could not retrieve location');
			return;
		}
		var longitude = e.coords.longitude;
		var latitude = e.coords.latitude;
		A2B.mapview.region = {
			latitude:latitude,
			longitude:longitude,
			latitudeDelta:0.5,
			longitudeDelta:0.5
		};

		// try to get address
		Titanium.Geolocation.reverseGeocoder(latitude,longitude, function(evt) {
			var street;
			var city;
			var country;
			if (evt.success) {
				var places = evt.places;
				if (places && places.length) {
					street = places[0].street;
					city = places[0].city;
					country = places[0].country_code;
				} else {
					address = "No address found";
				}
			}

			// update location labels
			A2B.streetLabel.text = street;
			A2B.cityCountryLabel.text = city + ', ' + country;

			var time = A2B.getTime();
			var title = street;
			var subtitle = city + ', ' + country + ' @ ' + time;

			// drop a pin
			A2B.addPin(longitude,latitude, title, subtitle );

			// add to array
			A2B.checkInArray.push({'check_in':{'lat':latitude,'long':longitude,'name':title + '***' + subtitle}});

			// update our table view that has all checkins
			A2B.updateCheckInTable();

			// save check-in
			A2B.saveCheckIn(longitude,latitude, title + '***' + subtitle);
		});
	});


};
//
// get time and return friendly format
//
A2B.getTime = function() {
	var d = new Date();
	var dateString = (d.getMonth()+1)+'/'+d.getDate()+'/'+d.getFullYear();
	var hour = d.getHours();
	var ampm;
	var min = d.getMinutes();

	if (hour < 12) {
		ampm = "AM";
	} else {
		ampm = "PM";
	}
	if (hour == 0) {
		hour = 12;
	}
	if (hour > 12) {
		hour = hour - 12;
	}
	if (min < 10) {
		min = '0' + min;
	}
	return dateString + ' ' + hour + ':' + min + ampm;

};
//
// drop a pin on the map
//
A2B.addPin = function(longitude,latitude,title, subtitle) {
	var pin = Titanium.Map.createAnnotation({
		latitude:latitude,
		longitude:longitude,
		title:title,
		subtitle:subtitle,
		animate:true,
		rightButton: Titanium.UI.iPhone.SystemButton.DISCLOSURE,
	});
	A2B.mapview.addAnnotation(pin);

};
//
// call remote service to save location
//
A2B.saveCheckIn = function(longitude, latitude, name) {
	var xhr = Ti.Network.createHTTPClient();
	xhr.open("POST","http://localhost/");
	xhr.setRequestHeader('Content-type','application/json');
	xhr.setRequestHeader('Accept','application/json');

	xhr.onload = function() {
		var rc = eval('('+this.responseText+')');
		if (rc['status'] == 'success') {
			alert('Location saved.');
		} else {
			alert('Cloud Error: try again.');
		}
	};
	xhr.send({
		'date':new Date(),
		'name':name,
		'long':longitude,
		'lat':latitude
	});

};
//
// retrieve all check-ins
//
A2B.getCheckIns = function(longitude, latitude, name) {
	var xhr = Ti.Network.createHTTPClient();
	xhr.open("GET","http://localhost/");

	xhr.onload = function() {
		A2B.checkInArray = eval('('+this.responseText+')');
		A2B.updateCheckInTable();
		for (var i=0;i<A2B.checkInArray.length;i++){
			var obj = A2B.checkInArray[i]['check_in'];
			var name = obj['name'].split('***');
			A2B.addPin(obj['long'], obj['lat'], name[0],name[1]);
		}
	};
	xhr.send();

};

//
// show all check-ins
//
A2B.showCheckInDetail = function(){
	var win = Ti.UI.createWindow({
		backgroundColor:'#fff'
	});

	// add top grey view
	var topView = Ti.UI.createView({
		backgroundImage:'images/gray_top_BG.png',
		height:83,
		width:320,
		top:0,
		left:0
	});
	win.add(topView);

	// add labels
	var headerLabel = Ti.UI.createLabel({
		top:5,
		left:10,
		height:20,
		width:200,
		shadowColor:'#646464',
		shadowOffset: {
			x:1,
			y:1
		},
		text:'Total Check-Ins',
		font: {
			fontSize:12,
			fontWeight:'bold',
			fontFamily:'Helvetica Neue'
		},
		color:'#ccc'
	});
	topView.add(headerLabel);

	var checkInCount = Ti.UI.createLabel({
		top:25,
		left:10,
		height:20,
		width:'auto',
		text:A2B.checkInArray.length,
		shadowColor:'#555',
		shadowOffset: {
			x:1,
			y:1
		},
		font: {
			fontSize:20,
			fontWeight:'bold',
			fontFamily:'Helvetica Neue'
		},
		color:'#fff',
	});
	topView.add(checkInCount);

	// add close button for window
	var closeButton = Ti.UI.createButton({
		backgroundImage:'images/blk_button_BG.png',
		height:31,
		width:100,
		color:'#fff',
		title:'Close',
		font: {
			fontSize:14,
			fontFamily:'Helvetica Neue',
			fontWeight:'bold'
		},
		right:10,
		top:20
	});
	topView.add(closeButton);
	closeButton.addEventListener('click', function(){
		win.close();
	});

	// add table view of check-ins
	win.add(A2B.tableView);

	// open window
	win.open();

}
//
//  build a table view cotaining all checkins
//
A2B.updateCheckInTable = function(){
	var data = [];

	if (A2B.tableView == null){
		A2B.tableView = Ti.UI.createTableView({top:83});
	}

	for (var i=(A2B.checkInArray.length-1);i>=0;i--){
		var row = Ti.UI.createTableViewRow({height:60});
		var nameString = A2B.checkInArray[i]['check_in']['name'].split('***');
		var title = nameString[0];
		var subtitle = nameString[1];

		var titleLabel = Ti.UI.createLabel({
			color:'#000',
			font:{fontSize:20, fontWeight:'bold', fontFamily:'Helvetica Neue'},
			text:title,
			height:25,
			width:300,
			top:5,
			left:5
		});
		row.add(titleLabel);

		var subtitleLabel = Ti.UI.createLabel({
			color:'#999',
			font:{fontSize:16, fontWeight:'bold', fontFamily:'Helvetica Neue'},
			text:subtitle,
			height:25,
			width:300,
			top:30,
			left:5
		});
		row.add(subtitleLabel);
		data.push(row);
	}

	A2B.tableView.setData(data);
};
