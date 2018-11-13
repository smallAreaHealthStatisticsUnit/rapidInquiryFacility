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
 * SERVICE to extend leafletdraw library
 */

/* global L, key, topojson */
/*
 * From leaflet.draw github
 * Adapt Circle.draw method for concentric circles
 * Adapt Polygon.draw methods for freehand polygons
 */

angular.module("RIF")
        .factory('LeafletDrawService', ['$rootScope', 'ParametersService', 'DrawSelectionService', 'CommonMappingStateService',
                function ($rootScope, ParametersService, DrawSelectionService, CommonMappingStateService) {
					var parameters=ParametersService.getParameters();
                    var selectorBands = { // Study and comparison are selectors
							weight: 3,
							opacity: 0.8,
							fillOpacity: 0,
							bandColours: ['#e41a1c', '#377eb8', '#4daf4a', '#984ea3', '#ff7f00', '#ffff33']
						};
					if (parameters && parameters.selectorBands) {
						selectorBands=parameters.selectorBands
					}			
					
                    function extendLeafletDrawCircle() {

                        //increment of band count, 1st band is #1, to a max of 6
                        var thisBand = 1;

                        L.SimpleShape = {};
                        L.Draw.SimpleShape = L.Draw.Feature.extend({
                            options: {
                                repeatMode: false
                            },
                            initialize: function (map, options) {
                                this._endLabelText = L.drawLocal.draw.handlers.simpleshape.tooltip.end;

                                L.Draw.Feature.prototype.initialize.call(this, map, options);
                            },
                            addHooks: function () {
                                L.Draw.Feature.prototype.addHooks.call(this);
                                if (this._map) {
                                    this._mapDraggable = this._map.dragging.enabled();

                                    if (this._mapDraggable) {
                                        this._map.dragging.disable();
                                    }

                                    this._container.style.cursor = 'crosshair';

                                    this._tooltip.updateContent({text: this._initialLabelText});

                                    this._map
                                            .on('mousedown', this._onMouseDown, this)
                                            .on('mousemove', this._onMouseMove, this)
                                            .on('touchstart', this._onMouseDown, this)
                                            .on('touchmove', this._onMouseMove, this);
                                }
                            },
                            removeHooks: function () {
                                L.Draw.Feature.prototype.removeHooks.call(this);
                                if (this._map) {
                                    if (this._mapDraggable) {
                                        this._map.dragging.enable();
                                    }

                                    this._container.style.cursor = '';

                                    this._map
                                            .off('mousedown', this._onMouseDown, this)
                                            .off('mousemove', this._onMouseMove, this)
                                            .off('touchstart', this._onMouseDown, this)
                                            .off('touchmove', this._onMouseMove, this);

                                    // If the box element doesn't exist they must not have moved the mouse, so don't need to destroy/return
                                    if (this._shape) {
                                        this._map.removeLayer(this._shape);
                                        delete this._shape;
                                    }
                                }
                                this._isDrawing = false;
                            },
                            _getTooltipText: function () {
                                return {
                                    text: this._endLabelText
                                };
                            },
                            _onMouseDown: function (e) {

                                //add circles 
                                this._isDrawing = true;
								var rifShapeId= CommonMappingStateService.getState("areamap").getNextShapeId();
								var rifShapePolyId= CommonMappingStateService.getState("areamap").getNextShapePolyId();
								
                                //fire new circle at same centre
                                if (!this._isConcentricing) {
                                    this._startLatLng = e.latlng;
                                    this._isConcentricing = true;
                                }
                                //Draw a single circle only (disease mapping comparison area)
                                if (this._map.band === 1) {
									
                                    this._map.addLayer(this._shape);
                                    this._fireCreatedEvent();
                                    if (this._shape) {
                                        this._fireCreatedEvent();
                                        //fire event in directive     
                                        $rootScope.$broadcast('makeDrawSelection2', {
                                            data: this._shape,
                                            circle: true,
                                            freehand: false,
                                            band: thisBand,
											finalCircleBand: true,
											rifShapeId: rifShapeId,
											rifShapePolyId: rifShapePolyId
                                        });
                                    }
                                    this.disable();
                                    if (this.options.repeatMode) {
                                        this.enable();
                                    }
                                    this._isConcentricing = false;
                                    //remove shape
                                    this.maxRadius.r = -1;
                                    //make selection
                                    $rootScope.$broadcast('removeDrawnItems');
                                    return;
                                }

                                //Multiple bands (study area)
                                //stop drawing on right click or if band = max
                                if (e.originalEvent.buttons === 2 | thisBand === this._map.band) {
                                    if (this._shape) {
                                        this._fireCreatedEvent();
                                        //fire event in directive     
                                        $rootScope.$broadcast('makeDrawSelection2', {
                                            data: this._shape,
                                            circle: true,
                                            freehand: false,
                                            band: thisBand,
											finalCircleBand: true,
											rifShapeId: rifShapeId,
											rifShapePolyId: rifShapePolyId
                                        });
                                    }
                                    this.disable();
                                    if (this.options.repeatMode) {
                                        this.enable();
                                    }
                                    this._isConcentricing = false;

                                    //remove shape
                                    this.maxRadius.r = -1;
                                    thisBand = 1;

                                    //make selection
                                    $rootScope.$broadcast('removeDrawnItems');
                                } else {
                                    this._map.addLayer(this._shape);
                                    this._fireCreatedEvent();

                                    //fire event in directive
                                    $rootScope.$broadcast('makeDrawSelection2', {
                                        data: this._shape,
                                        circle: true,
                                        freehand: false,
                                        band: thisBand,
										rifShapeId: rifShapeId,
										rifShapePolyId: rifShapePolyId
                                    });
                                    //increase band number
                                    thisBand++;
                                }
                            },
                            _onMouseMove: function (e) {
                                var latlng = e.latlng;

                                this._tooltip.updatePosition(latlng);
                                if (this._isDrawing) {
                                    this._tooltip.updateContent(this._getTooltipText());
                                    this._drawShape(latlng);
                                }
                            }
                        });

                        L.Draw.Circle = L.Draw.SimpleShape.extend({
                            maxRadius: {
                                r: -1 //to ensure concentric bands always get bigger
                            },
                            statics: {
                                TYPE: 'circle'
                            },
                            options: {
                                shapeOptions: {
                                    stroke: true,
                                    color: '#0099cc',
                                    weight: 4,
                                    opacity: 1,
                                    fill: false,
                                    fillOpacity: 0.2,
                                    clickable: true
                                },
                                showRadius: true,
                                metric: true, // Whether to use the metric meaurement system or imperial
                                feet: true // When not metric, use feet instead of yards for display
                            },
                            initialize: function (map, options) {
                                // Save the type so super can fire, need to do this as cannot do this.TYPE :(
                                this.type = L.Draw.Circle.TYPE;
                                this._initialLabelText = L.drawLocal.draw.handlers.circle.tooltip.start;
                                L.Draw.SimpleShape.prototype.initialize.call(this, map, options);
                            },
                            _drawShape: function (latlng) {
                                this.options.shapeOptions.color = selectorBands.bandColours[thisBand - 1];
                                if (!this._shape) {
                                    this._shape = new L.Circle(this._startLatLng, this._startLatLng.distanceTo(latlng), this.options.shapeOptions);
                                    this._map.addLayer(this._shape);
                                } else {
                                    //ensure bands always increase in radius
                                    if (this._startLatLng.distanceTo(latlng) > this.maxRadius.r) {
                                        this._shape.setRadius(this._startLatLng.distanceTo(latlng));
                                    } else {
                                        this._shape.setRadius(this.maxRadius.r);
                                    }
                                }
                            },
                            _fireCreatedEvent: function () {
                                var circle = new L.Circle(this._startLatLng, this._shape.getRadius(), this.options.shapeOptions);                                                           
                                L.Draw.SimpleShape.prototype._fireCreatedEvent.call(this, circle);
                                //set this radius as the new minimum for new bands
                                this.maxRadius.r = this._shape.getRadius();
                            },
                            _onMouseMove: function (e) {
                                var latlng = e.latlng,
                                        showRadius = this.options.showRadius,
                                        useMetric = this.options.metric,
                                        radius;

                                this._tooltip.updatePosition(latlng);
                                if (this._isDrawing) {
                                    this._drawShape(latlng);
                                    // Get the new radius (rounded to 1 dp)
                                    radius = this._shape.getRadius().toFixed(1);
                                    this._tooltip.updateContent({
                                        text: "Left click to add band, Right click to finish",
                                        subtext: showRadius ? L.drawLocal.draw.handlers.circle.radius + ': ' +
                                                L.GeometryUtil.readableDistance(radius, useMetric, this.options.feet) : ''
                                    });
                                }
                            }
                        });
                    }

                    function extendLeafletDrawPolygon() {
                        L.Draw.Polygon = L.Draw.Polyline.extend({
                            statics: {
                                TYPE: 'polygon'
                            },
                            Poly: L.Polygon,
                            options: {
                                showArea: false,
                                shapeOptions: {
                                    stroke: true,
                                    color: '#0099cc',
                                    weight: 4,
                                    opacity: 0.5,
                                    fill: true,
                                    fillColor: null, //same as color by default
                                    fillOpacity: 0.2,
                                    clickable: true
                                }
                            },
                            initialize: function (map, options) {
                                L.Draw.Polyline.prototype.initialize.call(this, map, options);

                                // Save the type so super can fire, need to do this as cannot do this.TYPE :(
                                this.type = L.Draw.Polygon.TYPE;
                            },
                            _updateFinishHandler: function () {
                                var markerCount = this._markers.length;

                                // The first marker should have a click handler to close the polygon
                                if (markerCount === 1) {
                                    this._markers[0].on('click', this._finishShape, this);
                                }

                                // Add and update the double click handler
                                if (markerCount > 2) {
                                    this._markers[markerCount - 1].on('dblclick', this._finishShape, this);
                                    // Only need to remove handler if has been added before
                                    if (markerCount > 3) {
                                        this._markers[markerCount - 2].off('dblclick', this._finishShape, this);
                                    }
                                }
                            },
                            _getTooltipText: function () {
                                var text, subtext;

                                if (this._markers.length === 0) {
                                    text = L.drawLocal.draw.handlers.polygon.tooltip.start;
                                } else if (this._markers.length < 3) {
                                    text = L.drawLocal.draw.handlers.polygon.tooltip.cont;
                                } else {
                                    text = L.drawLocal.draw.handlers.polygon.tooltip.end;
                                    subtext = this._getMeasurementString();
                                }

                                return {
                                    text: text,
                                    subtext: subtext
                                };
                            },
                            _getMeasurementString: function () {
                                var area = this._area;

                                if (!area) {
                                    return null;
                                }

                                return L.GeometryUtil.readableArea(area, this.options.metric);
                            },
                            _shapeIsValid: function () {
                                return this._markers.length >= 3;
                            },
                            _vertexChanged: function (latlng, added) {
                                var latLngs;

                                // Check to see if we should show the area
                                if (!this.options.allowIntersection && this.options.showArea) {
                                    latLngs = this._poly.getLatLngs();

                                    this._area = L.GeometryUtil.geodesicArea(latLngs);
                                }

                                L.Draw.Polyline.prototype._vertexChanged.call(this, latlng, added);
                            },
                            _cleanUpShape: function () {
                                var markerCount = this._markers.length;

                                if (markerCount > 0) {
                                    this._markers[0].off('click', this._finishShape, this);

                                    if (markerCount > 2) {
                                        this._markers[markerCount - 1].off('dblclick', this._finishShape, this);

                                        //Make selection from polygon
                                        $rootScope.$broadcast('makeDrawSelection2', {
                                            data: this._poly,
                                            circle: false,
                                            freehand: true,
                                            band: -1
                                        });
                                    }
                                }
                            }
                        });
                    }

                    return {
                        getCircleCapability: function () {
                            return extendLeafletDrawCircle();
                        },
                        getPolygonCapability: function () {
                            return extendLeafletDrawPolygon();
                        }
                    };
                }]);         