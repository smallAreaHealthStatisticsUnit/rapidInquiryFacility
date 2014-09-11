var beautify = require( 'js-beautify' ).js_beautify,
  fs = require( 'fs' );

fs.readFile( 'components/map/mapfoo.js', 'utf8', function( err, data ) {
  if ( err ) {
    throw err;
  }
  console.log( beautify( data, {
    indent_size: 2
  } ) );
} );