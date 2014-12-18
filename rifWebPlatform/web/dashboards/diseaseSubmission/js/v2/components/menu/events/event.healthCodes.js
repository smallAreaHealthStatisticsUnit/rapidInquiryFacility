RIF.menu[ 'event-healthCodes' ] = ( function( _dom ) {

  var menuContext = this;

  _dom.icdClassification.change( function() {
    var val = $( this ).val();
    menuContext.proxy.updateTopLevelHealthCodes( val );
  } );


  $( _dom.tree ).on( 'click', _dom.healthCodesHeader, function( e ) {
    var target = e.target;
    if ( target.className != 'healthCodesHeader' ) {
      return;
    };

    var childContainer = target.getElementsByTagName( 'div' )[ 0 ],
      val = target.getElementsByTagName( 'span' )[ 0 ]
      .innerHTML
      .trim();

    menuContext.proxy.updateSubLevelHealthCodes( val, childContainer );

  } );

} );