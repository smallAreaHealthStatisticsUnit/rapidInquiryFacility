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
 
 * David Morley
 * @author dmorley
 */

/*
 * SERVICE for basic GIS operations
 */
/* global L */

angular.module("RIF")
        .factory('GISService', ['AlertService',
                function (AlertService) {
                    function isPointInPolygon(point, poly) {
                        //http://stackoverflow.com/questions/31790344/determine-if-a-point-reside-inside-a-leaflet-polygon

                        //the hull
                        var polyPoints;
                        var xi;
                        var xj;
                        var yi;
                        var yj;
                        if (poly.freehand) {
                            polyPoints = poly.data.getLatLngs();
                        } else {
                            polyPoints = poly.data.getLatLngs()[0];
                        }

                        //the centroid
                        var x = point.lat;
                        var y = point.lng;

                        //the test
                        var inside = false;
                        for (var i = 0, j = polyPoints.length - 1; i < polyPoints.length; j = i++) {
                            if (poly.freehand) {
                                xi = polyPoints[i].lat;
                                yi = polyPoints[i].lng;
                                xj = polyPoints[j].lat;
                                yj = polyPoints[j].lng;
                            } else {
                                //Shp Library inverts lat, lngs for some reason (Bug?) - switch 
                                xi = polyPoints[i].lng;
                                yi = polyPoints[i].lat;
                                xj = polyPoints[j].lng;
                                yj = polyPoints[j].lat;
                            }
                            var intersect = ((yi > y) !== (yj > y)) && (x < (xj - xi) * (y - yi) / (yj - yi) + xi);
                            if (intersect) {
                                inside = !inside;
                            }
                        }
                        return inside;
                    }
                    function isPointInCircle(point, circle) {
                        if (circle.data.getLatLng().distanceTo(point) <= circle.data.getRadius()) {
                            return true;
						}
						else {
							return false;
						}
                    }
					function convertToGeoJSONPoints(LatLngList) {
						var geoJSONPoints = [];
						for (var i=0; i<LatLngList.length; i++) {
							geoJSONPoints.push([LatLngList[i].lng, LatLngList[i].lat]);
						}
						if (LatLngList[0].lng == LatLngList[(LatLngList.length-1)].lng &&
						    LatLngList[0].lng == LatLngList[(LatLngList.length-1)].lng) { // Is a ring
						}
						else {
							geoJSONPoints.push([LatLngList[0].lng, LatLngList[0].lat]); // Make it a ring
						}
						return [geoJSONPoints];
					}
					function getdistanceFromNearestSource2(point, centroid) {
						// Point is Leaflet latLng
                        if (centroid && point) {
							return Math.round(centroid.distanceTo(point) * 10)/10;
						}
						else {
							throw new Error("centroid/point is undefined");
						}
					}
					function getCentroid2(shape) {
						// Point is Leaflet latLng
                        if (shape.circle) {
							return shape.data.getLatLng();
                        }
						else if (shape.data) {	
							var polyPoints;
							if (shape.freehand) {
								polyPoints = convertToGeoJSONPoints(shape.data.getLatLngs());
							} else {
								polyPoints = convertToGeoJSONPoints(shape.data.getLatLngs()[0]);
							}		
							var polygon=turf.polygon(polyPoints);
							var centroid;
							if (polygon) {
								centroid=turf.centroid(polygon); // GeoJSON point as longitude and latitude
								return L.latLng(centroid.geometry.coordinates[1], centroid.geometry.coordinates[0]);
							}
							else {
								throw new Error("No polygon could be created for polyPoints: " + JSON.stringify(polyPoints));
							}
//							AlertService.consoleDebug("rifs-util-gis.js] polyPoints: " + JSON.stringify(polyPoints) +
//								"; polygon: " + JSON.stringify(polygon) +
//								"; centroid: " + JSON.stringify(centroid)); 
						}
						else {
							throw new Error("shape.data is undefined; shape: " + JSON.stringify(shape.data, null, 1));
						}
					}
                    return {
                        getPointinpolygon: function (point, poly) {
                            return isPointInPolygon(point, poly);
                        },
                        getPointincircle: function (point, circle) {
                            return isPointInCircle(point, circle);
                        },
						getdistanceFromNearestSource: function (point, centroid) {
                            return getdistanceFromNearestSource2(point, centroid);
                        },
						getCentroid: function(shape) {
							return getCentroid2(shape);
						}
                    };
                }]);
