RIF.menu.diseaseSubmissionFront = ( function() {

  var parent = this,
    
    /* DOM ELEMENTS */   
    _domObjects = {
      dropdowns: $(".dropdowns input"),
      availables: $('.availables'),    
      healthThemeAvailablesEl: document.getElementById('healthThemeAvailables'),      
      importExportEl: document.getElementById('importExport'),
      runEl: document.getElementById('run'),
      studyArea: document.getElementById('studyArea'),
      compArea: document.getElementById('comparisonArea'),
      healthConds: document.getElementById('healthConditions'),  
      invParameters: document.getElementById('invParameters')   
    },
      

    /* EVENTS */
    _events = function() {
        
        var isDropDwonHovered = false;
        
        $(_domObjects.studyArea).click( function(){
            $('#studyAreaModal').show();
        });
        
        $(_domObjects.compArea).click( function(){
            $('#comparisonAreaModal').show();
        });
        
        $(_domObjects.healthConds).click( function(){
            $('#healththemeModal').show();
        });
        
        $(_domObjects.invParameters).click( function(){
            $('#parametersModal').show();
        });
        
       _domObjects.dropdowns.focus( function(){
            $(this).next().show();
       });
        
       _domObjects.dropdowns.blur( function(){
            if( !isDropDwonHovered ){
                $(this).next().hide();
            }
       });    
        
        _domObjects.availables.mouseover( function(){          
            isDropDwonHovered = true;
        });
        
        _domObjects.availables.mouseleave( function(){
            $(this).hide();
            isDropDwonHovered = false;
        });
        
        _domObjects.availables.children().click( function(){
            var input = $(this).parent().prev();
            $(input)
                .attr( "value", $(this).text())
                .addClass( "inpputBorderSelection");

            $(this).parent().hide();
        });
        

    },

    /* geolevel obj */
    _p = {
      
      getHealthThemesListElement: function(){
         return $(_domObjects.healthThemeAvailablesEl);
      },
        
      diseaseSubmissionFront: function() {
        _events();
        //
      }

      

    };

  _p.diseaseSubmissionFront();

  return _p;
} );