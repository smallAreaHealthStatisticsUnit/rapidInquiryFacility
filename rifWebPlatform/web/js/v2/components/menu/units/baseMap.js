RIF.menu.baseMap = ( function() {

  var parent = this,

    _domObjects = {
      /* DOM elements */
      baseMapObj: $( '#baseMapBtn' ),
      saveObj: $( '#savebasemap' ),
      baseMapFieldSelection: $( '#baseMapSlct' ),
      nobasemap: $( '#nobasemap' ),
      basemapselector: $( '#basemapselector' )
    },

    /* events */
    _events = function() {

      _domObjects.baseMapObj.click( function() {
        $( "#baseMap" ).show();
      } );

      _domObjects.saveObj.click( function() {
        $( '#baseMap' ).hide();
        parent.facade.baseMapChanged( _getBaseMapSelected() );
      } );

      _domObjects.nobasemap.change( function() {
        if ( _isBaseMapChecked() ) {
          _enableDisable( '#EDEDED', true );
          return;
        };
        _enableDisable( '#F7F7F7', false );
      } );

    },

    _getBaseMapSelected = function() {
      if ( _isBaseMapChecked() ) {
        return '';
      };
      var m = _domObjects.baseMapFieldSelection.find( ":selected" ).val();
      return _baseMaps[ m ];
    },

    _isBaseMapChecked = function() {
      return _domObjects.nobasemap.is( ":checked" );
    },

    _enableDisable = function( color, disableEnable ) {
      _domObjects.baseMapFieldSelection.attr( "disabled", disableEnable );
      _domObjects.baseMapFieldSelection.css( 'background', color );
      _domObjects.basemapselector.css( 'background', color );
    },

    _baseMapsName = [ 'osm', 'osm_hot', 'hydda_base', 'stamen_toner', 'stamen_toner_background', 'stamen_water_color',
                       'esri_world_topo_map', 'esri_world_terrain', 'esri_world_gray_canvas', 'Esri_WorldImagery' ],

    _baseMaps = {
      osm: "http://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png",
      osm_hot: "http://{s}.tile.openstreetmap.fr/hot/{z}/{x}/{y}.png",
      hydda_base: 'http://{s}.tile.openstreetmap.se/hydda/base/{z}/{x}/{y}.png',
      stamen_toner: 'http://{s}.tile.stamen.com/toner/{z}/{x}/{y}.png',
      stamen_toner_background: 'http://{s}.tile.stamen.com/toner-background/{z}/{x}/{y}.png',
      stamen_water_color: 'http://{s}.tile.stamen.com/watercolor/{z}/{x}/{y}.png',
      esri_world_topo_map: 'http://server.arcgisonline.com/ArcGIS/rest/services/World_Topo_Map/MapServer/tile/{z}/{y}/{x}',
      esri_world_terrain: 'http://server.arcgisonline.com/ArcGIS/rest/services/World_Terrain_Base/MapServer/tile/{z}/{y}/{x}',
      esri_world_gray_canvas: 'http://server.arcgisonline.com/ArcGIS/rest/services/World_Terrain_Base/MapServer/tile/{z}/{y}/{x}',
      Esri_WorldImagery: 'http://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}'
    },

    /* baseMap obj */
    _p = {

      initBaseMap: function() {
        _events();
        _enableDisable( '#EDEDED', true );
        parent.dropDown( _baseMapsName, _domObjects.baseMapFieldSelection );
      },

      getBaseMapMenuDom: function( obj ) {
        return _domObjects[ obj ];
      }

    };

  _p.initBaseMap();

  return _p;
} );