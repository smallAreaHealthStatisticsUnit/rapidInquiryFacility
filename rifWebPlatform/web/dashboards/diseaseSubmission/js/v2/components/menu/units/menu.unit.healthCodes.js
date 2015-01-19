    RIF.menu.healthCodes = ( function( _dom ) {

      var parent = this,

        _topLevelHealthCodesRequested = [],

        _requests = {
          getTaxonomy: function() {
            RIF.getHealthTaxonomy( _callbacks[ 'getTaxonomy' ], null );
          },
          getTopLevelHealthCodes: function( icd ) {
            if ( _topLevelHealthCodesRequested.indexOf( icd ) != -1 ) {
              $( '#' + icd ).show();
              return;
            };

            _topLevelHealthCodesRequested.push( icd );

            var _callback = function() {
              _callbacks[ 'getTopLevelHealthCodes' ].call( this, icd );
            };

            RIF.getTopLevelHealthCodes( _callback, [ icd ] ); // param hardcoded for now
          },
          getSubLevelHealthCodes: function( params ) { // {taxonomy,code,dom}
            var specialClbk = function() {
              _callbacks.getSubLevelHealthCodes.call( this, params.dom, params.taxonomy );
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
              $( tree ).append( "<div class='taxonomySection' id='" + this[ l ][ 'nameSpace' ] + "'></div>" )
            };

            if ( this.length > 0 ) {
              var firstTaxonomy = this[ 0 ][ 'nameSpace' ];
              parent.proxy.taxonomy = firstTaxonomy
              _requests.getTopLevelHealthCodes( firstTaxonomy );
              parent.proxy.updateTopLevelHealthCodes( firstTaxonomy );
              _dom.icdClassification.val( firstTaxonomy );
              parent.dropDownInputText( taxonomies, _dom.icdClassificationAvailable );
            };
          },

          getTopLevelHealthCodes: function( taxonomyName ) {
            var el = document.getElementById( taxonomyName )
            _insertChildrenElements( this, el );
          },

          getSubLevelHealthCodes: function( domParent, taxonomy ) {
            _insertChildrenElements( this, domParent, taxonomy );
          },

        },

        _insertChildrenElements = function( data, domParent, taxonomy ) {
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
            divHeader.innerHTML = expand + '<span>' + data[ l ][ 'code' ] + '</span> - ' +
              '<span>' + description + '</span>';
            div.appendChild( divHeader );

            childrenContainer.className = 'childrenContainer';
            div.appendChild( childrenContainer ); // for children elements

            fragment.appendChild( container );
          };

          domParent.appendChild( fragment );
          domParent.style.display = 'block';
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