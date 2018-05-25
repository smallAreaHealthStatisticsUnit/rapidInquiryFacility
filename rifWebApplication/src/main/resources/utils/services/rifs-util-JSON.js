/**
 * The Rapid Inquiry Facility (RIF) is an automated tool devised by SAHSU 
 * that rapidly addresses epidemiological and public health questions using 
 * routinely collected health and population data and generates standardised 
 * rates and relative risks for any given health outcome, for specified age 
 * and year ranges, for any given geographical area.
 *
 * Copyright 2016 Imperial College London, developed by the Small Area
 * Health Statistics Unit. The work of the Small Area Health Statistics Unit 
 * is funded by the Public Health England as part of the MRC-PHE Centre for 
 * Environment and Health. Funding for this project has also been received 
 * from the United States Centers for Disease Control and Prevention.  
 *
 * This file is part of the Rapid Inquiry Facility (RIF) project.
 * RIF is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * RIF is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with RIF. If not, see <http://www.gnu.org/licenses/>; or write 
 * to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, 
 * Boston, MA 02110-1301 USA

 * David Morley
 * @author dmorley
 */

/* 
 * JSON - text conversions
 */

angular.module("RIF")
        .factory('JSONService',
                function () {
                    function CSVToArray(strData, strDelimiter) {
                        //https://jsfiddle.net/sturtevant/AZFvQ/
                        strDelimiter = (strDelimiter || ",");
                        var objPattern = new RegExp(
                                (
                                        "(\\" + strDelimiter + "|\\r?\\n|\\r|^)" +
                                        "(?:\"([^\"]*(?:\"\"[^\"]*)*)\"|" +
                                        "([^\"\\" + strDelimiter + "\\r\\n]*))"
                                        ),
                                "gi"
                                );
                        var arrData = [[]];
                        var arrMatches = null;
                        while (arrMatches = objPattern.exec(strData)) {
                            var strMatchedDelimiter = arrMatches[ 1 ];
                            if (
                                    strMatchedDelimiter.length &&
                                    strMatchedDelimiter != strDelimiter
                                    ) {
                                arrData.push([]);
                            }
                            var strMatchedValue;
                            if (arrMatches[ 2 ]) {
                                strMatchedValue = arrMatches[ 2 ].replace(
                                        new RegExp("\"\"", "g"),
                                        "\""
                                        );
                            } else {
                                strMatchedValue = arrMatches[ 3 ];

                            }
                            arrData[ arrData.length - 1 ].push(strMatchedValue);
                        }
                        return(arrData);
                    }
                    function CSV2JSON(csv) {
                        var array = CSVToArray(csv);
                        var objArray = [];
                        for (var i = 1; i < array.length; i++) {
                            objArray[i - 1] = {};
                            for (var k = 0; k < array[0].length && k < array[i].length; k++) {
                                var key = array[0][k];
                                objArray[i - 1][key] = array[i][k];
                            }
                        }
                        var json = JSON.stringify(objArray);
                        var str = json.replace(/},/g, "},\r\n");
                        return str;
                    }
                    return {
                        getCSV2JSON: function (csv) {
                            return CSV2JSON(csv);
                        }
                    };
                });