RIF[ 'menu' ][ 'event-models' ] = ( function ( dom, firer ) {

  $( dom.statDone ).click( function ( aEvent ) {
    var modelsChecked = dom.modelsList.find( ' > div > input:checked' );
    var l = modelsChecked.length;
    var models = [];
    while ( l-- ) {
      var modelObj = {};
      modelObj.name = modelsChecked[ l ].name;
      modelObj.parameters = {};
      modelObj.parameters.parameter = [];
      var params = $( modelsChecked[ l ] ).parent().find( " div input" );
      for ( var i = 0; i < params.length; i++ ) {
        var paramName = params[ i ].name;
        var paramValue = params[ i ].value;
        modelObj.parameters.parameter.push( {
          name: paramName,
          value: paramValue
        } );
      };
      models.push( modelObj );
    };

    firer.calculationMethodsChanged( models );
  } );
} );