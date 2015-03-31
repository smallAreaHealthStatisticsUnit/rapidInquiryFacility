RIF.menu['controller-frontSubmission'] = (function( unit ) {
   
   var _p = {
       
            isLoggedIn: function() {
              var clbk = function(){
                  if (this[0]["result"] == "true") {
                     _p.getHealthThemes();
                  }else {
                     RIF.utils.redirect("../logIn/?rd=diseaseSubmission");
                  };
              };
              RIF.getIsLoggedIn(clbk, [RIF.user]);
            },
                
            getHealthThemes: function() {  
              var clbk = function(){
                 var themes = [this[0].name],
                     description = [this[0].description];
                 unit.getHealthThemes(themes, description);
                 _p.getNumDenom(description);  
                  
              };    
                
              RIF.getHealthThemes(clbk, null);
            },
           getNumDenom: function(desc) {
              var clbk = function(){
                 var num = [this[0].numeratorTableName],
                     denom = [this[0].denominatorTableName];
                  
                 unit.getNumDenom(num, denom);
                 // FIRE parent.proxy.frontMappingready();     
              };   
              RIF.getNumeratorDenominator(clbk, desc);
           },
           logOut: function() {
              var clbk = function(){
                RIF.utils.redirect("../logIn/?rd=diseaseSubmission");
                RIF.statusBar(' You are now logged out', null, 'notify');
              };   
               
              RIF.getLogOut( clbk, [RIF.user]);
           },
       
           showDialog: function(dialog){
                unit.showDialog(dialog);
           }    
        };
    
    _p.isLoggedIn();
    
    return _p;
});