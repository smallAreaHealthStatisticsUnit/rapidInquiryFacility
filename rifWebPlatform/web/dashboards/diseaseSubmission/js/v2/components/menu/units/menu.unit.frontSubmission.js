RIF.menu.frontSubmission = (function(_dom) {
   var parent = this,
      _requests = {
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
            //if (this == true){
            _requests.getHealthThemes();
            /*}else {
            window.top.location = "../logIn/#diseaseSubmission";
         };*/
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
            RIF.statusBar(' You are now logged out', null, 'notify');
         },
      },
      /* geolevel obj */
      _p = {
         show: function(dialog) {
            $(_dom[dialog]).show();
         },
         initDiseaseSubmissionFront: function() {
            _requests.isLoggedIn();
         },
         getStudyName: function() {},
         getHealthThemes: function() {},
         getNumerator: function() {},
         getDenominator: function() {},
         getRetrievableStudies: function() {},
         request: function(reqName, params) {
            _requests[reqName](params);
         },
      };
   _p.initDiseaseSubmissionFront();
   return {
      frontSubmission: _p
   };
});