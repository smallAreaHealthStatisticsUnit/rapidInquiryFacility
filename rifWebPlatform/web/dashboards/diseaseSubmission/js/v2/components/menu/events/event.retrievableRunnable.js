RIF.menu[ 'event-retrievableRunnable' ] = ( function ( _dom, firer ) {

  _dom.finalRun.click( function () {
    var project = _dom.project.val();
    var description = _dom.studyDescription.val();
    firer.projectChanged( project );
    firer.studyDescriptionChanged( description );
    firer.isStudyReady();
  } );


} );