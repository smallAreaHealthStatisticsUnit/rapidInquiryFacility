RIF.menu[ 'event-healthCodes' ] = ( function( _dom ) {

  var menuContext = this,
    currentTaxonomy = null,
    index = 0,
    indexSelection = {},
    healthSelection = {}; //{ taxonomy: null , description: null, code: null }


  var checkTaxonomy = function( tax ) {
    if ( !healthSelection.hasOwnProperty( tax ) ) {
      healthSelection[ tax ] = [];
      indexSelection[ tax ] = [];
    };

  };

  /*var getTaxonomy = function(){
       if(currentTaxonomy == null){
           currentTaxonomy = menuContext.proxy.taxonomy;
           //checkTaxonomy(currentTaxonomy);
       };
        return currentTaxonomy;     
  };*/

  _dom.icdClassification.change( function() {
    currentTaxonomy = $( this ).val();
    $( '.taxonomySection' ).hide();
    menuContext.proxy.updateTopLevelHealthCodes( currentTaxonomy );
    //checkTaxonomy(currentTaxonomy);
  } );


  $( ".healthCodes" ).on( "click", _dom.healthCodesHeader, function( aEvent ) {
    var target = $( this );

    var spans = target.find( '>span' ),
      plusMinus = spans[ 0 ],
      childContainer = target.next( 'div' )[ 0 ];

    if ( target.hasClass( 'headerClicked' ) ) {
      plusMinus.innerHTML = ' + ';
      childContainer.style.display = 'none';
      target.toggleClass( 'headerClicked' );
      return;
    };

    if ( childContainer.hasChildNodes() ) {
      plusMinus.innerHTML = ' - ';
      childContainer.style.display = 'block';
      target.toggleClass( 'headerClicked' );
      return;
    };

    var val = spans[ 1 ]
      .innerHTML
      .trim();

    plusMinus.innerHTML = ' - ';
    target.toggleClass( 'headerClicked' );
    menuContext.proxy.updateSubLevelHealthCodes( val, childContainer );
  } );


  $( ".healthCodes" ).on( "click", _dom.noChildElements, function( aEvent ) {

    var spans = $( this ).find( 'span' ),
      code = spans[ 0 ].innerHTML.trim(),
      description = spans[ 1 ].innerHTML.trim();

    var classes = $( this ).attr( "class" ).toString().split( ' ' ),
      classLength = classes.length,
      taxonomy = function() {
        while ( classLength-- ) {
          if ( classes[ classLength ] != 'icdSelected' && classes[ classLength ] != 'noChildElements' ) {
            return classes[ classLength ];
          };
        };
        return null;
      }();

    checkTaxonomy( taxonomy );

    if ( indexSelection[ taxonomy ].indexOf( code ) === -1 ) {
      $( this ).css( 'background-color', 'rgba( 152,251,152	,0.2);' );
      indexSelection[ taxonomy ].push( code );
      healthSelection[ taxonomy ].push( {
        description: description,
        code: code
      } );
    } else {
      $( this ).css( 'background-color', 'white' );
      var i = indexSelection[ taxonomy ].indexOf( code );
      indexSelection[ taxonomy ].splice( i, 1 );;
      healthSelection[ taxonomy ].splice( i, 1 );
      if ( healthSelection[ taxonomy ].length == 0 ) {
        delete healthSelection[ taxonomy ];
      };
    };

    $( this ).toggleClass( 'icdSelected' );
    menuContext.proxy.icdSelectionChanged( healthSelection );
  } );

  _dom.clearAll.click( function() {
    $( '.' + _dom.healthSelection )
      .removeClass( _dom.healthSelection )
      .css( 'background-color', 'white' );

    indexSelection = {};
    healthSelection = {};
  } );


} );