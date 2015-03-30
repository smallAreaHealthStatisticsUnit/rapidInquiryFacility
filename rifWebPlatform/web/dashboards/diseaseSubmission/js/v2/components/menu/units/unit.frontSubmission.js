RIF.menu.frontSubmission = (function( _dom, menuUtils ) {
   /*var _requests = {
         isLoggedIn: function() {
            RIF.getIsLoggedIn(_callbacks['isLoggedIn'], [RIF.user]);
         },
         getHealthThemes: function() {
            RIF.getHealthThemes(_callbacks['getHealthThemes'], null);
         },
         getNumDenom: function(desc) {
            RIF.getNumeratorDenominator(_callbacks['getNumDenom'], desc);
         },
         logOut: function() {
            RIF.getLogOut(_callbacks['logOut'], [RIF.user]);
         },
      },
      _callbacks = {
         isLoggedIn: function() {
             
           if (this["result"] == "true") {
             _requests.getHealthThemes();
           }else {
              RIF.redirect("../logIn/?rd=diseaseSubmission");
           };
         },
         getHealthThemes: function() {
            var themes = [this[0].name],
               description = [this[0].description],
               el = _dom['healthThemeAvailablesEl'];
            parent.dropDownInputText(themes, el);
            _requests.getNumDenom(description);
         },
         getNumDenom: function() {
            var num = [this[0].numeratorTableName],
               denom = [this[0].denominatorTableName];
            parent.dropDownInputText(num, _dom['numeratorAvailablesEl']);
            parent.dropDownInputText(denom, _dom['denominatorAvailablesEl']);
            parent.proxy.frontMappingready();
         },
         logOut: function() {
            RIF.redirect("../logIn/?rd=diseaseSubmission");
            RIF.statusBar(' You are now logged out', null, 'notify');
         },
      },*/
    /* geolevel obj */

    var _p = {
         showDialog: function(dialog) {
            console.log(dialog); 
            $(_dom[dialog]).show();
         },
         getHealthThemes: function( themes, description ) {
            var el = _dom['healthThemeAvailablesEl'];
           menuUtils.dropDownInputText(themes, el);
         },
         getNumDenom: function(num, denom) {
            menuUtils.dropDownInputText(num, _dom['numeratorAvailablesEl']);
            menuUtils.dropDownInputText(denom, _dom['denominatorAvailablesEl']);
            //parent.proxy.frontMappingready();
         }
        
      };

   return _p;
});