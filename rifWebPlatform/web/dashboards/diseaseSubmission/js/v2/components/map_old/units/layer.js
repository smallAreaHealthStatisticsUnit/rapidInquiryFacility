/*  context passed by calling object 
 *  type: "geojson"| "tilegeojson"| "topojson"| "tiletopojson",
 *
 *   Layer Types must implement the following methods:
 *
 *   @highlight(id,scltd) // allows to select a map area
 *   @slct(id) // selects an area and call highlight method
 */
RIF.map.layer = ( function ( type, sett ) {
  var map = this,

    layer = RIF.utils.mix( RIF.map.layer.settings( sett, type ), RIF.map.layer.hover(), {

      selection: {},
      hoverLbls: {}, // selection field

      init: function ( layerType ) {
        if ( sett.study === "diseaseMapping" ) {
          layer.applyDefaultChoro( layerType );
        } else {
          layer.initLayerType( layerType );
        }
      },
      initLayerType: function ( layerType ) {
        RIF.map.layer[ layerType ].call( layer );
      },
      add: {
        tile: function ( myLyr ) {
          layer.mylyr = myLyr;
          map.addTiled( layer.mylyr, layer.geoLevel );
          console.log( "tile added" )
        },
        geojson: function () {},
        topojson: function () {}
      },
      clbk: { /* called after layer is rendered */
        tile: function () {
          map.facade.populateMenus( {
            geoLvl: layer.geoLevel
          } );
        },
        topojson: function () {}
      },
      joinField: function ( field ) {
        var join = function () {
          layer.hoverLbls = this;
        };
        layer.selectionField = field || layer.selectionField;
        RIF.getSingleFieldData( join, [ map.getDataset(), layer.selectionField ] );
      },
      setTransparency: function ( val ) {
        this.style.setTransparency( val );
      },
      applyDefaultChoro: function ( layerType ) {
        var params = RIF.utils.extend( {
          field: sett.field
        }, layer.style.defaultChoro );
        var doChoro = function () {
          layer.style.setChoropleth( this, params, true );
          layer.style.updateColors( this );
          layer.initLayerType( layerType );
          layer.joinField( params.field );
        };
        RIF.getSingleFieldChoro( doChoro, [ map.getDataset(), params.field ] )
      },
      uStyle: function ( params ) { /* {classification: , colorScale: , field: , intervals:  }  */
        if ( params.intervals === 1 ) {
          this.style.setSingleColor( params );
          this.clearLegend();
          return;
        };
        var doChoro = function () {
          layer.style.setChoropleth( this, params, true );
          layer.style.updateColors( this );
          layer.repaintSlctd();
        };
        RIF.getSingleFieldChoro( doChoro, [ map.getDataset(), params.field ] );
      },
      getBreaks: function ( params ) {
        var getScale = function () {
          layer.style.setChoropleth( this, params, false );
          map.facade.scaleRange( layer.style.breaks );
        };
        RIF.getSingleFieldChoro( getScale, [ map.getDataset(), params.field ] )
      },
      resetSlctd: function () {
        /*if (!layer.isTiled()) {
			return;
		  }*/
        for ( var key in layer.selection ) {
          var e = $( "#" + key );
          e[ 0 ].style.fill = style.colors[ key ];
        }
      },
      repaintSlctd: function () {
        for ( var key in this.selection ) {
          layer.highlight( key );
        };
      },
      isSlctd: function ( id ) {
        if ( this.selection[ id ] === undefined ) {
          return false;
        }
        return true;
      },
      slct: function ( id ) {
        this.addRemoveId( id );
        this.selectionChanged()
        this.highlight( id );
      },
      slctNoPropagation: function ( id ) {
        this.addRemoveId( id );
        this.highlight( id );
      },
      addRemoveId: function ( id ) {
        if ( typeof this.selection[ id ] === 'undefined' ) {
          this.selection[ id ] = 1;
        } else {
          delete this.selection[ id ];
        }
      },
      selectAreas: function ( ids ) {
        var l = ids.length;
        while ( l-- ) {
          var id = "g" + ids[ l ];
          this.slct( id );
        }
      },
      getLayerStyle: function ( id, slctd ) {
        var isSlctd = slctd || this.isSlctd( id );
        return {
          fill: ( isSlctd ) ? layer.style.slctd.fill : layer.style.colors[ id ],
          stroke: ( isSlctd ) ? layer.style.slctd.stroke : layer.style.default.stroke,
          stroke_width: layer.style.slctd[ "stroke-width" ]
        }
      },
      clearLegend: function () {
        $( '.map-legend' ).empty();
      },
      clearSelection: function () {
        this.selection = [];
        this.resetSlctd();
        this.style.repaint();
      },
      setSelection: function ( selectionFromStudy ) {
        this.selection = selectionFromStudy;
      },
      selectionChanged: function () {
        var selection = [];
        for ( var key in layer.selection ) {
          var key = key.substring( 1 ); // remove 'g' from id
          selection.push( key );
        }
        map.facade.selectionChanged( selection );
      }
    } );

  layer.init( type );

  return layer;
} );