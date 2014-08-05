/*
 *  The Manager module allows you to visualize raw data
 *  with maps , charts and tables.
 *  Data can be exported as .shp , csv , GEOJSON.
 *  The main purpose of this module is to set event handlers
 *  and  relations between the modules involved in Manager.
 *
 *  @components
 *      list of modules dynamically initialized
 *  @events
 *      list of events occurring within the manager module
 *      ->firer:
 *          objects that will fire the specific event
 *      ->subscriber
 *          objects that will handle the event fired
 *      ->method
 *          method which must be implemented in subscriber object
 */
RIF.manager = (function () {

    var _p = {

        components: {
            sync: 'manager',
            map: { studyType: 'manager' , layerType: 'tilesvg'},
            table: {studyType: 'manager', tableType : 'click2row'},
            chart: { studyType: 'manager' , charts: ['pyramid','histogram']},
            menu:  { studyType: 'manager',  menus:[/*'export',*/ 'geoLevel', 'choropleth' , 'settings']},
			resizable: ''
        },

        events: {
            // sync
            selectionchange: {
                subscribers: ["sync"],
                firer: ["map", "table", "menu"],
                method: "uAreaSelection"
            },

            geolvlchange: {
                subscribers: ["sync"],
                firer: ["menu"],
                method: "cGeoLvl"
            },
						
			clearMapTable:{
			    subscribers: ["sync"],
                firer: ["menu"],
                method: "clearMapTable"
			},
			// Chart update 
			chartUpdateClick: {
                subscribers: ["sync"],
                firer: ["menu"],
                method: "chartUpdateClick"
            },
			// sync - charts
			updateCharts: {
                subscribers: ["chart"],
                firer: ["sync"],
                method: "updateCharts"
            },
			// sync - map/table
            clearSelection: {
                subscribers: ["map", "table"],
                firer: ["sync"],
                method: "clearSelection"
            },
			
            // sync - map
            updateSelectionMap: {
                subscribers: ["map"],
                firer: ["sync"],
                method: "updateSelection"
            },
			
			zoomToExtent: {
                subscribers: ["map"],
                firer: ["menu"],
                method: "zoomToExtent"
            },
			
			// sync - table
            updateSelectionTable: {
                subscribers: ["table"],
                firer: ["sync"],
                method: "updateSelection"
            },

            // Map	
            zoomToArea: {
                subscribers: ["map"],
                firer: ["menu"],
                method: "zoomTo"
            },
			
			addGeolevel: {
                subscribers: ["map"],
                firer: ["menu"],
                method: "uGeolevel"
            },
			
			hoverFieldChange: {
                subscribers: ["map"],
                firer: ["menu"],
                method: "uHoverField"
            },
			
			mapStyleChange: {
                subscribers: ["map"],
                firer: ["menu"],
                method: "uMapStyle"
            },
			
			editChoroplethBreaks: {
                subscribers: ["map"],
                firer: ["menu"],
                method: "editBreaks"
            },
			
			//Map - Resizable
			resizeMap: {
                subscribers: ["map"],
                firer: ["resizable"],
                method: "resizeMap"
            },

			//Map - Table
			addTabularData:{
				subscribers: ["table"],
                firer: ["menu"],
                method: "getTabularData"
			},
			
			populateMenus: {
                subscribers: ["menu"],
                firer: ["map"],
                method: "uDropdownFlds"
            },
			
			scaleRangeReady:{
			    subscribers: ["menu"],
                firer: ["map"],
                method: "getScaleRange"
			},
			
			//Resizable - Table
			resizeTable: {
				subscribers: ["table"],
                firer: ["resizable"],
                method: "resizeTable"
			},
			//Menu - Table 
			filterCols: {
				subscribers: ["table"],
                firer: ["menu"],
                method: "filterCols"
			},
			
			changeNumRows: {
				subscribers: ["table"],
                firer: ["menu"],
                method: "changeNumRows"
			},
			
            //Chart - Pyramid 	
            updatePyramid: {
                subscribers: ["chart"],
                firer: ["menu"],
                method: "updatePyramid"
            },

            changePyramidField: {
                subscribers: ["chart"],
                firer: ["menu"],
                method: "uPyramidField"
            },

            changePyramidYear: {
                subscribers: ["chart"],
                firer: ["menu"],
                method: "uPyramidYear"
            },

            //Chart - Distribution hist.
            updateHistogram: {
                subscribers: ["chart"],
                firer: ["menu"],
                method: "updateHistogram"
            },

            changeHistField: {
                subscribers: ["chart"],
                firer: ["menu"],
                method: "uDistHistField"
            },

            changeHistYear: {
                subscribers: ["chart"],
                firer: ["menu"],
                method: "uDistHistYear"
            }

        },

        init: function () {
            RIF.initComponents.call(this);
            RIF.addEvents.call(this);
        }

    };

    return {
        setUp: (function (args) {
            _p.init();
        }())
    };

});