RIF.menu[ 'event-retrievableRunnable' ] = ( function ( _dom, firer ) {

  _dom.finalRun.click( function () {
    firer.isStudyReady();
  } );

  _dom.project.change( function () {
    var project = _dom.project.val();
    firer.projectChanged( project );
  } );

  _dom.studyDescription.change( function () {
    var description = _dom.studyDescription.val();
    firer.studyDescriptionChanged( description );
  } );

  $( _dom.viewSummary ).click( function () {
    firer.mapModelToSchema();
    $( _dom.summaryModal ).show();
  } );


} );