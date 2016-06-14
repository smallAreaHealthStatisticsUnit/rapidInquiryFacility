/* global L 
 * SERVICE for basic GIS operations
 * Point in polygon, Point in circle
 * Used by LeafletDraw selection tools
 * 
 * */

angular.module("RIF")
        .factory('GISService',
                function () {
                    function isPointInPolygon(point, poly) {
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
                        get_pointinpolygon: function (point, poly) {
                            return isPointInPolygon(point, poly);
                        },
                        get_pointincircle: function (point, circle) {
                            return isPointInCircle(point, circle);
                        }
                    };
                });
