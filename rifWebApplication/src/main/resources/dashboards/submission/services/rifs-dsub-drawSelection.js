/**
 * The Rapid Inquiry Facility (RIF) is an automated tool devised by SAHSU 
 * that rapidly addresses epidemiological and public health questions using 
 * routinely collected health and population data and generates standardised 
 * rates and relative risks for any given health outcome, for specified age 
 * and year ranges, for any given geographical area.
 *
 * Copyright 2016 Imperial College London, developed by the Small Area
 * Health Statistics Unit. The work of the Small Area Health Statistics Unit 
 * is funded by the Public Health England as part of the MRC-PHE Centre for 
 * Environment and Health. Funding for this project has also been received 
 * from the United States Centers for Disease Control and Prevention.  
 *
 * This file is part of the Rapid Inquiry Facility (RIF) project.
 * RIF is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * RIF is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with RIF. If not, see <http://www.gnu.org/licenses/>; or write 
 * to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, 
 * Boston, MA 02110-1301 USA
 
 * Peter Hambly
 * @author phambly
 */

/* 
 * SERVICE to make drawn shape selection
 */
angular.module("RIF")
        .factory('DrawSelectionService', ['SelectStateService', '$timeout', 'AlertService', 'CommonMappingStateService', 'GISService', 
				function (SelectStateService, $timeout, AlertService, CommonMappingStateService, GISService) {

                        function makeDrawSelection2(shape, selectorBands, input, mapName, latlngList, makeDrawSelectionCallback) {
							
							if (mapName == undefined) {
								throw new Error("[rifs-dsub-drawSelection.js] mapName is undefined");
							}
							else if (CommonMappingStateService.getState(mapName) == undefined) {
								throw new Error("[rifs-dsub-drawSelection.js] CommonMappingStateService.getState(mapName) is undefined");
							}
							else if (CommonMappingStateService.getState(mapName).shapes == undefined) {
								throw new Error("[rifs-dsub-drawSelection.js] CommonMappingStateService.getState(mapName).shapes is undefined");
							}
							
							// Create savedShape for SelectStateService
							var savedShape = {
								isShapefile: (shape.isShapefile || false),
								circle: shape.circle,
								freehand: shape.freehand,
								band: shape.band,
								area: shape.area, 
								properties: shape.properties,
								radius: shape.radius,
								latLng: undefined,
								geojson: undefined,
								bbox: undefined,
								finalCircleBand: (shape.finalCircleBand || false),
								style: undefined,
								selectionMethod: shape.selectionMethod
							}
//							
// Risk analysis study types (as per rif40_studies.stype_type): 
//
// 11 - Risk Analysis (many areas, one band), 
// 12 - Risk Analysis (point sources, many areas, one to six bands) [DEFAULT], 
// 13 - Risk Analysis (exposure covariates), 
// 14 - Risk Analysis (coverage shapefile), 
// 15 - Risk Analysis (exposure shapefile)
//
							if (shape.selectionMethod === 1) { // selectionMethod 1: Single boundary; already set
								SelectStateService.getState().studySelection.riskAnalysisType = 11;
							}
							else if (shape.selectionMethod === 2) { // selectionMethod 2: make selection by band attribute in file
								SelectStateService.getState().studySelection.riskAnalysisType = 13;
							}
							else if (shape.selectionMethod === 3) { // selectionMethod 3: make selection by attribute value in file			
								SelectStateService.getState().studySelection.riskAnalysisType = 15;				
							}
								
							if (savedShape.freehand) {						
								
								if (input.type === "Risk Analysis") {
									AlertService.showError("Freehand selection not permitted for risk analysis");
									var polyId=shape.data._leaflet_id;
									CommonMappingStateService.getState(mapName).drawnItems.eachLayer(
										function(layer) {
											AlertService.consoleDebug("[rifd-dsub-maptable.js] Remove freehand polygon; " + layer._leaflet_id);
											CommonMappingStateService.getState(mapName).map.removeLayer(layer);
										});
										
									if (makeDrawSelectionCallback && typeof makeDrawSelectionCallback === "function") {
										makeDrawSelectionCallback("Freehand selection not permitted for risk analysis");
									}	
									else {
										throw new Error("No makeDrawSelectionCallback() function");
									}	
									return;
								}
								else {
								
									if (shape.band == -1) {
										shape.band=1;
										savedShape.band=1;
									}
									
									if (shape.data._latlngs && shape.data._latlngs.length > 1) { // Fix freehand polygons
										if (shape.data._latlngs[0].lat == shape.data._latlngs[shape.data._latlngs.length-1].lat &&
										   shape.data._latlngs[0].lng == shape.data._latlngs[shape.data._latlngs.length-1].lng) { // OK
										} 
										else { // Make it a polygon
											shape.data._latlngs.push({
												lat: shape.data._latlngs[0].lat,
												lng: shape.data._latlngs[0].lng
											});
											AlertService.consoleDebug("[rifd-dsub-maptable.js] Fix freehand polygon; " +
											shape.data._latlngs.length + " points: " + 
												JSON.stringify(shape.data._latlngs));
										}
									}
								}
							}
							else {
								var fileList = SelectStateService.getState().studySelection.fileList;
									if (fileList && fileList.length > 0) {
									AlertService.consoleDebug("[rifd-dsub-maptable.js] " + fileList.length +
										"; savedShape.isShapefile: " + savedShape.isShapefile);	
								}
								else {
									AlertService.consoleDebug("[rifd-dsub-maptable.js] no shapefiles");
								}								
							}
							
							if (SelectStateService.getState().studySelection.bandAttr.length > 0) {
								AlertService.consoleDebug("[rifd-dsub-maptable.js] " + 
									SelectStateService.getState().studySelection.bandAttr.length +
									"; bandAttr: " + JSON.stringify(SelectStateService.getState().studySelection.bandAttr, null, 1));	
							}					
							
							savedShape.style={
										color: (selectorBands.bandColours[savedShape.band-1] || 'blue'),
										weight: (selectorBands.weight || 3),
										opacity: (selectorBands.opacity || 0.8),
										fillOpacity: (selectorBands.fillOpacity || 0)
									};
		
							function highLightFeature(e) {
								AlertService.consoleDebug("[rifd-dsub-maptable.js] makeDrawSelection highLightFeature " +  
										"(" + this._leaflet_id + "; " + JSON.stringify(this._latlng) + "): " +
										(JSON.stringify(savedShape.properties) || "no properties"));
								CommonMappingStateService.getState(mapName).info.update(savedShape, this._latlng); 
							}									
							function resetFeature(e) {
								AlertService.consoleDebug("[rifd-dsub-maptable.js] makeDrawSelection resetFeature " +  
										"(" + this._leaflet_id + "; " + JSON.stringify(this._latlng) + "): " +
										(JSON.stringify(savedShape.properties) || "no properties"));
								CommonMappingStateService.getState(mapName).info.update(undefined, this._latlng);
							}		
							
							if (shape.circle) { // Represent circles as a point and a radius
								if (savedShape.radius == undefined) {
									savedShape.radius=shape.data.getRadius();
								}
								savedShape.latLng=shape.data.getLatLng();
								if (savedShape.area == undefined || savedShape.area == 0) {
									savedShape.area = Math.round((Math.PI*Math.pow(shape.data.getRadius(), 2)*100)/1000000)/100 // Square km to 2dp
									
									if (savedShape.area == undefined || savedShape.area == 0) {
										AlertService.consoleDebug("[rifd-dsub-maptable.js] makeDrawSelection(): Cannot determine area" +
											"; geoJSON: " + JSON.stringify(savedShape.geojson, null, 1));
										AlertService.showError("Could not create circle shape");
									}
								}
								if ((shape.band == 1) || (shape.band > 1 && !savedShape.finalCircleBand)) {
									// basic shape to map shapes layer group
									var circle = new L.Circle([savedShape.latLng.lat, savedShape.latLng.lng], {
											pane: 'shapes', 
											band: savedShape.band,
											area: savedShape.area,
											radius: savedShape.radius,
											color: (savedShape.style.color || selectorBands.bandColours[savedShape.band-1] || 'blue'),
											weight: (savedShape.style.weight || selectorBands.weight || 3),
											opacity: (savedShape.style.opacity || selectorBands.opacity || 0.8),
											fillOpacity: (savedShape.style.fillOpacity || selectorBands.fillOpacity || 0)
										});				
									circle.on({
										mouseover: highLightFeature,
										mouseout: resetFeature
									});
									
									CommonMappingStateService.getState(mapName).shapes.addLayer(circle);
									AlertService.consoleLog("[rifd-dsub-maptable.js] makeDrawSelection() added circle" +
										"; color: " + selectorBands.bandColours[savedShape.band-1] +
										"; savedShape: " + JSON.stringify(savedShape, null, 1));
									if (shape.band == 1) {
									   var factory = L.icon({
											iconUrl: 'images/factory.png',
											iconSize: 15
										});
										var marker = new L.marker([savedShape.latLng.lat, savedShape.latLng.lng], {
											pane: 'shapes',
											icon: factory
										});
										CommonMappingStateService.getState(mapName).shapes.addLayer(marker);
									}
								}
								else {
									AlertService.consoleLog("[rifd-dsub-maptable.js] makeDrawSelection() suppressed circle" +
										"; savedShape: " + JSON.stringify(savedShape, null, 1));
								}
							}
							else if (shape.data._latlngs) { // Use geoJSON								
								
								var coordinates;
								
								if (shape.data._latlngs && shape.data._latlngs.length > 1) {
									coordinates=shape.data._latlngs;
								}
								else {
									coordinates=shape.data._latlngs[0];
								}
							
								var polygon;
								
								if (savedShape.freehand) {
									polygon=L.polygon(coordinates, {
											pane: 'shapes', 
											band: savedShape.band,
											area: savedShape.area,
											color: (savedShape.style.color || selectorBands.bandColours[savedShape.band-1] || 'blue'),
											weight: (savedShape.style.weight || selectorBands.weight || 3),
											opacity: (savedShape.style.opacity || selectorBands.opacity || 0.8),
											fillOpacity: (savedShape.style.fillOpacity || selectorBands.fillOpacity || 0)
										});		
									savedShape.coordinates=coordinates; // L.Polygon()	- now fixed; was a lineString
									savedShape.geojson=angular.copy(polygon.toGeoJSON());								
								}
								else { // Shapefile
									savedShape.geojson=angular.copy(shape.data.toGeoJSON());	
									polygon=L.polygon(savedShape.geojson.geometry.coordinates[0], {
											pane: 'shapes', 
											band: savedShape.band,
											area: savedShape.area,
											color: (savedShape.style.color || selectorBands.bandColours[savedShape.band-1] || 'blue'),
											weight: (savedShape.style.weight || selectorBands.weight || 3),
											opacity: (savedShape.style.opacity || selectorBands.opacity || 0.8),
											fillOpacity: (savedShape.style.fillOpacity || selectorBands.fillOpacity || 0)
										});
								}
							
								if (polygon && polygon._latlngs.length > 0) {	
									
									if (savedShape.area == undefined || savedShape.area == 0) {
										if (savedShape.geojson) {
											savedShape.area = Math.round((turf.area(savedShape.geojson)*100)/1000000)/100; // Square km to 2dp
										}
										else {
											AlertService.consoleLog("[rifd-dsub-maptable.js] makeDrawSelection(): savedShape.area could not be set: " + 
												JSON.stringify(savedShape));
										}
									
										if (savedShape.area == undefined || savedShape.area == 0) {
											AlertService.consoleDebug("[rifd-dsub-maptable.js] makeDrawSelection(): Cannot determine area" +
												"; geoJSON: " + JSON.stringify(savedShape.geojson, null, 1));
											if (savedShape.freehand) {	
												AlertService.showError("Could not create freehand Polygon shape");
											}
											else {
												AlertService.showError("Could not create shapefile Polygon shape");
											}
										}
										polygon.options.area=savedShape.area;
									}
								
									polygon.on({
										mouseover: highLightFeature,
										mouseout: resetFeature
									}); 
									CommonMappingStateService.getState(mapName).shapes.addLayer(polygon);
										
									AlertService.consoleDebug("[rifd-dsub-maptable.js] makeDrawSelection(): added Polygon" + 
										"; band: " + savedShape.band +
										"; area: " + savedShape.area +
										"; freehand: " + savedShape.freehand +
										"; style: " + JSON.stringify(savedShape.style) +
										"; " + coordinates.length + " coordinates; " +
												JSON.stringify(coordinates).substring(0,100) + "..." +
										"; properties: " + (JSON.stringify(savedShape.properties) || "None"));										
								}
								else {
									AlertService.consoleDebug("[rifd-dsub-maptable.js] makeDrawSelection(): L.Polygon is undefined" +
										"; geoJSON: " + JSON.stringify(savedShape.geojson, null, 1));
									if (savedShape.freehand) {	
										AlertService.showError("Could not create freehand Polygon shape");
									}
									else {
										AlertService.showError("Could not create shapefile Polygon shape");
									}
								}
							}
							else { // Ignored (outermost band in freehand circles)
							}	
							
							function addBbox() { // Returns a promise
								
								return $timeout(function() { // Wait a bit to avoid
									// TypeError: Unable to get property 'layerPointToLatLng' of undefined or null reference				  
									try {	
										var bbox=undefined;	
										var bounds=undefined;										
										if (shape.circle && circle) { // Represent circles as a point and a radius								
											bounds=circle.getBounds();
										}
										else if (savedShape.geojson && polygon) {
											bounds = polygon.getBounds();
										}
										else { // Ignored (outermost band in freehand circles)
											savedShape.bbox=undefined;
											return;
										}
										
										if (bounds && bounds.isValid()) {
											bbox=[bounds.getWest(), 
												 bounds.getSouth(),
												 bounds.getEast(),
												 bounds.getNorth()]; // Bounding box:  [minX, minY, maxX, maxY]	 
										}	
										
										if (bbox) {
											savedShape.bbox=L.latLngBounds(
												L.latLng(bbox[1], bbox[0]),  // South west [minY, minX]
												L.latLng(bbox[3], bbox[2])); // North east [maxY, maxX]
											if (savedShape.bbox && 
											    savedShape.bbox.isValid() && 
												bounds && 
												bounds.isValid() && 
												savedShape.bbox.equals(bounds)) { 
												// Verify
												shape.bbox = savedShape.bbox;
											}
											else { // It doesn't matter if the BBOX is invalid as the GISService will be called directly
												bboxErrors++;
												if (bboxErrors < 10) {							
													AlertService.consoleError("[rifd-dsub-maptable.js] makeDrawSelection() invalid bbox: " +
														JSON.stringify(bbox) +
														"; savedShape.bbox: " + 
															(savedShape.bbox ? savedShape.bbox.toBBoxString() : "undefined") +
														"; savedShape.bbox.isValid(): " + 
															(savedShape.bbox ? savedShape.bbox.isValid() : "undefined") +
														"; bounds: " + (bounds ? bounds.toBBoxString() : "undefined") +
														"; bounds.isValid(): " + (bounds ? bounds.isValid() : "undefined")+
														"; savedShape.bbox.equals(bounds): " + 
															((bounds && savedShape.bbox) ? savedShape.bbox.equals(bounds) : "undefined"));
												}
												savedShape.bbox=undefined;
											}
										}
										else {
											bboxErrors++;
											if (bboxErrors < 10) {
												AlertService.consoleError("[rifd-dsub-maptable.js] makeDrawSelection() no bbox" +
													"; bounds: " + (bounds ? bounds.toBBoxString() : "undefined"));
											}
										}
									}	
									catch (e) { // For a better message
										AlertService.consoleError("[rifd-dsub-maptable.js] makeDrawSelection() error in bbox creation: " + 
											e.message +
											"; bounds: " + (bounds ? bounds.toBBoxString() : "undefined"), e);
										bboxErrors++;
									}	
								}, 100);
							}
										
							// Save to SelectStateService
							if (input.name == "ComparisionAreaMap") { 
								SelectStateService.getState().studySelection.comparisonShapes.push(savedShape);
								AlertService.consoleDebug("[rifd-dsub-maptable.js] Save to ComparisionAreaMap SelectStateService " +
									SelectStateService.getState().studySelection.comparisonShapes.length);
							}
							else {
								SelectStateService.getState().studySelection.studyShapes.push(savedShape);							
							
								AlertService.consoleDebug("[rifd-dsub-maptable.js] Save to StudyAreaMap SelectStateService " +
									SelectStateService.getState().studySelection.studyShapes.length);
							}
							
							function latlngListEnd () { 
								CommonMappingStateService.getState(mapName).areaNameList = {};
								var areaCheck = {};
								var duplicateAreaCheckIds = [];
								
								// Check for duplicate selectedPolygons 
								for (var j = 0; j < CommonMappingStateService.getState(mapName).selectedPolygon.length; j++) {
									var thisPolyID = CommonMappingStateService.getState(mapName).selectedPolygon[j].id;
									if (areaCheck[thisPolyID]) {
										areaCheck[thisPolyID].count++;
									}
									else {
										areaCheck[thisPolyID] = { 
											count: 1,
											index: []
										};
									}
									areaCheck[thisPolyID].index.push(j);
								}
								for (var id in areaCheck) {
									if (areaCheck[id].count > 1) {
										duplicateAreaCheckIds.push(id);
									}
									
/*									if (duplicateAreaCheckIds.length < 10) {
										AlertService.consoleDebug("[rifd-dsub-maptable.js] " + 
											"duplicateAreaCheckIds[" + duplicateAreaCheckIds.length + "] " +
											id + "; duplicates: " +
											JSON.stringify(areaCheck[id].index));
									} */
								}
								if (duplicateAreaCheckIds.length > 0) {
									AlertService.consoleDebug("[rifd-dsub-maptable.js] " + 
										duplicateAreaCheckIds.length +
										" duplicateAreaCheckIds: " + JSON.stringify(duplicateAreaCheckIds));
									AlertService.showError("Duplicate area IDs detected in selected polygon list");
								}
								
								for (var i = 0; i < latlngList.length; i++) {
									var thisPolyID = latlngList[i].id;
									
									// Update band
									if (CommonMappingStateService.getState(mapName).selectedPolygonObj[thisPolyID]) {
										latlngList[i].band=CommonMappingStateService.getState(mapName).selectedPolygonObj[thisPolyID].band;
										break;
									} 	
									// Sync table - done by submissionMapTable $scope.$watchCollection() above
                            									
									// Update areaNameList for debug
									if (latlngList[i].band && latlngList[i].band != -1) {
										if (CommonMappingStateService.getState(mapName).areaNameList[latlngList[i].band]) {
											CommonMappingStateService.getState(mapName).areaNameList[latlngList[i].band].push(latlngList[i].name);
										}
										else {
											CommonMappingStateService.getState(mapName).areaNameList[latlngList[i].band] = [];
											CommonMappingStateService.getState(mapName).areaNameList[latlngList[i].band].push(latlngList[i].name);
										}
									}
								}
										
								AlertService.consoleDebug("[rifd-dsub-maptable.js] CommonMappingStateService.getState(" + mapName + 
									").selectedPolygon.length: " + 
									CommonMappingStateService.getState(mapName).selectedPolygon.length);
								
								for (var band in CommonMappingStateService.getState(mapName).areaNameList) {
									AlertService.consoleDebug("[rifd-dsub-maptable.js] areaNameList band: " + 
									band + "; " + CommonMappingStateService.getState(mapName).areaNameList[band].length + " areas");
//										": " + JSON.stringify(CommonMappingStateService.getState(mapName).areaNameList[band]));	
								}

								if (!shape.circle && !shape.shapefile) {
									removeMapDrawItems();
									//auto increase band dropdown
									if (CommonMappingStateService.getState(mapName).currentBand < 
									    Math.max.apply(null, CommonMappingStateService.getState(mapName).possibleBands)) {
										CommonMappingStateService.getState(mapName).currentBand++;
									}
								}
								
								if (bboxErrors > 0) {
									AlertService.showWarning(bboxErrors + " errors occurred create bounding boxes for shape");
								}
								if (excludedByBbox > 0) {
									AlertService.showWarning(excludedByBbox + " shapes would have been excluded by using a bounding box");
								}
								
								if (makeDrawSelectionCallback && typeof makeDrawSelectionCallback === "function") {
									makeDrawSelectionCallback();
								}
								else {
									throw new Error("No makeDrawSelectionCallback() function");
								}								
							} // End of latlngListEnd ()
							var itemsProcessed = 0;
							var bboxErrors = 0;
							var excludedByBbox = 0;
							var useAsync = false; // Do not use async version - it is much slower					
							addBbox().then(function(res) {
								if (useAsync) {
									async.eachOfSeries(latlngList, 
										function latlngListIteratee(latLng, indexKey, latlngListCallback) {
										if (indexKey == 0) {
											AlertService.consoleDebug("[rifd-dsub-maptable.js] async processing latLng " + indexKey + "/" + latlngList.length + 
												" to check if in shape: " + (shape.bbox ? JSON.stringify(shape.bbox) : "no bbox"));	
										}
										else if (indexKey % 500 == 0) {
											AlertService.consoleDebug("[rifd-dsub-maptable.js] async processing latLng " + indexKey + "/" + latlngList.length + 
												" to check if in shape: " + (shape.bbox ? JSON.stringify(shape.bbox) : "no bbox"));									
											$timeout(function() { // Be nice to the stack if you are going to be aggressive!
												latlngListFunction(latLng, latlngListCallback);
											}, 100);
										}	
										else if (indexKey % 50 == 0) {
											async.setImmediate(function() { // Be nice to the stack if you are going to be aggressive!
												latlngListFunction(latLng, latlngListCallback);
											});
										}
										else {
											latlngListFunction(latLng, latlngListCallback);
										}
									}, function done(err) {
										if (err) {
											AlertService.showError("[rifd-dsub-maptable.js] latlngList error: " + err);
										}
										latlngListEnd();
									});	
								}		
								else { // Non async version
									for (var i=0; i<latlngList.length; i++) {
										if (i == 0) {
											AlertService.consoleDebug("[rifd-dsub-maptable.js] processing latLng " + i + "/" + latlngList.length + 
												" to check if in shape: " + (shape.bbox ? JSON.stringify(shape.bbox) : "no bbox"));	
										}
										else if (i % 500 == 0) {
											AlertService.consoleDebug("[rifd-dsub-maptable.js] processing latLng " + i + "/" + latlngList.length + 
												" to check if in shape: " + (shape.bbox ? JSON.stringify(shape.bbox) : "no bbox"));
										}												
										latlngListFunction(latlngList[i], undefined /* no latlngListCallback - not using async */);
									}
									latlngListEnd();
								}	
							}, function (err) {
								if (makeDrawSelectionCallback && typeof makeDrawSelectionCallback === "function") {
									makeDrawSelectionCallback(err);
								}
								else {
									throw new Error("No makeDrawSelectionCallback() function");
								}
							});								
							
							function latlngListFunction(latLng, latlngListCallbackFunction) { 
								// Check centroids of areas lie within the shape
								//is point in defined polygon?
								var test=false;
								
								if (shape.bbox && shape.bbox.isValid()) { // Use the bounding box first to eliminate most points
									if (shape.circle) {
										test = GISService.getPointincircle(latLng.latLng, shape);
									}
									else if (shape.bbox.contains(latLng.latLng)) { // This does NOT work!
										// Shape bounding box contains point
										test = GISService.getPointinpolygon(latLng.latLng, shape);
									}
									else { // Test above
										// This code should only be commented out when we are sure shape.bbox.contains(latLng.latLng) is working OK
										test = GISService.getPointinpolygon(latLng.latLng, shape);
										if (test) {	// It is in polygon, shape.bbox.contains(latLng.latLng) produce false when it should be true		
											excludedByBbox++;								
											AlertService.consoleError("[rifd-dsub-maptable.js] latlngListFunction bbox contains() failed to match point: " +
												"; latLng.latLng: " + JSON.stringify(latLng.latLng) +
												"; shape.bbox: " + shape.bbox.toBBoxString() +
												"; shape.bbox.isValid(): " + shape.bbox.isValid() +
												"; shape.bbox.contains(latLng.latLng)): " + shape.bbox.contains(latLng.latLng));
										}
										// Replace with:
										// excludedByBbox++;	0
									}
								}
								else if (shape.circle) {
									test = GISService.getPointincircle(latLng.latLng, shape);
								} else {
									test = GISService.getPointinpolygon(latLng.latLng, shape);
								}
								
								if (test) { // Intersects
									var thisLatLng = latLng.latLng;
									var thisPoly = latLng.name;
									var thisPolyID = latLng.id;
									var bFound = false;
									
//									AlertService.consoleDebug("[rifd-dsub-maptable.js] latlngList.forEach(" +
//										itemsProcessed + ")");
									// Selects the correct polygons
									if (CommonMappingStateService.getState(mapName).selectedPolygonObj[thisPolyID]) { // Found
										if (CommonMappingStateService.getState(mapName).selectedPolygonObj[thisPolyID].band == undefined ||
											CommonMappingStateService.getState(mapName).selectedPolygonObj[thisPolyID].band === -1) { 
													// If not set in concentric shapes
											if (latlngList[itemsProcessed].band == undefined ||
												latlngList[itemsProcessed].band === -1) { 
													// If not set on map
												if (shape.band === -1) {  // Set band
													CommonMappingStateService.getState(mapName).selectedPolygonObj[thisPolyID].band=CommonMappingStateService.getState(mapName).currentBand;
												}
												else if (CommonMappingStateService.getState(mapName).selectedPolygonObj[thisPolyID].band > 0 &&
													CommonMappingStateService.getState(mapName).selectedPolygonObj[thisPolyID].band < shape.band) {  // Do not set band
													CommonMappingStateService.getState(mapName).selectedPolygonObj[thisPolyID].band=CommonMappingStateService.getState(mapName).currentBand;
												}
												else {
													CommonMappingStateService.getState(mapName).selectedPolygonObj[thisPolyID].band=shape.band;
												}		
											}
										}
										bFound = true;
									}
																			
//										AlertService.consoleDebug("[rifd-dsub-maptable.js] latlngList.forEach(" +
//											itemsProcessed + ") bFound: " + bFound + 
//											"; latLng: " + JSON.stringify(latLng));
										
									if (!bFound) {
										if (shape.band === -1) {
											CommonMappingStateService.getState(mapName).selectedPolygon.push({
												id: thisPolyID, 
												gid: thisPolyID, 
												label: thisPoly, 
												band: CommonMappingStateService.getState(mapName).currentBand, 
												centroid: thisLatLng
											});
											CommonMappingStateService.getState(mapName).selectedPolygonObj[thisPolyID] = {
												id: thisPolyID, 
												gid: thisPolyID, 
												label: thisPoly, 
												band: CommonMappingStateService.getState(mapName).currentBand, 
												centroid: thisLatLng
											};
											latlngList[itemsProcessed].band=CommonMappingStateService.getState(mapName).currentBand;
										} else {
											CommonMappingStateService.getState(mapName).selectedPolygon.push({
												id: thisPolyID, 
												gid: thisPolyID, 
												label: thisPoly, 
												band: shape.band, 
												centroid: thisLatLng
											});
											CommonMappingStateService.getState(mapName).selectedPolygonObj[thisPolyID] = {
												id: thisPolyID, 
												gid: thisPolyID, 
												label: thisPoly, 
												band: shape.band, 
												centroid: thisLatLng
											};
											latlngList[itemsProcessed].band=shape.band;
										}
									}
								}
								itemsProcessed++;
								if (latlngListCallbackFunction && typeof latlngListCallbackFunction === "function") {
									latlngListCallbackFunction();
								}
								// Otherwise not using async
							}
							
							function removeMapDrawItems() {
								CommonMappingStateService.getState("areamap").drawnItems.clearLayers();
								CommonMappingStateService.getState("areamap").map.addLayer(CommonMappingStateService.getState("areamap").drawnItems);
								input.bDrawing = false; //re-enable layer events
							}					

                        } // End of makeDrawSelection2()
						
            return {
                //return the job submission as unformatted JSON
                makeDrawSelection: function (shape, selectorBands, input, mapName, latlngList, makeDrawSelectionCallback) {
                    return makeDrawSelection2(shape, selectorBands, input, mapName, latlngList, makeDrawSelectionCallback);
                }
            };
        }]);