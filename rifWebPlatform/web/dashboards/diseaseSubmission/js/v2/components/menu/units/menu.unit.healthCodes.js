RIF.menu.healthCodes = ( function( _dom ) {

  var parent = this,

    _requests = {
      getTaxonomy: function() {
        RIF.getHealthTaxonomy( _callbacks[ 'getTaxonomy' ], null );
      },
      getTopLevelHealthCodes: function( icd ) {
        RIF.getTopLevelHealthCodes( _callbacks[ 'getTopLevelHealthCodes' ], [ icd ] ); // param hardcoded for now
      },

    },

    _callbacks = {
      getTaxonomy: function() {
        var taxonomies = [],
          l = this.length;
        while ( l-- ) {
          taxonomies.push( this[ l ][ 'nameSpace' ] )
        };
        parent.dropDownInputText( taxonomies, _dom.icdClassificationAvailable );
        _requests.getTopLevelHealthCodes( 'icd10' );
      },

      getTopLevelHealthCodes: function() {
        var fragment = document.createDocumentFragment();
        l = this.length,
        divClass = 'healthCodesHeader';

        _dom.tree.style.display = 'none';
        _dom.tree.innerHTML = '';
        while ( l-- ) {
          var oddOreven = ( l % 2 == 0 ) ? 'even' : 'odd',
            div = document.createElement( "div" ),
            span = '';

          if ( this[ l ][ 'numberOfSubTerms' ] > 0 ) {
            span = '<span> + </span>';
          };
          div.innerHTML = '<div class=' + divClass + '>' + span + ' ' + this[ l ][ 'description' ] + '</div';
          fragment.appendChild( div );
        }
        _dom.tree.appendChild( fragment );
        _dom.tree.style.display = 'block';
      },
    },

    /* geolevel obj */
    _p = {

      request: function( reqName, params ) {
        _requests[ reqName ]( params );
      }

    };


  return {
    healthCodes: _p
  };
} );