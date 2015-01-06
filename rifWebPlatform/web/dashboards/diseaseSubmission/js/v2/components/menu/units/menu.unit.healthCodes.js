RIF.menu.healthCodes = ( function( _dom ) {

  var parent = this,

    _requests = {
      getTaxonomy: function() {
        RIF.getHealthTaxonomy( _callbacks[ 'getTaxonomy' ], null );
      },
      getTopLevelHealthCodes: function( icd ) {
        RIF.getTopLevelHealthCodes( _callbacks[ 'getTopLevelHealthCodes' ], [ icd ] ); // param hardcoded for now
      },
      getSubLevelHealthCodes: function( params ) { // {taxonomy,code,dom}
        var specialClbk = function() {
          _callbacks.getSubLevelHealthCodes.call( this, params.dom );
        };
        RIF.getSubHealthCodes( specialClbk, [ params.taxonomy, params.code ] ); // param hardcoded for now
      },


    },

    _callbacks = {

      getTaxonomy: function() {
        var taxonomies = [],
          l = this.length;
        while ( l-- ) {
          taxonomies.push( this[ l ][ 'nameSpace' ] );
        };
        parent.dropDownInputText( taxonomies, _dom.icdClassificationAvailable );
        _requests.getTopLevelHealthCodes( 'icd10' );
      },

      getTopLevelHealthCodes: function() {
        _insertChildrenElements( this, _dom.tree );
      },

      getSubLevelHealthCodes: function( domParent ) {
        _insertChildrenElements( this, domParent );
      },

    },

    _insertChildrenElements = function( data, domParent ) {
      var fragment = document.createDocumentFragment();
      l = data.length

      domParent.style.display = 'none';
      domParent.innerHTML = '';

      data.reverse();
      while ( l-- ) {
        var container = document.createElement( "div" ),
          div = document.createElement( "div" ),
          divHeader = document.createElement( "div" ),
          childrenContainer = document.createElement( "div" ),
          expand = '';

        container.appendChild( div );

        if ( data[ l ][ 'numberOfSubTerms' ] > 0 ) {
          expand = '<span> + </span> ';
          divHeader.className = 'healthCodesHeader';
        } else {
          container.className = 'noChildElements'
        };

        var description = ( data[ l ][ 'description' ] ).replace( ';', '' );
        divHeader.innerHTML = expand + '<span>' + data[ l ][ 'code' ] + '</span> - ' + description;
        div.appendChild( divHeader );

        childrenContainer.className = 'childrenContainer';
        div.appendChild( childrenContainer ); // for children elements

        fragment.appendChild( container );
      };

      domParent.appendChild( fragment );
      domParent.style.display = 'block';
      parent.proxy.updateEventsHealthTree();
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