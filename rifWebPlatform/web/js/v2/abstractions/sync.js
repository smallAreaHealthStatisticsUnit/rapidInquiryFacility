RIF.sync = ( function( type ) {

  var _shared = {

      currentGeoLvl: "",

      setSelection: function( slct ) {
        _study[ type ].selection = slct;
      },

      getSelection: function() {
        return _study[ type ].selection;
      },

      clear: function() {
        _shared.setSelection( [] );
        _study[ type ].fire( 'clearSelection', [] );
      }

    },

    _study = {

      manager: {
        selection: [],
        uAreaSelection: function( params ) {
          /*
           * Area ids are in the following format: g + id , example: "g101"
           * params[0]: ids
           * params[1]: firer
           */
          var newlySlctd = params[ 0 ];
          if ( RIF.arraysEqual( newlySlctd, _study[ type ].selection ) ) {
            return;
          };

          _shared.setSelection( RIF.unique( newlySlctd ) );

          if ( params[ 1 ] === 'table' ) {
            _study[ type ].fire( 'updateSelectionMap', _study[ type ].selection );
          };

          if ( params[ 1 ] === 'map' ) {
            _study[ type ].fire( 'updateSelectionTable', _study[ type ].selection );
          };
        },

        chartUpdateClick: function() {
          this.fire( 'updateCharts', {
            gids: this.getSelection()
          } );
        },

        clearMapTable: function() {
          this.fire( 'updateCharts', {
            gids: []
          } );
          _shared.clear();
        }
      },

      diseaseMapping: {
        mapAreaFromAreaChartChange: function( args ) {
           _study[ type ].fire( 'slctMapAreaFromAreaChart', args );
        }
      },

      riskAnalysis: {}
    };

  return RIF.extend( _shared, _study[ type ] );
} );