RIF.areaSelectionTable = ( function() {

  var _p = {



    init: function() {

    },


    setEvents: function() {
      //Empty for now due to how table handles events
      // var ev = RIF.getEvent( 'table', settings.studyType );
      // ev.call( this );
    },

    //conforms	
    extendTable: function() {
      //Empty for now and maybe forever
      //return _p;
    },

    getFacade: function() {
      //this.facade = RIF.getFacade( 'table', settings.studyType, _p );
      //return this;
    }


  };

  //_p.getFacade()
  //  .extendTable();
  //-.setEvents() : called after initGrid event is fired from within renderer init()

  _p.init();
  return _p;

} );