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
        .factory('GISService',
                function () {
                    function isPointInPolygon(point, poly) {
                        http://stackoverflow.com/questions/31790344/determine-if-a-point-reside-inside-a-leaflet-polygon
                        var polyPoints = poly.data.getLatLngs();
                        var x = point.lat;
                        var y = point.lng;
                        var inside = false;
                        for (var i = 0, j = polyPoints.length - 1; i < polyPoints.length; j = i++) {
                            var xi = polyPoints[i].lat, yi = polyPoints[i].lng;
                            var xj = polyPoints[j].lat, yj = polyPoints[j].lng;
                            var intersect = ((yi > y) !== (yj > y))
                                    && (x < (xj - xi) * (y - yi) / (yj - yi) + xi);
                            if (intersect)
                                inside = !inside;
                        }
                        return inside;
                    }
                    function isPointInCircle(point, circle) {
                        if (circle.data.getLatLng().distanceTo(point) <= circle.data.getRadius()) {
                            return true;
                        }
                    }
                    return {
                        getPointinpolygon: function (point, poly) {
                            return isPointInPolygon(point, poly);
                        },
                        getPointincircle: function (point, circle) {
                            return isPointInCircle(point, circle);
                        }
                    };
                });
