RIF.table[ 'event-ageGroupsRenderer' ] = ( function( _dom ) {

  var menuContext = this,
    slctd = [],
    isHighlighted;

  _dom.rows.unbind( 'mouseover' );
  _dom.rows.unbind( 'selectstart' );
  _dom.rows.unbind( 'mousedown' );

  _dom.rows.mousedown( function() {
    menuContext.isMouseDown = true;
    $( this ).toggleClass( "rowSelected" );
    isHighlighted = $( this ).hasClass( "rowSelected" );
    return false; // prevent text selection

  } ).mouseover( function() {
    if ( menuContext.isMouseDown ) {
      $( this ).toggleClass( "rowSelected", isHighlighted );
      var r = d3.selectAll( '#ageGroupsWrapper .rowSelected' ).each( function( d, i ) {
        slctd.push( +this.id );
      } );
      //menuContext.facade.studyAreaSelectionEvent(slctd) ;     
    }
  } ).bind( "selectstart", function() {
    return false;
  } )


} );