RIF.menu.utils = ( function() {

  var _p = {

    dropDown: function( data, el ) {
      el.empty();
      if ( RIF.isArray( data ) ) {
        _p.dropDownFromArray( data, el );
      } else if ( !jQuery.isEmptyObject( data ) ) {
        _p.dropDownFromObj( data, el );
      } else {
        _p.dropDownFromObj( {
          "N/A": "None available"
        }, el );
      }
    },

    dropDownFromArray: function( arr, el ) {

      if ( arr.length === 0 ) {
        arr.push( "None available" );
      };

      var l = arr.length;
      while ( l-- ) {
        var val = arr[ l ];
        _p.addSelectOption( el, val, val );
      }
    },

    dropDownInputText: function( arr, el ) {

      if ( arr.length === 0 ) {
        arr.push( "None available" );
      }

      if ( !( el instanceof jQuery ) ) {
        el = $( el );
      };

      el.empty();

      var l = arr.length;
      while ( l-- ) {
        var val = arr[ l ];
        el.prepend( "<div><a href='#'>" + val + "</a></div>" )
      }

      _p.dropDownInputTextEvents( el.prev(), el );
    },

    dropDownInputTextEvents: function( dropdown, available ) {

      var isDropDwonHovered = false;

      dropdown.off( 'focus' );
      dropdown.off( 'blur' );
      available.off( 'mouseover' )
      available.children().off( 'click' );

      dropdown.on( 'focus', function() {
        $( this ).next().show();
      } );

      dropdown.on( 'blur', function() {
        if ( !isDropDwonHovered ) {
          $( this ).next().hide();
        }
      } );

      available.on( 'mouseover', function() {
        isDropDwonHovered = true;
      } );

      available.on( 'mouseleave', function() {
        $( this ).hide();
        $( this ).prev().blur();
        isDropDwonHovered = false;
      } );

      available.children().on( 'click', function() {
        var input = $( this ).parent().prev();
        $( input )
          .attr( "value", $( this ).text() )
          .addClass( "inpputBorderSelection" );

        $( this ).parent().hide();
      } );
    },

    extendMenuComponent: function( component, units ) {
      var l = units.length;
      while ( l-- ) {
        var r = RIF[ 'menu' ][ units[ l ] ].call( component, RIF.dom[ 'menu' ][ units[ l ] ] );
        component = RIF.mix( r, component );
      };

      return component;
    },

    setMenuEvent: function( component, units ) {
      var l = units.length;
      while ( l-- ) {
        var eventName = [ 'event', units[ l ] ].join( '-' );
        RIF[ 'menu' ][ eventName ].call( component, RIF.dom[ 'menu' ][ units[ l ] ] );
      };
    },


    fieldCheckboxes: function( obj, el, name ) {
      el.empty();
      var counter = 0,
        checked = true;
      for ( var key in obj ) {
        var id = "filterCols" + counter++,
          p = _p.getCheckBoxLabel( name, obj[ key ], obj[ key ], id, checked );

        el.prepend( '<div>' + p + '</div>' );
      }
    },

    fieldCheckboxesResultsSet: function( obj, el, name ) {
      el.empty();
      var counter = 0;
      for ( var key in obj ) {
        var checked = counter < 4 ? true : false,
          id = "filterCols" + counter++,
          p = _p.getCheckBoxLabel( name, obj[ key ], obj[ key ], id, checked );

        el.prepend( '<div>' + p + '</div>' );
      }
    },

    greyOut: function( el ) {
      el.find( "select, button" ).prop( 'disabled', 'disabled' ).css( {
        opacity: '0.5'
      } );
    },

    removeGreyOut: function( el ) {
      el.find( "select, button" ).prop( 'disabled', false ).css( {
        opacity: '1'
      } );
    },

    dropDownFromObj: function( obj, el ) {
      for ( var key in obj ) {
        _p.addSelectOption( el, key, obj[ key ] );
      }
    },

    getSpecialDropdownValue: function( id ) {
      return $( "#" + id ).find( "dt a span.value" ).html();
    },

    addSelectOption: function( slct, val, option_text ) {
      slct.prepend( "<option value=" + val + ">" + option_text + "</option>" )
    },

    getCheckBoxLabel: function( name, val_txt, id, checked ) {
      var c = ( checked ) ? "checked" : "",
        p = '<input type="checkbox" name="' + name + '" value="' + val_txt + '" id="' + id + '" class="colsChoice" ' + c + ' />' +
        '<label for="' + id + '" class="colsChoiceLbl">' + val_txt + '</label>';

      return p;
    },

    getCheckedValues: function( name ) {
      var checkedValues = $( 'input[name="' + name + '"]:checked' ).map( function() {
        return this.value;
      } ).get().reverse();

      return checkedValues;
    }

  };

  return _p;
} );