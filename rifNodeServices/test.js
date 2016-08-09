var test={
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
coordinates=test.coordinates;
var firstPoint=[];
var lastPoint=[];
for (var i=0; i<coordinates.length; i++) {
	for (var j=0; j<coordinates[i].length; j++) {
		console.error("Polygon [" + i + "," + j + "]; length: " + (coordinates[i][j].length-1) + "; " + coordinates[i][j].toString() + 
			"; " + JSON.stringify(coordinates[i][j], null, 4));
	}			
	firstPoint=coordinates[i][(coordinates[i].length-1)].slice(0,1);
	lastPoint=coordinates[i][(coordinates[i].length-1)].slice((coordinates[i][(coordinates[i].length-1)].length-1),coordinates[i][(coordinates[i].length-1)].length);
	if (firstPoint != lastPoint) {
		console.error("Polygon add first point [" + i + "]; firstPoint: " + JSON.stringify(firstPoint) + "; lastPoint: " + JSON.stringify(lastPoint));
		coordinates[i][(coordinates[i].length-1)].push(firstPoint[0]);
	}
}
console.error(JSON.stringify(test, null, 4));