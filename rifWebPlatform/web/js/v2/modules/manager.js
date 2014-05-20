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
            study: 'manager',
            map: 'tilesvg',
            table: 'click2row',
            chart: ['pyramid', 'histogram'],
            menu: [/*'export',*/ 'geoLevel', 'choropleth' , 'settings'],
			resizable: ''
        },

        events: {
            // Study
            selectionchange: {
                subscribers: ["study"],
                firer: ["map", "table", "menu"],
                method: "uAreaSelection"
            },

            geolvlchange: {
                subscribers: ["study"],
                firer: ["menu"],
                method: "cGeoLvl"
            },
			
            // Map/Table
            updateSelection: {
                subscribers: ["map", "table"],
                firer: ["study"],
                method: "addSelection"
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
                firer: ["map"],
                method: "getTabularData"
			},
			
			//Map - Menu
			addZoomIdentifiers: {
                subscribers: ["menu"],
                firer: ["map"],
                method: "uZoomOpts"
            },
			
			addAvlbFields: {
                subscribers: ["menu"],
                firer: ["map"],
                method: "uAvlbFlds"
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
			
            //Chart - Pyramid 	
            updatePyramidWithSelection: {
                subscribers: ["chart"],
                firer: ["menu"],
                method: "uPyramid"
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
            updateHistWithSelection: {
                subscribers: ["chart"],
                firer: ["menu"],
                method: "uDistHist"
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