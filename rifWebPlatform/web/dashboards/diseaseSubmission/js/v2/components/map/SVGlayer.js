RIF.SVGlayer = ( function () {

  RIF.Map.apply( this, [] );

  this.initLayer = function () {
    console.log( 'layerInit' );
  }
} );

/*
 * Pseudo-classical inheritance (through prototyping): similar to Object.create
 *
 * RIF.SVGlayer.prototype = new RIF.Map();
 * var layer = new RIF.SVGlayer();
 *
 */



console.log( new RIF.SVGlayer() )