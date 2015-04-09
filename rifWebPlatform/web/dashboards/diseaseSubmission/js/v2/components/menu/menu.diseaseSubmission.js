RIF.menu = ( function ( settings, publisher ) {

  var menus = settings.menus,
    _investigationReady = false,
    _observable = {},
    _p = {
      /*proxy: {
            frontMappingready: function() {
               _p.studyArea.request('getSelectAtsAvailable', RIF.user);
            },
            selectAtChange: function(selectAt) {
               _p.studyArea.request('getResolutionsAvailable', [RIF.user, selectAt]);
               _p.facade.selectAtChanged(selectAt);
               _p.facade.resolutionChanged(null);
            },
            studyAreaReady: function() {
               _p.healthCodes.request('getTaxonomy');
            },
            numeratorChanged: function(val) {
               _p.facade.numeratorChanged(val);
               _p.investigationParameters.request('getYears', val); // on numerator change!!  
            },
            taxonomy: null,
            updateTopLevelHealthCodes: function(taxonomy) {
               _p.healthCodes.request('getTopLevelHealthCodes', taxonomy);
               _p.proxy.taxonomy = taxonomy;
            },
            updateSubLevelHealthCodes: function(code, domEl) {
               _p.healthCodes.request('getSubLevelHealthCodes', {
                  "taxonomy": _p.proxy.taxonomy,
                  "code": code,
                  "dom": domEl
               })
            },
            investigationParameterChange: function(val, fnct) {
               fnct.call(_p.facade, val);
               _p.facade.isInvestigationReady();
            },
            icdSelectionChanged: function(args) {
               this.investigationParameterChange(args, _p.facade.icdSelectionChanged);
            },
            investigationReadyToBeAdded: function() {
               _investigationReady = true;
               $('#addInvestigation').addClass('addInvestigationActive');
            },
            investigationNotReadyToBeAdded: function() {
               _investigationReady = false;
               $('#addInvestigation').removeClass('addInvestigationActive');
            },
            addCurrentInvestigation: function() {
               if (_investigationReady) {
                  _p.facade.addInvestigation();
               };
            },
            searchHealthCodes: function(params) {
               _p.healthCodes.request('getSearchHealthCodes', params);
            },
            isDialogReady: function(dialog) {
               _p.facade.isDialogReady(dialog);
            },
            showDialog: function(dialog) {
               _p.frontSubmission.show(dialog);
            },
         },*/

      menuUtils: RIF.menu.utils(),

      initialize: function () {
        var l = menus.length
        while ( l-- ) {
          _p.initializeUnit( menus[ l ] );
        };
        return _p;
      },

      initializeUnit: function ( name ) {
        var dom = _p.getDom( name ),
          unit = _p.getUnit( dom, name ),
          controller = _p.getController( unit, name ),
          firer = _p.getFirer( name ),
          subscriber = _p.getSubscriber( controller, name );

        _p.setEvent( _observable, dom, name );
      },

      localExtend: function ( obj ) {
        for ( var i in obj ) {
          if ( typeof _observable[ i ] == 'undefined' ) {
            _observable[ i ] = obj[ i ];
          } else {
            var copy = _observable[ i ],
              copy2 = obj[ i ];
            _observable[ i ] = function ( args ) {
              copy2( args );
              copy( args );
            };
          };
        }
      },

      getUnit: function ( dom, name ) {
        var unit = RIF.utils.getUnit( 'menu', name, dom, this.menuUtils );
        return unit;
      },

      getFirer: function ( unitName ) {
        var firer = RIF.utils.getFirer( 'menu', unitName );
        _p.localExtend( firer );
        return firer;
      },

      getSubscriber: function ( controller, unitName ) {
        var sub = RIF.utils.getSubscriber( 'menu', unitName, controller );
        _p.localExtend( sub );
        return sub;
      },

      getController: function ( unit, unitName ) {
        return RIF.utils.getController( 'menu', unit, unitName );
      },
      getDom: function ( unit ) {
        return RIF.dom[ 'menu' ][ unit ]();
      },

      setEvent: function ( firer, dom, unitName ) {
        RIF.utils.setMenuEvent( firer, dom, unitName, this.menuUtils );
      }

    };


  _p.initialize();

  return _observable;
} );