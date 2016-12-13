/* 
 * SERVICE to export leaflet canvas to file
 */
angular.module("RIF")
        .factory('LeafletExportService',
                function (leafletData) {
                    function exportMap(mapID, stub, legend, scale) {

                        leafletData.getMap(mapID).then(function (map) {
                            leafletImage(map, function (err, canvas) {
                                //4 canvas elements here canvas=leaflet map, legend=legend, scale=scalebar                          
                                //and a new blank canvas
                                var can4 = document.createElement('canvas');
                                can4.width = canvas.width;
                                can4.height = canvas.height;
                                var ctx4 = can4.getContext('2d');
                                ctx4.drawImage(canvas, 0, 0);

                                var pad = 10;
                                //overlay the legend
                                if (!angular.isUndefined(legend)) {
                                    if (legend.width < canvas.width / 2) {
                                        ctx4.drawImage(legend, canvas.width - legend.width - pad, pad);
                                    }
                                }
                                //overlay the scale bar
                                if (!angular.isUndefined(scale)) {
                                    if (scale.width < canvas.width / 2) {
                                        ctx4.drawImage(scale, pad, pad);
                                    }
                                }

                                var link = document.createElement('a');
                                link.id = "lf";
                                link.innerHTML = 'download image';
                                link.addEventListener('click', function (event) {
                                    link.href = can4.toDataURL();
                                    link.download = stub + ".png";
                                }, false);
                                document.body.appendChild(link);
                                link.click();
                            });
                        });
                    }
                    return {
                        getLeafletExport: function (mapID, stub, legend, scale) {
                            return exportMap(mapID, stub, legend, scale);
                        }
                    };
                });