RIF.menu.healthCodes = ( function ( _dom, menuUtils ) {

  var _topLevelHealthCodesRequested = [],

    _reRequestTopLevelHealthCodes = function ( taxonomy ) {
      var i = _topLevelHealthCodesRequested.indexOf( taxonomy );
      if ( i >= 0 ) {
        _topLevelHealthCodesRequested.splice( i, 1 );
        _requests.getTopLevelHealthCodes( taxonomy );
        return true;
      };
    },

    _insertChildrenElements = function ( data, domParent, taxonomy ) {
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
          container.className = 'noChildElements ' + taxonomy;
        };
        var description = ( data[ l ][ 'description' ] ).replace( ';', '' );
        divHeader.innerHTML = expand + '<span>' + data[ l ][ 'code' ] + '</span> - ' + '<span>' + description + '</span>';
        div.appendChild( divHeader );
        childrenContainer.className = 'childrenContainer';
        div.appendChild( childrenContainer ); // for children elements
        fragment.appendChild( container );
      };
      domParent.appendChild( fragment );
      domParent.style.display = 'block';
    },

    _p = {

      getTaxonomy: function ( entries ) {
        var l = entries.length;
        while ( l-- ) {
          $( _dom[ "tree" ] ).append( "<div class='taxonomySection' id='" + entries[ l ] + "'></div>" )
        };
        menuUtils.dropDownInputText( entries, _dom.icdClassificationAvailable );
      },

      getTopLevelHealthCodes: function ( taxonomyName, codes ) {
        var el = document.getElementById( taxonomyName )
        _insertChildrenElements( codes, el );
        _dom.icdClassification.val( taxonomyName );
      },

      getSubLevelHealthCodes: function ( data, domParent, taxonomy ) {
        _insertChildrenElements( data, domParent, taxonomy );
      },

      getSearchHealthCodes: function ( data, taxonomy ) {
        if ( data.length === 0 ) {
          RIF.statusBar( 'No Health codes found', true, 'notify' );
          return;
        };
        _insertChildrenElements( data, _dom.healthResults, taxonomy );
        _dom.searchResults.style.display = 'block';
      },

    };

  return _p;

} );