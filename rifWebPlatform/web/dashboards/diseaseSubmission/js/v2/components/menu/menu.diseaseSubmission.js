RIF.menu = (function(settings) {
   var menus = settings.menus,
      _investigationReady = false,
      _p = {
         init: function() {
            _p = RIF.mix(RIF.menu.utils(), _p);
            return _p;
         },
         proxy: {
            frontMappingready: function() {
               _p.studyArea.request('getSelectAtsAvailable', RIF.user);
            },
            selectAtChange: function(selectAt) {
               _p.studyArea.request('getResolutionsAvailable', [RIF.user, selectAt]);
               _p.facade.selectAtChanged(selectAt);
               _p.facade.resolutionChanged(null);
            },
            studyAreaReady: function() {
               _p.healthCodes.request('getTaxonomy');
            },
            numeratorChanged: function(val) {
               _p.facade.numeratorChanged(val);
               _p.investigationParameters.request('getYears', val); // on numerator change!!  
            },
            taxonomy: null,
            updateTopLevelHealthCodes: function(taxonomy) {
               _p.healthCodes.request('getTopLevelHealthCodes', taxonomy);
               _p.proxy.taxonomy = taxonomy;
            },
            updateSubLevelHealthCodes: function(code, domEl) {
               _p.healthCodes.request('getSubLevelHealthCodes', {
                  "taxonomy": _p.proxy.taxonomy,
                  "code": code,
                  "dom": domEl
               })
            },
            investigationParameterChange: function(val, fnct) {
               fnct.call(_p.facade, val);
               _p.facade.isInvestigationReady();
            },
            icdSelectionChanged: function(args) {
               this.investigationParameterChange(args, _p.facade.icdSelectionChanged);
            },
            investigationReadyToBeAdded: function() {
               _investigationReady = true;
               $('#addInvestigation').addClass('addInvestigationActive');
            },
            investigationNotReadyToBeAdded: function() {
               _investigationReady = false;
               $('#addInvestigation').removeClass('addInvestigationActive');
            },
            addCurrentInvestigation: function() {
               if (_investigationReady) {
                  _p.facade.addInvestigation();
               };
            },
            searchHealthCodes: function(params) {
               _p.healthCodes.request('getSearchHealthCodes', params);
            },
            isDialogReady: function(dialog) {
               _p.facade.isDialogReady(dialog);
            },
            showDialog: function(dialog) {
               _p.frontSubmission.show(dialog);
            },
         },
         getFacade: function() {
            this.facade = RIF.getFacade('menu', settings.studyType, this);
            return this;
         },
         extendMenu: function() {
            _p = this.extendMenuComponent(_p, menus);
            return _p;
         },
         setEvents: function(menu) {
            var m = menu || menus;
            var ev = this.setMenuEvent(_p, m);
         }
      };
   _p.init().getFacade().extendMenu().setEvents();
   return _p.facade;
});