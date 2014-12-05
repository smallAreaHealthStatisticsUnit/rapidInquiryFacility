RIF.menu[ 'event-choropleth' ] = ( function( _dom ) {

  var menuContext = this,

    breksEdit = function() {
      menuContext.choropleth.breaksEdited = false;
      $( _dom.breaks ).hide();
    };

  _dom.dropDownColourScale.click( function() {
    _dom.palette.toggle();
  } );

  _dom.intervals.change( function() {
    breksEdit();
  } );

  _dom.classification.change( function() {
    breksEdit();
    if ( $( this ).val() === 'standardDeviation' ) {
      menuContext.choropleth.updateStandardDeviation( _dom.intervals );
    };

    menuContext.choropleth.updateIntervals( _dom.selectedScale.children().length, _dom );
  } );

  _dom.choroplethBtn.click( function() {
    _dom.choroplethModal.show();
  } );

  _dom.dropDownColourScale.click( function() {
    menuContext.choropleth.updateIntervals( _dom.selectedScale.children().length, _dom );
  } );


  _dom.colourScales.click( function() {
    menuContext.choropleth.updateIntervals( $( this ).children().length, _dom );
    menuContext.choropleth.scale = this.className;
    var text = $( this ).html();
    _dom.selectedScale.html( text );
    $( this ).hide();
  } );

  _dom.edit.click( function() {
    menuContext.choropleth.breaksEdited = false;
    var slctd = menuContext.choropleth.selected( _dom );
    if ( slctd.field !== "N/A" ) {
      menuContext.facade.fire( "editChoroplethBreaks", slctd );
    };
  } );

  _dom.invert.change( function() {
    _dom.palette.empty();
    menuContext.choropleth.makeColourScales( "invert" );
    var selectionInv = $( "." + menuContext.choropleth.scale ).html()
    _dom.selectedScale.html( selectionInv );
  } );

  _dom.save.click( function() {
    if ( _dom.classification.val() === 'standardDeviation' ) {
      menuContext.choropleth.updateStandardDeviation( _dom.intervals );
    };

    _dom.choroplethModal.hide();

    var slctd = menuContext.choropleth.selected( _dom );

    if ( slctd.field !== "N/A" ) {
      menuContext.facade.fire( "mapStyleChange", slctd );
      menuContext.facade.hoverFieldChange( slctd.field );
    };

  } );


} );