RIF.table.investigationsRecap = ( function( _dom ) {

  var parent = this,


    _p = {

      addRow: function( params ) {
        var investigationId = params[ 0 ],
          data = params[ 1 ];

        _dom.tableContent.style.display = 'none';
        var oddOreven = '',
          healthOutcomes = '',
          taxonomies = '',
          covariates = ( data.covariates != null ) ? data.covariates.join() : 'none',
          ageGroups = data.ageGroups.reverse().map( function( elem ) {
            return elem.band.replace( / /g, '' );
          } ).join( '   ' );

        for ( var taxonomy in data.healthOutcomes ) {
          var counter = 0,
            outcomes = data.healthOutcomes[ taxonomy ].length;

          while ( counter < outcomes ) {
            healthOutcomes += '<p>' + data.healthOutcomes[ taxonomy ][ counter ].code + ' - ' + data.healthOutcomes[ taxonomy ][ counter ].description + '</p>';
            taxonomies += '<p>' + taxonomy + '</p>';
            counter++;
          };
        };

        var tr = document.createElement( "tr" );

        oddOreven = ( investigationId % 2 == 0 ) ? 'even' : 'odd',
        tr.className = 'aSR ' + oddOreven;
        tr.id = 'investigation' + investigationId;

        tr.innerHTML = '<td class="taxonomiRow">' + taxonomies + '</td>' +
          '<td class="healthOutcomesRow">' + healthOutcomes + '</td>' +
          '<td class="agegroupsRow"><p>' + ageGroups + '</p></td>' +
          '<td class="yearRow"><p>' + data.minYear + '-' + data.maxYear + '</p></td>' +
          '<td class="genderRow"><p>' + data.gender + '</p></td>' +
          '<td class="covariatesRow"><p>' + covariates + '</p></td>' +
          '<td class="removeInvestigation"><p></p></td>';


        _dom.tableBody.append( tr );
        _dom.tableContent.style.display = 'block';
      }

    };

  return {
    investigationsRecap: _p
  };
} );