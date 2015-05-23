RIF.table['unit-investigationsRecap'] = (function (_dom) {

  var addRow = function (params) {

      var investigationId = params[0],
        data = params[1];
      _dom.tableContent.style.display = 'none';

      var oddOreven = '',
        healthOutcomes = '',
        taxonomies = '',
        covariates = (data.covariates != null) ? data.covariates.join() : 'none',
        lower = data.ageGroups.lower.ageLimits.split('-')[0],
        upper = data.ageGroups.upper.ageLimits.split('-')[1],
        ageGroups = lower + ' To ' + upper;

      for (var taxonomy in data.healthOutcomes) {
        var counter = 0,
          outcomes = data.healthOutcomes[taxonomy].length;
        while (counter < outcomes) {
          healthOutcomes += '<p>' + data.healthOutcomes[taxonomy][counter].code + ' - ' + data.healthOutcomes[taxonomy][counter].description + '</p>';
          taxonomies += '<p>' + taxonomy + '</p>';
          counter++;
        };
      };

      var tr = document.createElement("tr");
      oddOreven = (investigationId % 2 == 0) ? 'even' : 'odd',
      tr.className = 'aSR ' + oddOreven;
      tr.id = 'investigation' + investigationId;

      // Jquery needed to make IE compatible, .innerHTML wouldnt work otherwise  
      $(tr).html('<td class="taxonomiRow">' + taxonomies + '</td>' +
        '<td class="healthOutcomesRow">' + healthOutcomes +
        '</td>' + '<td class="agegroupsRow"><p>' + ageGroups + '</p></td>' +
        '<td class="yearRow"><p>' + data.minYear + '-' + data.maxYear + '</p></td>' +
        '<td class="genderRow"><p>' + data.gender + '</p></td>' +
        '<td class="covariatesRow"><p>' + covariates + '</p></td>' +
        '<td class="removeInvestigation"><p></p></td>');

      _dom.tableBody.append(tr);
      _dom.tableContent.style.display = 'block';
    },

    _p = {
      addRow: addRow
    };


  return _p

});