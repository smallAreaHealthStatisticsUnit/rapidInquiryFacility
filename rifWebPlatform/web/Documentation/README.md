# RIF 4.0 Javascript architecture

<p>The RIF 4.0 Javascript User Interface has been designed to allow its different components to be completely decoupled from one another. To do this a hybrid mixture between a Mediator and Observer pattern has been used within the RIF code's architecture.In practice the Graphical components notify a Mediator Object when a registered event occurs, this is able to propagate it to other components that have subscribed to the specific event as well as to modify a Model object which stores the entire application's business domain. </p>

###Initialize File
Events and their subscriptions can be set in the initialize.js file, this is also used to define and initialize the set of components and sub components needed in the application.<br />
For instance looking at a simplified version below of the diseaseSubmission  [initialize file]:
```
RIF.initialize = (function () {

  var _p = {
    components: {
      mediator: '',
      map: {
        studyType: 'diseaseSubmission',
        layerType: 'tilesvg',
        maps: ['studyArea', 'comparisonArea']
      },
      menu: {
        studyType: 'diseaseSubmission',
        menus: ['frontSubmission', 'investigationParameters', 'healthCodes', 'areaSelection', 'comparisonArea', 'retrievableRunnable', 'models']
      },
      table: {
        studyType: 'diseaseSubmission',
        tables: ['ageGroups', 'investigationsRecap', 'studyArea', 'comparisonArea', 'summary']
      }
    },
    events: {
      studyNameChanged: {
        subscribers: ["mediator"],
        firer: ["menu"],
        method: "studyNameChanged"
      }

    },
    init: function () {
      RIF.dom();
      RIF.utils.initComponents.call(this);
      RIF.utils.addEvents.call(this);
    }
  };

  return {
    setUp: (function (args) {
      _p.init();
      _p.mediator.isLoggedIn();
    }())
  };

});
```
We can gather that the application uses the following components :

  - Mediator - which has no graphical sub componetns
  - Map - With 2 sub componets called studyArea and ComparisonArea
  - Menu - With a list of sub components which includes 'frontSubmission', 'investigationParameters', 'healthCodes' etc..
  - Table - 'ageGroups', 'investigationsRecap', 'studyArea', 'comparisonArea', 'summary'


> A component then can be thought as a collection of units (or sub components) that may 
> share functionalities between one another and area able to communicate to the Mediator object.



### Events
An event within the initialiaze file can be added by introducing a new object within the events object.
For example examine the following:<br/>
```
     studyNameChanged: {
        subscribers: ["mediator"],
        firer: ["menu"],
        method: "studyNameChanged"
      }
```

The literal object above tells the RIF that a new event called studyNameChanged should be registed within its internal event's register, the &quot;firer&quot; property define which component will be firing/propagating the specific event, (we do not have to specify which sub components will actually do this), the &quot;subscribers&quot; property on the other hand set the component name which will listen for the event change/propagation, in this case as in most cases the Mediator is responsible to subscribe to events coming from User interaction with the Interface. </p>

### Components
There are 4 graphical components:
* Menu
* Table
* Map
* Charts
* Mediator - A unique abstract type of components which has no subcomponents 

Please note that some dashboard may not use all the components, i.e the diseaseSubmission does not make use of charts.</p>

### Sub components
As we saw earlier each components is made of multiple sub components, each sub component consists of:
  - One subscriber file
    -  Receives and handles events propagated by the mediator Object ( or rarely directly from other components)
  - One firer file
    -  Propagates the event and all paremeters needed to the Mediator Object ( or rarely directly to other components)<br />
 For example following up with the example above, the frontSubmission sub component is responsible for
 firing the studyNameChanged event, as can been seen below:

```
RIF.menu['firer-frontSubmission'] = (function () {
  var firer = {
    studyNameChanged: function (arg) {
      this.fire('studyNameChanged', arg);
    }
  };
  return firer;
});
```
- One event file
    -  Takes care of events originating from the GUI for the specific unit <br />
 ( apart from map events which are captured inside the renderer object) <br />The Event file has access to the Firer object, so when something is captured the correspoing firer/propagator <br />
 method is called to notify the Mediator 
- One unit file (or view)
    -   Renders data and has access to the DOM elements of the specific unit
- One controller file 
    -   Access the request methods gateway to the web services and pass data to the unit for rendering
- A dom object entry having the same name of the subcomponent in the DOM.js file
    -   Following up again from the previous example the frontSubmission sub component part of the Menu' s component have its own<br /> list of DOM elememts under [dom.menu.frontSubmission file]:


### Add new Sub Component

The 5 files above must along with the DOM object must exist for each new sub compent that needs to be added, each of them is initialized within the component local initializer file found at the root at the specific component's folder, for example take a look at menu's local initializer :
```
RIF.menu = (function (settings, publisher) {
  var menus = settings.menus,
    _investigationReady = false,
    _observable = {},
    _p = {
      menuUtils: RIF.menu.utils(),
      initialize: function () {
        var l = menus.length
        while (l--) {
          _p.initializeUnit(menus[l]);
        };
        return _p;
      },
      initializeUnit: function (name) {
        var dom = _p.getDom(name),
          unit = _p.getUnit(dom, name),
          controller = _p.getController(unit, name),
          firer = _p.getFirer(name),
          subscriber = _p.getSubscriber(controller, name);

        _p.setEvent(_observable, dom, name);
      },
      localExtend: function (obj) {
        for (var i in obj) {
          if (typeof _observable[i] == 'undefined') {
            _observable[i] = obj[i];
          } else {
            var copy = _observable[i],
              copy2 = obj[i];
            _observable[i] = function (args) {
              copy2(args);
              copy(args);
            };
          };
        }
      },
      getUnit: function (dom, name) {
        var unit = RIF.utils.getUnit('menu', name, dom, this.menuUtils);
        return unit;
      },
      getFirer: function (unitName) {
        var firer = RIF.utils.getFirer('menu', unitName);
        _p.localExtend(firer);
        return firer;
      },
      getSubscriber: function (controller, unitName) {
        var sub = RIF.utils.getSubscriber('menu', unitName, controller);
        _p.localExtend(sub);
        return sub;
      },
      getController: function (unit, unitName) {
        return RIF.utils.getController('menu', unit, unitName);
      },
      getDom: function (unit) {
        return RIF.dom['menu'][unit]();
      },
      setEvent: function (firer, dom, unitName) {
        RIF.utils.setMenuEvent(firer, dom, unitName, this.menuUtils);
      }
    };

  _p.initialize();

  return _observable;
});

```
The code snipeet above make sure that all of the 5 files are initialized and each received the expected parameter(s). This effectively set up the whole Menu's component and return an observable object to the main initializer which is used then within the internal event registerer to keep all function references to which method method is subscribed to what.

<p>To facilitate this process a commandline facility built using NODE.js has been produced to allow new subcomponents to be added to the app. The facility can be started by simplying clicking on the addUnit.bat (under windows) within the JSFrameworkUtilities folder.<br />
The utility will ask you for a dashboard name, as of 01/06/2015 the dashboards available can either be diseaseMapping|diseaseSubmission|logIn next it will ask for compoent's name (map|menu|table|chart) and then finally the name of the new sub component to be added. Once done 5 new files will be added to the project, for example if we were to add the following new sub component:<br />

* Dashboard: diseaseSubmission
* Component: Map
* Unit: choropleth

 
The following new 5 files will be added in the following location:<br />

> ->components<br />
 --->map<br />
 ----->controllers<br />
 -------->controller.choropleth.js<br />
 ----->events<br />
 -------->event.choropleth.js <br />
 ----->firers<br />
  --------->firer.choropleth.js <br />
  ----->units<br />
  --------->unit.choropleth.js <br />
  ----->subscribers<br />
  ---------->subscriber.choropleth.js <br />
 <br />
 
The dom.js file will have to be added manually, you will have to create a new object under dom.menu and call it choropleth: <br />
```
menu:{
   choropleth: function () {} 
}
```

<p>Once all five files have been created and dom entry added you can add the relevant script tags reference to the head of the htmlm document and start using the new sub component by adding it top the initialize file map's sub component list:<br />
```
     map: {
        studyType: 'diseaseSubmission',
        layerType: 'tilesvg',
        maps: ['studyArea', 'comparisonArea', 'choropleth' /* THE NEW ADDED SUB COMPONENT  */ ]
      }
```

### Development

Want to contribute? Great!

Dillinger uses Gulp + Webpack for fast developing.
Make a change in your file and instantanously see your updates!

Open your favorite Terminal and run these commands.

First Tab:
```sh
$ node app
```

Second Tab:
```sh
$ gulp watch
```

(optional) Third:
```sh
$ karma start
```

### Todo's

Write Tests
Github saving overhaul
Code Commenting
Night Mode

License
----

MIT


**Free Software, Hell Yeah!**


[initialize file]:https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/blob/master/rifWebPlatform/web/dashboards/diseaseSubmission/js/v2/initialize.js 
[dom.menu.frontSubmission file]:https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/blob/master/rifWebPlatform/web/dashboards/diseaseSubmission/js/v2/dom/dom.submission.js

