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
RIF.diseaseMapping = ( function() {

  var _p = {

    components: {
      study: 'diseaseMapping',
      sync: 'diseaseMapping',
      map: {
        studyType: 'diseaseMapping',
        layerType: 'tilesvg'
      },
      chart: {
        studyType: 'diseaseMapping',
        charts: [ 'line_bivariate', 'multipleAreaCharts' ]
      },
      menu: {
        studyType: 'diseaseMapping',
        menus: [ /*'export',*/ 'diseaseStudyLevel', 'choropleth', 'settings' ]
      },
      resizable: 'diseaseMapping'
    },

    events: {
      /**********/
      /**Study**/
	  
	  resultSetSelectionChanged: {
	    subscribers: [ "study" ],
        firer: [ "menu" ],
        method: "resultSetSelectionChanged"
	  },
	  
	  changeResultSetSelection: {
	    subscribers: [ "chart" ],
        firer: [ "study" ],
        method: "changeResultSetSelection"
	  },
	  
	  clearSelectionClicked: {
        subscribers: [ "study" ],
        firer: [ "menu" ],
        method: "clearSelectionClicked"
      },
	  
	  clearSelection: {
        subscribers: [ "map" /*, "chart" */ ],
        firer: [ "study" ],
        method: "clearSelection"
      },
	  
      menusReady: {
        subscribers: [ "study" ],
        firer: [ "menu" ],
        method: "menusReady"
      },

      addGeolevel: {
        subscribers: [ "map" ],
        firer: [ "study" ],
        method: "uGeolevel"
      },

      drawLineBivariateChart: {
        subscribers: [ "chart" ],
        firer: [ "study" ],
        method: "updateLineBivariate"
      },

      drawMultipleAreaCharts: {
        subscribers: [ "chart" ],
        firer: [ "study" ],
        method: "updateMultipleAreaCharts"
      },

      areaChartBrushed: {
        subscribers: [ "study" ],
        firer: [ "chart" ],
        method: "areaChartBrushed"
      },

      updateLineChartWithBrush: {
        subscribers: [ "chart" ],
        firer: [ "study" ],
        method: "updateLineChartWithBrush"
      },

      hoverFieldChange: {
        subscribers: [ "study" ],
        firer: [ "menu" ],
        method: "uHoverField"
      },

      hoverFieldChangeApply: {
        subscribers: [ "map" ],
        firer: [ "study" ],
        method: "uHoverField"
      },

      /**********/
      /** Sync **/
      selectionchange: {
        subscribers: [ "sync" ],
        firer: [ "map", "menu" ],
        method: "uAreaSelection"
      },

      geolvlchange: {
        subscribers: [ "sync" ],
        firer: [ "menu" ],
        method: "cGeoLvl"
      },

      mapAreaFromAreaChartChange: {
        subscribers: [ "sync" ],
        firer: [ "chart" ],
        method: "mapAreaFromAreaChartChange"
      },

      slctMapAreaFromAreaChart: {
        subscribers: [ "map" ],
        firer: [ "sync" ],
        method: "slctMapAreaFromAreaChart"
      },

      /**********/
      /** Map **/
	  
     // SYNC is not needed for disease mapping
	 
	 /* clearSelection: {
        subscribers: [ "map" ],
        firer: [ "sync" ],
        method: "clearSelection"
      },*/

      updateSelectionMap: {
        subscribers: [ "map" ],
        firer: [ "sync" ],
        method: "updateSelection"
      },

      zoomToExtent: {
        subscribers: [ "map" ],
        firer: [ "menu" ],
        method: "zoomToExtent"
      },

      zoomToArea: {
        subscribers: [ "map" ],
        firer: [ "menu", "chart" ],
        method: "zoomTo"
      },

      mapStyleChange: {
        subscribers: [ "map" ],
        firer: [ "menu" ],
        method: "uMapStyle"
      },

      editChoroplethBreaks: {
        subscribers: [ "map" ],
        firer: [ "menu" ],
        method: "editBreaks"
      },

      //Map - Resizable
      resizeMap: {
        subscribers: [ "map" ],
        firer: [ "resizable" ],
        method: "resizeMap"
      },

      addResizableChart: {
        subscribers: [ "resizable" ],
        firer: [ "chart" ],
        method: "rr_chart"
      },

      addResizableAreaCharts: {
        subscribers: [ "resizable" ],
        firer: [ "chart" ],
        method: "multipleAreaCharts"
      },

      resizeLineBivariateChart: {
        subscribers: [ "chart" ],
        firer: [ "resizable" ],
        method: "refreshLineBivariate"
      },

      resizeAreaCharts: {
        subscribers: [ "chart" ],
        firer: [ "resizable" ],
        method: "refreshMultipleArea"
      },

      populateMenus: {
        subscribers: [ "menu" ],
        firer: [ "map" ],
        method: "uDropdownFlds"
      },

      scaleRangeReady: {
        subscribers: [ "menu" ],
        firer: [ "map" ],
        method: "getScaleRange"
      }

    },

    init: function() {
      RIF.initComponents.call( this );
      RIF.addEvents.call( this );
    }

  };

  return {
    setUp: ( function( args ) {
      _p.init();
    }() )
  };

} );