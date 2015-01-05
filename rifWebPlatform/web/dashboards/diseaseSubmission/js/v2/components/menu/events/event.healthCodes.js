RIF.menu[ 'event-healthCodes' ] = ( function( _dom ) {

  var menuContext = this;

  _dom.icdClassification.change( function() {
    var val = $( this ).val();
    menuContext.proxy.updateTopLevelHealthCodes( val );
  } );


  _dom.noChildElements.unbind( "click" );
  _dom.noChildElements.click( function( e ) {
    if ( !$( this ).hasClass( 'icdSelected' ) ) {
      $( this ).css( 'background-color', 'rgba( 152,251,152	,0.2);' );
    } else {
      $( this ).css( 'background-color', 'white' );
    };
    $( this ).toggleClass( 'icdSelected' );


    var sel = _dom.selection.call( null ),
      codes = [],
      i = sel.length;
    while ( i-- ) {
      codes.push( sel[ i ].innerHTML );
    };

    menuContext.proxy.taxonomyChanged( codes );
  } );


  return function() {

    _dom.healthCodesHeader.unbind( "click" );
    _dom.healthCodesHeader.click( function( e ) {

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

  }();


} );