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
RIF.diseaseMapping = (function () {

    var _p = {

        components: {
            sync: 'diseaseMapping',
            map: { studyType: 'diseaseMapping' , layerType: 'tilesvg'},
            //chart: ['bivariate'],
            menu: [/*'export',*/ 'diseaseStudyLevel', 'choropleth' /*, 'settings' */],
			resizable: ''
        },

        events: {
            // sync
            selectionchange: {
                subscribers: ["sync"],
                firer: ["map",  "menu"],
                method: "uAreaSelection"
            },

            geolvlchange: {
                subscribers: ["sync"],
                firer: ["menu"],
                method: "cGeoLvl"
            },
					
			// sync - map/table
            clearSelection: {
                subscribers: ["map"],
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

            //Chart - Distribution hist.
            /*updateHistogram: {
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
            }*/

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