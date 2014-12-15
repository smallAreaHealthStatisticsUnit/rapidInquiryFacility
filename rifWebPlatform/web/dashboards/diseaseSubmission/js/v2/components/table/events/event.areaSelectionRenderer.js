RIF.table[ 'event-areaSelectionRenderer' ] = ( function( _dom ) {

  var menuContext = this,
    startId = null,
    endId = null,
    mousedrag = false;

  /*var dragUp = function(){
      var interval =  Math.abs( (1 + parseInt(startId)) -  parseInt(endId))  ;
      var slctd = [];
      while( interval-- ){     
        var id = startId - ( interval );
        slctd.push( startId - ( interval ) );   
      }
      return slctd;
  };
    
  var dragDown = function(){

      var interval =  Math.abs( ( parseInt(endId)) -  parseInt(startId)) +1  ;
      console.log("Interval" +interval)
      var slctd = [];
      while( interval-- ){   
        var id = endId - ( interval ) ;  
        slctd.push( id );
      };
      return slctd;
  };*/

  var highlight = function() {

    var doesIt = $( this ).hasClass( "rowSelected" );
    if ( !doesIt && mousedrag ) {
      $( this ).addClass( 'rowSelected' );
    } else if ( doesIt && mousedrag ) {
      $( this ).removeClass( 'rowSelected' );
    };
  };



  _dom.rows.mouseover( function() {
    highlight.call( this );
  } );


  _dom.rows.mousedown( function() {
    mousedrag = true;
    startId = parseInt( this.id );
    highlight.call( this );
  } );

  _dom.rows.mouseup( function() {
    mousedrag = false;
    endId = parseInt( this.id );
    var slctd = [];
    /*console.log( "EndID:" + endId + "--StartID:"+ startId)
          if (endId < startId){
            var slctd = dragUp(); 
          }else {
            var slctd = dragDown();
          }*/

    var r = d3.selectAll( '.rowSelected' ).each( function( d, i ) {
      slctd.push( +this.id );
    } );

    menuContext.facade.studyAreaSelectionEvent( slctd );

  } );


} );