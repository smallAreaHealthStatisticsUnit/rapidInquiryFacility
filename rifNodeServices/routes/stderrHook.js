// ************************************************************************
//
// GIT Header
//
// $Format:Git ID: (%h) %ci$
// $Id: 7ccec3471201c4da4d181af6faef06a362b29526 $
// Version hash: $Format:%H$
//
// Description:
//
// Rapid Enquiry Facility (RIF) - stderr hook: see https://gist.github.com/stringparser/b539b8cfd5769542037d
//                                for the stdout hook this module is derived from
//
// Copyright:
//
// The Rapid Inquiry Facility (RIF) is an automated tool devised by SAHSU 
// that rapidly addresses epidemiological and public health questions using 
// routinely collected health and population data and generates standardised 
// rates and relative risks for any given health outcome, for specified age 
// and year ranges, for any given geographical area.
//
// Copyright 2014 Imperial College London, developed by the Small Area
// Health Statistics Unit. The work of the Small Area Health Statistics Unit 
// is funded by the Public Health England as part of the MRC-PHE Centre for 
// Environment and Health. Funding for this project has also been received 
// from the Centers for Disease Control and Prevention.  
//
// This file is part of the Rapid Inquiry Facility (RIF) project.
// RIF is free software: you can redistribute it and/or modify
// it under the terms of the GNU Lesser General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// RIF is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public License
// along with RIF. If not, see <http://www.gnu.org/licenses/>; or write 
// to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, 
// Boston, MA 02110-1301 USA
//
// Author:
//
// Peter Hambly, SAHSU; copied from original stdout hook by: Javier Carrillo

var stderrHook = function (callback) {

  var oldWrite = process.stderr.write;
  var output = { str : '' };

  return {
    restore : function(){
      process.stderr.write = oldWrite;
      return this;
    },
    disable : function(){
      var self = this;
      process.stderr.write = (function(){
        return function(str, enc, fd){
          callback.call(self, output, { str : str, enc : enc, fd : fd });
        };
      })();
      return this;
    },
    enable : function(){
      var self = this;
      process.stderr.write = (function(write){
        return function(str, enc, fd){
          write.apply(process.stderr, arguments);
          callback.call(self, output, { str : str, enc : enc, fd : fd });
        };
      })(oldWrite);
    },
    output : function(){
        return output;
    },
    str : function(){
      return this.output().str;
    },
    clean : function(){
      output = { str : '' };
      return this;
    },
    reset : function(){
      return this.disable().clean().enable();
    }
  };
}

module.exports.stderrHook = stderrHook;
