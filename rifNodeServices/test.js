var test1={
  "type": "MultiPolygon",
  "coordinates": [
    [
      [ 
	    [100.0, 2.0], 
	    [101.0, 3.0], 
		[102.0, 4.0], 
		[103.0, 5.0], 
		[104.0, 6.0] 
	  ]
    ],
    [
      [ 
	    [105.0, 7.0], 
		[106.0, 8.0], 
		[107.0, 9.0], 
		[108.0, 10.0], 
		[109.0, 11.0]
	  ]
    ],
    [
      [ 
	    [110.0, 12.0], 
		[111.0, 13.0], 
		[112.0, 14.0], 
		[113.0, 15.0], 
		[114.0, 16.0]
	  ]
    ]
  ]
};
var test2={
  "type": "Polygon",
  "coordinates": [
      [ 
	    [100.0, 2.0], 
	    [101.0, 3.0], 
		[102.0, 4.0], 
		[103.0, 5.0], 
		[104.0, 6.0] 
	  ]
  ]
};
function mpTest(test) {
	coordinates=test.coordinates;
	var firstPoint=[];
	var lastPoint=[];
	for (var i=0; i<coordinates.length; i++) {
		if (coordinates[i][0][0] && coordinates[i][0][0][0]) { // a further dimension (multipolygon)
			console.error("MultiPolygon");
			for (var j=0; j<coordinates[i].length; j++) {
				console.error("Polygon [" + i + "," + j + "]; length: " + (coordinates[i][j].length-1) + "; " + coordinates[i][j].toString() + 
					"; " + JSON.stringify(coordinates[i][j], null, 4));
			}			
			firstPoint=coordinates[i][(coordinates[i].length-1)].slice(0,1);
			lastPoint=coordinates[i][(coordinates[i].length-1)].slice((coordinates[i][(coordinates[i].length-1)].length-1),coordinates[i][(coordinates[i].length-1)].length);
			if (firstPoint[0][0] != lastPoint[0][0] && firstPoint[0][1] != lastPoint[0][1]) {
				console.error("Polygon add first point [" + i + "]; firstPoint: " + JSON.stringify(firstPoint) + "; lastPoint: " + JSON.stringify(lastPoint));
				coordinates[i][(coordinates[i].length-1)].push(firstPoint[0]);
			}				
		}
		else {
			console.error("Polygon");
			firstPoint=coordinates[i].slice(0,1);
			lastPoint=coordinates[i].slice((coordinates[i].length-1),coordinates[i].length);
			if (firstPoint[0][0] != lastPoint[0][0] && firstPoint[0][1] != lastPoint[0][1]) {
				console.error("Polygon add first point [" + i + "]; firstPoint: " + JSON.stringify(firstPoint) + "; lastPoint: " + JSON.stringify(lastPoint));
				coordinates[i].push(firstPoint[0]);
			}		
		}

	}
	console.error(JSON.stringify(test, null, 4));
}
mpTest(test1);
mpTest(test2);