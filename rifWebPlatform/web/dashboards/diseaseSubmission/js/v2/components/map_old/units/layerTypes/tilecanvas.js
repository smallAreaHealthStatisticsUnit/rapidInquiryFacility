RIF.map.layer.tilecanvas = ( function () {
  /*
   * [TODO] For slecting map areas must implement:
   * @highlight(id,scltd)
   * @slct(id)
   *
   */
  var layer = this,
    tiled = {
      url: "backend/gets/getTiles.php",
      firstLoad: true,
      init: function () {
        var sett = {
            class: "polygon",
            geolevel: layer.geoLevel,
            loaded: tiled.tilesLoaded,
            resetIds: tiled.resetIds,
            style: tiled.getStyle,
            url: tiled.createUrl
          },
          registerTitles = function () {
            layer.hoverLbls = this;
            layer.add.tile( tiled.getLayer( sett ) );
          };
        RIF.getSingleFieldData( registerTitles, [ layer.geoLevel, layer.selectionField ] );
      },
      getLayer: function ( sett ) {
        return new L.TileLayer.canvasTopojson( sett );
      },
      getStyle: function ( id ) {
        return layer.style.getStyle( id, "tilecanvas" );
      },
      tilesLoaded: function () {
        if ( tiled.firstLoad ) {
          layer.clbk.tile();
        }
        tiled.firstLoad = false;
      },
      createUrl: function ( bounds, tileX, tileY, zoom ) {
        var tileId = tileX + '_' + tileY,
          url = [ tiled.url + '?x=' + bounds[ 0 ],
            'y=' + bounds[ 1 ],
            'x2=' + bounds[ 2 ],
            'y2=' + bounds[ 3 ],
            'zoom=' + zoom,
            'tileId=' + tileId,
            'geolevel=' + layer.geoLevel,
            'field=' + layer.selectionField
          ].join( "&" );
        return url;
      }
    };
  return tiled.init();
} );