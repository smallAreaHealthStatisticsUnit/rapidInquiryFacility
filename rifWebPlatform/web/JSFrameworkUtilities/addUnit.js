 var prompt = require('prompt');
 var fs = require('fs');  
    
  var pathToDshaboard = '../dashboards';   

  var properties = [
    {
      name: 'dashboard', 
      validator: /^diseaseMapping|diseaseSubmission|logIn$/,
      warning: 'Dashboard can only be  diseaseMapping or diseaseSubmission or logIn.'
    },
    {
      name: 'component', 
      validator:  /^map|menu|table|chart/,
      warning: 'Componet can only be chart or map or menu or table.'
    },
      
      {
      name: 'unit', 
      validator:  /^[a-zA-Z\s\-]+$/,
      warning: 'Unit must be only letters, spaces, or dashes'
    }
  ];

  prompt.start();

  prompt.get(properties, function (err, result) {
    if (err) { return onErr(err); }
    console.log('Command-line input received:');
    console.log('  Dashboard: ' + result.dashboard);
    console.log('  Component: ' + result.component);
    console.log('  Unit: ' + result.unit);
    
    var bodies = getBodies( result.component, result.unit );
    var pathToJs =  pathToDshaboard +"/" + result.dashboard + "/js/v2/components/" + result.component ;
    checkIfDirExists(pathToJs);
      
    for (var i in bodies){
        
        var componentPath = pathToJs + "/" + i + "/";
        checkIfDirExists(componentPath);
        
        var path = componentPath + i + "." + result.unit + ".js",
            content = bodies[i][0] + bodies[i][1]; 
        
        createFile( path, content );
    };      
  });

  function onErr(err) {
    console.log(err);
    return 1;
  };

  var getBodies = function(component, unit){   
      var main = "RIF['" + component + "']";
      
      var bodies = {
        controllers: [
            main + "[ 'controller-" + unit + "' ] = ( function ( unit ) { \n",
            "var _p = {}; \n" +
            "return _p; \n" +
            "});"
        ],  
        events: [
            main + "[ 'event-" + unit + "' ] = ( function ( dom, firer ) { \n" ,
            "});"
        ],
        firers: [
            main + "[ 'firer-" + unit + "' ] = ( function () { \n" ,
            "var firer = {}; \n" +
            "return firer; \n" +
            "});"
        ],
        subscribers: [
            main + "[ 'subscriber-" + unit + "' ] = ( function ( controller ) { \n" , 
            "var subscriber = {}; \n" +
            "return subscriber; \n" +
            "});"
        ],
        units: [
          main + "[ 'unit- " + unit + "'] = ( function ( _dom, menuUtils ) { \n" , 
          "var _p = {}; \n" +
          "return _p; \n" +
          "});"
        ]
      };
      
      return bodies;
  };

  var createFile = function( path, content ){
    fs.writeFile(path, content, function(err) {
    if(err) {
        return console.log(err);
    }
    console.log("The file " + path + " was saved!");
    }); 
  };

  var checkIfDirExists = function( dir ){
    if (!fs.existsSync(dir)){
    fs.mkdirSync(dir);
    }
  };
    
    