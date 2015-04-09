RIF.map.layer.tilesvg = ( function () {
  var layer = this,
    tiled = {
      url: "backend/gets/getTiles.php",
      ids: {},
      firstLoad: true,
      init: function () {
        RIF.statusBar( "Rendering Map", true );
        var sett = {
            class: "polygon",
            evntHndl: this.evntHndl,
            id: tiled.getPathId,
            loaded: tiled.tilesLoaded,
            resetIds: tiled.resetIds,
            deduplicate: tiled.checkId,
            style: tiled.getStyle,
            getUrlFromTile: tiled.getUrlFromTile
          },
          registerTitles = function () {
            layer.hoverLbls = this;
            layer.add.tile( tiled.getLayer( sett ) );
            layer.highlight = tiled.highlight; // register select function					 
          };
        RIF.getSingleFieldData( registerTitles, [ layer.geoLevel, layer.selectionField ] );
      },
      getLayer: function ( sett ) {
        return new L.TileLayer.d3_geoJSON( layer.tilegeojsonUrl, sett );
      },
      getUrlFromTile: function ( tilePoint, zoom ) {
        var tileX = tilePoint.x,
          tileY = tilePoint.y,
          tileId = tileX + '_' + tileY,
          start = tiled.getTileBounds( tileX, tileY, zoom ),
          to = tiled.getTileBounds( tileX + 1, tileY + 1, zoom ),
          geolevel = layer.geoLevel,
          field = layer.selectionField,
          url = [ tiled.url + '?x=' + start[ 0 ],
            'y=' + Math.abs( start[ 1 ] ),
            'x2=' + to[ 0 ],
            'y2=' + Math.abs( to[ 1 ] ),
            'zoom=' + zoom,
            'tileId=' + tileId,
            'geolevel=' + geolevel,
            'field=' + field
          ].join( "&" );
        return url;
      },
      getTileBounds: function ( tilePointX, tilePointY, zoom ) {
        //get Mercator lat / lon coordinates from tile number
        var originShift = Math.PI * 6378137,
          initialRes = 2 * Math.PI * 6378137 / 256,
          px = tilePointX * 256,
          py = tilePointY * 256,
          res = initialRes / ( Math.pow( 2, zoom ) ),
          coords_x = px * res - originShift,
          coords_y = py * res - originShift,
          coords_x_4326 = coords_x / originShift * 180.0,
          coords_y_4326 = coords_y / originShift * 180.0,
          coords_y_4326 = 180 / Math.PI * ( 2 * Math.atan( Math.exp( coords_y_4326 * Math.PI / 180 ) ) - Math.PI / 2.0 );
        return [ coords_x_4326, coords_y_4326 ];
      },
      /*
       *  Should move highlight method to style.tilesvg
       */
      highlight: function ( id, slctd ) {
        var s = layer.getLayerStyle( id, slctd );
        d3.select( "#" + id ).style( {
          "fill": s.fill,
          "stroke": s.stroke,
          "stroke-width": s.stroke_width,
          "opacity": 1
        } )
      },
      getStyle: function ( d ) {
        var id = tiled.getPathId( d ),
          isSlctd = layer.isSlctd( id );
        if ( isSlctd ) {
          return "fill:" + layer.style.slctd.fill;
        }
        return layer.style.getStyle( id, "tilesvg" );
      },
      getPathId: function ( d ) {
        var id = RIF.addG( d.id ); //+ "__" + d.properties.field.trim();
        return id.replace( /\s+/g, '' );
      },
      getPathTitle: function ( d ) {
        return tiled.titles[ d.id ];
      },
      tilesLoaded: function () {
        if ( tiled.firstLoad ) {
          layer.clbk.tile();
          tiled.firstLoad = false;
        };
        RIF.statusBar( "Rendering Map", false );
      },
      resetIds: function () {
        tiled.ids = {};
        //RIF.statusBar( "Rendering Map", true );
      },
      checkId: function ( d ) {
        var id = tiled.getPathId( d );
        if ( typeof tiled.ids[ id ] === "undefined" ) {
          tiled.ids[ id ] = 1;
          return true;
        };
        return false;
      },
      evntHndl: function ( c ) {
        /* 'this' is the intialized layer's context */
        var isSlctd = layer.isSlctd( this.id );
        switch ( c ) {
        case "click":
          layer.slct( this.id, isSlctd );
          break;
        case "mouseout":
          if ( !isSlctd ) {
            this.style.fill = layer.style.colors[ this.id ];
          }
          layer.out.call( this );
          break;
        case "mouseover":
          var id = RIF.removeG( this.id ),
            label = layer.hoverLbls[ id ];
          layer.hover.call( this, label );
          if ( !isSlctd ) {
            this.style.fill = layer.style.hover.fill;
            return;
          }
          break;
        }
      }
    };
  return tiled.init();
} );