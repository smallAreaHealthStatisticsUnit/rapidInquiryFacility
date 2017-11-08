
# ZAP Scanning Report




## Summary of Alerts

| Risk Level | Number of Alerts |
| --- | --- |
| High | 0 |
| Medium | 1 |
| Low | 3 |
| Informational | 0 |

## Alert Detail


  
  
  
### X-Frame-Options Header Not Set
##### Medium (Medium)
  
  
  
  
#### Description
<p>X-Frame-Options header is not included in the HTTP response to protect against 'ClickJacking' attacks.</p>
  
  
  
* URL: [https://localhost:8080/RIF4/dashboards/submission/partials/rifp-dsub-stats.html](https://localhost:8080/RIF4/dashboards/submission/partials/rifp-dsub-stats.html)
  
  
  * Method: `GET`
  
  
  * Parameter: `X-Frame-Options`
  
  
  
  
* URL: [https://localhost:8080/RIF4/dashboards/submission/partials/rifp-dsub-studyarea.html](https://localhost:8080/RIF4/dashboards/submission/partials/rifp-dsub-studyarea.html)
  
  
  * Method: `GET`
  
  
  * Parameter: `X-Frame-Options`
  
  
  
  
* URL: [https://localhost:8080/RIF4/dashboards/submission/partials/rifp-dsub-maptable.html](https://localhost:8080/RIF4/dashboards/submission/partials/rifp-dsub-maptable.html)
  
  
  * Method: `GET`
  
  
  * Parameter: `X-Frame-Options`
  
  
  
  
* URL: [https://localhost:8080/RIF4/dashboards/viewer/partials/rifp-view-main.html](https://localhost:8080/RIF4/dashboards/viewer/partials/rifp-view-main.html)
  
  
  * Method: `GET`
  
  
  * Parameter: `X-Frame-Options`
  
  
  
  
* URL: [https://localhost:8080/RIF4/dashboards/export/partials/rifp-expt-main.html](https://localhost:8080/RIF4/dashboards/export/partials/rifp-expt-main.html)
  
  
  * Method: `GET`
  
  
  * Parameter: `X-Frame-Options`
  
  
  
  
* URL: [https://localhost:8080/RIF4/dashboards/submission/partials/rifp-dsub-fromfile.html](https://localhost:8080/RIF4/dashboards/submission/partials/rifp-dsub-fromfile.html)
  
  
  * Method: `GET`
  
  
  * Parameter: `X-Frame-Options`
  
  
  
  
* URL: [https://localhost:8080/RIF4/](https://localhost:8080/RIF4/)
  
  
  * Method: `GET`
  
  
  * Parameter: `X-Frame-Options`
  
  
  
  
* URL: [https://localhost:8080/RIF4/utils/partials/rifp-util-yesno.html](https://localhost:8080/RIF4/utils/partials/rifp-util-yesno.html)
  
  
  * Method: `GET`
  
  
  * Parameter: `X-Frame-Options`
  
  
  
  
* URL: [https://localhost:8080/RIF4/dashboards/submission/partials/rifp-dsub-params.html](https://localhost:8080/RIF4/dashboards/submission/partials/rifp-dsub-params.html)
  
  
  * Method: `GET`
  
  
  * Parameter: `X-Frame-Options`
  
  
  
  
* URL: [https://localhost:8080/RIF4](https://localhost:8080/RIF4)
  
  
  * Method: `GET`
  
  
  * Parameter: `X-Frame-Options`
  
  
  
  
* URL: [https://localhost:8080/RIF4/dashboards/submission/partials/rifp-dsub-status.html](https://localhost:8080/RIF4/dashboards/submission/partials/rifp-dsub-status.html)
  
  
  * Method: `GET`
  
  
  * Parameter: `X-Frame-Options`
  
  
  
  
* URL: [https://localhost:8080/RIF4/dashboards/submission/partials/rifp-dsub-comparea.html](https://localhost:8080/RIF4/dashboards/submission/partials/rifp-dsub-comparea.html)
  
  
  * Method: `GET`
  
  
  * Parameter: `X-Frame-Options`
  
  
  
  
* URL: [https://localhost:8080/RIF4/dashboards/mapping/partials/rifp-dmap-main.html](https://localhost:8080/RIF4/dashboards/mapping/partials/rifp-dmap-main.html)
  
  
  * Method: `GET`
  
  
  * Parameter: `X-Frame-Options`
  
  
  
  
* URL: [https://localhost:8080/RIF4/dashboards/submission/partials/rifp-dsub-trace.html](https://localhost:8080/RIF4/dashboards/submission/partials/rifp-dsub-trace.html)
  
  
  * Method: `GET`
  
  
  * Parameter: `X-Frame-Options`
  
  
  
  
* URL: [https://localhost:8080/RIF4/utils/partials/rifp-util-choro.html](https://localhost:8080/RIF4/utils/partials/rifp-util-choro.html)
  
  
  * Method: `GET`
  
  
  * Parameter: `X-Frame-Options`
  
  
  
  
* URL: [https://localhost:8080/RIF4/dashboards/submission/partials/rifp-dsub-runstudy.html](https://localhost:8080/RIF4/dashboards/submission/partials/rifp-dsub-runstudy.html)
  
  
  * Method: `GET`
  
  
  * Parameter: `X-Frame-Options`
  
  
  
  
* URL: [https://localhost:8080/RIF4/dashboards/submission/partials/rifp-dsub-main.html](https://localhost:8080/RIF4/dashboards/submission/partials/rifp-dsub-main.html)
  
  
  * Method: `GET`
  
  
  * Parameter: `X-Frame-Options`
  
  
  
  
* URL: [https://localhost:8080/RIF4/dashboards/login/partials/rifp-login-main.html](https://localhost:8080/RIF4/dashboards/login/partials/rifp-login-main.html)
  
  
  * Method: `GET`
  
  
  * Parameter: `X-Frame-Options`
  
  
  
  
Instances: 18
  
### Solution
<p>Most modern Web browsers support the X-Frame-Options HTTP header. Ensure it's set on all web pages returned by your site (if you expect the page to be framed only by pages on your server (e.g. it's part of a FRAMESET) then you'll want to use SAMEORIGIN, otherwise if you never expect the page to be framed, you should use DENY. ALLOW-FROM allows specific websites to frame the web page in supported web browsers).</p>
  
### Reference
* http://blogs.msdn.com/b/ieinternals/archive/2010/03/30/combating-clickjacking-with-x-frame-options.aspx

  
#### CWE Id : 16
  
#### WASC Id : 15
  
#### Source ID : 3

  
  
  
### Incomplete or No Cache-control and Pragma HTTP Header Set
##### Low (Medium)
  
  
  
  
#### Description
<p>The cache-control and pragma HTTP header have not been set properly or are missing allowing the browser and proxies to cache content.</p>
  
  
  
* URL: [https://localhost:8080/RIF4/libs/l.geosearch.css](https://localhost:8080/RIF4/libs/l.geosearch.css)
  
  
  * Method: `GET`
  
  
  * Parameter: `Cache-Control`
  
  
  
  
* URL: [https://localhost:8080/rifServices/studyResultRetrieval/ms/getTileMakerTiles?userID=peter&geographyName=SAHSULAND&geoLevelSelectName=SAHSU_GRD_LEVEL4&zoomlevel=7&x=63&y=42](https://localhost:8080/rifServices/studyResultRetrieval/ms/getTileMakerTiles?userID=peter&geographyName=SAHSULAND&geoLevelSelectName=SAHSU_GRD_LEVEL4&zoomlevel=7&x=63&y=42)
  
  
  * Method: `GET`
  
  
  * Parameter: `Cache-Control`
  
  
  
  
* URL: [https://localhost:8080/rifServices/studyResultRetrieval/ms/getTileMakerTiles?userID=peter&geographyName=SAHSULAND&geoLevelSelectName=SAHSU_GRD_LEVEL4&zoomlevel=3&x=0&y=4](https://localhost:8080/rifServices/studyResultRetrieval/ms/getTileMakerTiles?userID=peter&geographyName=SAHSULAND&geoLevelSelectName=SAHSU_GRD_LEVEL4&zoomlevel=3&x=0&y=4)
  
  
  * Method: `GET`
  
  
  * Parameter: `Cache-Control`
  
  
  
  
* URL: [https://localhost:8080/rifServices/studyResultRetrieval/ms/getTileMakerTiles?userID=peter&geographyName=SAHSULAND&geoLevelSelectName=SAHSU_GRD_LEVEL4&zoomlevel=7&x=63&y=41](https://localhost:8080/rifServices/studyResultRetrieval/ms/getTileMakerTiles?userID=peter&geographyName=SAHSULAND&geoLevelSelectName=SAHSU_GRD_LEVEL4&zoomlevel=7&x=63&y=41)
  
  
  * Method: `GET`
  
  
  * Parameter: `Cache-Control`
  
  
  
  
* URL: [https://localhost:8080/RIF4/libs/standalone/angular-material.min.css](https://localhost:8080/RIF4/libs/standalone/angular-material.min.css)
  
  
  * Method: `GET`
  
  
  * Parameter: `Cache-Control`
  
  
  
  
* URL: [https://localhost:8080/rifServices/studyResultRetrieval/ms/getTileMakerTiles?userID=peter&geographyName=SAHSULAND&geoLevelSelectName=SAHSU_GRD_LEVEL4&zoomlevel=6&x=31&y=19](https://localhost:8080/rifServices/studyResultRetrieval/ms/getTileMakerTiles?userID=peter&geographyName=SAHSULAND&geoLevelSelectName=SAHSU_GRD_LEVEL4&zoomlevel=6&x=31&y=19)
  
  
  * Method: `GET`
  
  
  * Parameter: `Cache-Control`
  
  
  
  
* URL: [https://localhost:8080/rifServices/studyResultRetrieval/ms/getYearsForStudy?userID=peter&study_id=5](https://localhost:8080/rifServices/studyResultRetrieval/ms/getYearsForStudy?userID=peter&study_id=5)
  
  
  * Method: `GET`
  
  
  * Parameter: `Cache-Control`
  
  
  
  
* URL: [https://localhost:8080/RIF4/dashboards/submission/partials/rifp-dsub-comparea.html](https://localhost:8080/RIF4/dashboards/submission/partials/rifp-dsub-comparea.html)
  
  
  * Method: `GET`
  
  
  * Parameter: `Cache-Control`
  
  
  
  
* URL: [https://localhost:8080/rifServices/studySubmission/ms/createZipFile?userID=peter&studyID=6&zoomLevel=9](https://localhost:8080/rifServices/studySubmission/ms/createZipFile?userID=peter&studyID=6&zoomLevel=9)
  
  
  * Method: `GET`
  
  
  * Parameter: `Cache-Control`
  
  
  
  
* URL: [https://localhost:8080/rifServices/studyResultRetrieval/ms/getTileMakerTiles?userID=peter&geographyName=SAHSULAND&geoLevelSelectName=SAHSU_GRD_LEVEL1&zoomlevel=7&x=61&y=39](https://localhost:8080/rifServices/studyResultRetrieval/ms/getTileMakerTiles?userID=peter&geographyName=SAHSULAND&geoLevelSelectName=SAHSU_GRD_LEVEL1&zoomlevel=7&x=61&y=39)
  
  
  * Method: `GET`
  
  
  * Parameter: `Cache-Control`
  
  
  
  
* URL: [https://localhost:8080/RIF4/libs/standalone/bootstrap.min.css](https://localhost:8080/RIF4/libs/standalone/bootstrap.min.css)
  
  
  * Method: `GET`
  
  
  * Parameter: `Cache-Control`
  
  
  
  
* URL: [https://localhost:8080/rifServices/studyResultRetrieval/ms/getTileMakerTiles?userID=peter&geographyName=USA_2014&geoLevelSelectName=CB_2014_US_COUNTY_500K&zoomlevel=3&x=0&y=4](https://localhost:8080/rifServices/studyResultRetrieval/ms/getTileMakerTiles?userID=peter&geographyName=USA_2014&geoLevelSelectName=CB_2014_US_COUNTY_500K&zoomlevel=3&x=0&y=4)
  
  
  * Method: `GET`
  
  
  * Parameter: `Cache-Control`
  
  
  
  
* URL: [https://localhost:8080/rifServices/studyResultRetrieval/ms/getTileMakerTiles?userID=peter&geographyName=USA_2014&geoLevelSelectName=CB_2014_US_COUNTY_500K&zoomlevel=4&x=1&y=6](https://localhost:8080/rifServices/studyResultRetrieval/ms/getTileMakerTiles?userID=peter&geographyName=USA_2014&geoLevelSelectName=CB_2014_US_COUNTY_500K&zoomlevel=4&x=1&y=6)
  
  
  * Method: `GET`
  
  
  * Parameter: `Cache-Control`
  
  
  
  
* URL: [https://localhost:8080/rifServices/studyResultRetrieval/ms/getStudyTableForProcessedStudy?userID=peter&studyID=5&type=results&stt=1&stp=100](https://localhost:8080/rifServices/studyResultRetrieval/ms/getStudyTableForProcessedStudy?userID=peter&studyID=5&type=results&stt=1&stp=100)
  
  
  * Method: `GET`
  
  
  * Parameter: `Cache-Control`
  
  
  
  
* URL: [https://localhost:8080/RIF4/libs/ngNotificationsBar.css](https://localhost:8080/RIF4/libs/ngNotificationsBar.css)
  
  
  * Method: `GET`
  
  
  * Parameter: `Cache-Control`
  
  
  
  
* URL: [https://localhost:8080/RIF4/](https://localhost:8080/RIF4/)
  
  
  * Method: `GET`
  
  
  * Parameter: `Cache-Control`
  
  
  
  
* URL: [https://localhost:8080/rifServices/studyResultRetrieval/ms/getTileMakerTiles?userID=peter&geographyName=USA_2014&geoLevelSelectName=CB_2014_US_COUNTY_500K&zoomlevel=4&x=4&y=5](https://localhost:8080/rifServices/studyResultRetrieval/ms/getTileMakerTiles?userID=peter&geographyName=USA_2014&geoLevelSelectName=CB_2014_US_COUNTY_500K&zoomlevel=4&x=4&y=5)
  
  
  * Method: `GET`
  
  
  * Parameter: `Cache-Control`
  
  
  
  
* URL: [https://localhost:8080/rifServices/studyResultRetrieval/ms/getTileMakerTiles?userID=peter&geographyName=USA_2014&geoLevelSelectName=CB_2014_US_COUNTY_500K&zoomlevel=3&x=0&y=3](https://localhost:8080/rifServices/studyResultRetrieval/ms/getTileMakerTiles?userID=peter&geographyName=USA_2014&geoLevelSelectName=CB_2014_US_COUNTY_500K&zoomlevel=3&x=0&y=3)
  
  
  * Method: `GET`
  
  
  * Parameter: `Cache-Control`
  
  
  
  
* URL: [https://localhost:8080/rifServices/studyResultRetrieval/ms/getTileMakerTiles?userID=peter&geographyName=USA_2014&geoLevelSelectName=CB_2014_US_COUNTY_500K&zoomlevel=4&x=1&y=5](https://localhost:8080/rifServices/studyResultRetrieval/ms/getTileMakerTiles?userID=peter&geographyName=USA_2014&geoLevelSelectName=CB_2014_US_COUNTY_500K&zoomlevel=4&x=1&y=5)
  
  
  * Method: `GET`
  
  
  * Parameter: `Cache-Control`
  
  
  
  
* URL: [https://localhost:8080/RIF4/utils/partials/rifp-util-choro.html](https://localhost:8080/RIF4/utils/partials/rifp-util-choro.html)
  
  
  * Method: `GET`
  
  
  * Parameter: `Cache-Control`
  
  
  
  
Instances: 190
  
### Solution
<p>Whenever possible ensure the cache-control HTTP header is set with no-cache, no-store, must-revalidate; and that the pragma HTTP header is set with no-cache.</p>
  
### Reference
* https://www.owasp.org/index.php/Session_Management_Cheat_Sheet#Web_Content_Caching

  
#### CWE Id : 525
  
#### WASC Id : 13
  
#### Source ID : 3

  
  
  
### X-Content-Type-Options Header Missing
##### Low (Medium)
  
  
  
  
#### Description
<p>The Anti-MIME-Sniffing header X-Content-Type-Options was not set to 'nosniff'. This allows older versions of Internet Explorer and Chrome to perform MIME-sniffing on the response body, potentially causing the response body to be interpreted and displayed as a content type other than the declared content type. Current (early 2014) and legacy versions of Firefox will use the declared content type (if one is set), rather than performing MIME-sniffing.</p>
  
  
  
* URL: [https://localhost:8080/RIF4/dashboards/submission/directives/rifd-dsub-onreadfile.js](https://localhost:8080/RIF4/dashboards/submission/directives/rifd-dsub-onreadfile.js)
  
  
  * Method: `GET`
  
  
  * Parameter: `X-Content-Type-Options`
  
  
  
  
* URL: [https://localhost:8080/RIF4/images/colorBrewer/PuOr.png](https://localhost:8080/RIF4/images/colorBrewer/PuOr.png)
  
  
  * Method: `GET`
  
  
  * Parameter: `X-Content-Type-Options`
  
  
  
  
* URL: [https://localhost:8080/RIF4/libs/standalone/angular-material.min.css](https://localhost:8080/RIF4/libs/standalone/angular-material.min.css)
  
  
  * Method: `GET`
  
  
  * Parameter: `X-Content-Type-Options`
  
  
  
  
* URL: [https://localhost:8080/RIF4/images/colorBrewer/Blues.png](https://localhost:8080/RIF4/images/colorBrewer/Blues.png)
  
  
  * Method: `GET`
  
  
  * Parameter: `X-Content-Type-Options`
  
  
  
  
* URL: [https://localhost:8080/rifServices/studyResultRetrieval/ms/getTileMakerTiles?userID=peter&geographyName=SAHSULAND&geoLevelSelectName=SAHSU_GRD_LEVEL1&zoomlevel=7&x=60&y=40](https://localhost:8080/rifServices/studyResultRetrieval/ms/getTileMakerTiles?userID=peter&geographyName=SAHSULAND&geoLevelSelectName=SAHSU_GRD_LEVEL1&zoomlevel=7&x=60&y=40)
  
  
  * Method: `GET`
  
  
  * Parameter: `X-Content-Type-Options`
  
  
  
  
* URL: [https://localhost:8080/RIF4/libs/standalone/FileSaver.min.js](https://localhost:8080/RIF4/libs/standalone/FileSaver.min.js)
  
  
  * Method: `GET`
  
  
  * Parameter: `X-Content-Type-Options`
  
  
  
  
* URL: [https://localhost:8080/RIF4/images/glyphicon/glyphicons-82-refresh.png](https://localhost:8080/RIF4/images/glyphicon/glyphicons-82-refresh.png)
  
  
  * Method: `GET`
  
  
  * Parameter: `X-Content-Type-Options`
  
  
  
  
* URL: [https://localhost:8080/RIF4/utils/controllers/rifc-util-choro.js](https://localhost:8080/RIF4/utils/controllers/rifc-util-choro.js)
  
  
  * Method: `GET`
  
  
  * Parameter: `X-Content-Type-Options`
  
  
  
  
* URL: [https://localhost:8080/RIF4/images/glyphicon/glyphicons-444-floppy-disk.png](https://localhost:8080/RIF4/images/glyphicon/glyphicons-444-floppy-disk.png)
  
  
  * Method: `GET`
  
  
  * Parameter: `X-Content-Type-Options`
  
  
  
  
* URL: [https://localhost:8080/rifServices/studyResultRetrieval/ms/getTileMakerTiles?userID=peter&geographyName=SAHSULAND&geoLevelSelectName=SAHSU_GRD_LEVEL1&zoomlevel=7&x=60&y=41](https://localhost:8080/rifServices/studyResultRetrieval/ms/getTileMakerTiles?userID=peter&geographyName=SAHSULAND&geoLevelSelectName=SAHSU_GRD_LEVEL1&zoomlevel=7&x=60&y=41)
  
  
  * Method: `GET`
  
  
  * Parameter: `X-Content-Type-Options`
  
  
  
  
* URL: [https://localhost:8080/RIF4/images/colorBrewer/Set3.png](https://localhost:8080/RIF4/images/colorBrewer/Set3.png)
  
  
  * Method: `GET`
  
  
  * Parameter: `X-Content-Type-Options`
  
  
  
  
* URL: [https://localhost:8080/RIF4/dashboards/submission/partials/rifp-dsub-comparea.html](https://localhost:8080/RIF4/dashboards/submission/partials/rifp-dsub-comparea.html)
  
  
  * Method: `GET`
  
  
  * Parameter: `X-Content-Type-Options`
  
  
  
  
* URL: [https://localhost:8080/RIF4/libs/l.geosearch.css](https://localhost:8080/RIF4/libs/l.geosearch.css)
  
  
  * Method: `GET`
  
  
  * Parameter: `X-Content-Type-Options`
  
  
  
  
* URL: [https://localhost:8080/RIF4/utils/services/rifs-util-basemap.js](https://localhost:8080/RIF4/utils/services/rifs-util-basemap.js)
  
  
  * Method: `GET`
  
  
  * Parameter: `X-Content-Type-Options`
  
  
  
  
* URL: [https://localhost:8080/RIF4/dashboards/submission/controllers/rifc-dsub-comparea.js](https://localhost:8080/RIF4/dashboards/submission/controllers/rifc-dsub-comparea.js)
  
  
  * Method: `GET`
  
  
  * Parameter: `X-Content-Type-Options`
  
  
  
  
* URL: [https://localhost:8080/RIF4/libs/standalone/fullscreen.png](https://localhost:8080/RIF4/libs/standalone/fullscreen.png)
  
  
  * Method: `GET`
  
  
  * Parameter: `X-Content-Type-Options`
  
  
  
  
* URL: [https://localhost:8080/rifServices/studyResultRetrieval/ms/getTileMakerTiles?userID=peter&geographyName=SAHSULAND&geoLevelSelectName=SAHSU_GRD_LEVEL4&zoomlevel=7&x=61&y=39](https://localhost:8080/rifServices/studyResultRetrieval/ms/getTileMakerTiles?userID=peter&geographyName=SAHSULAND&geoLevelSelectName=SAHSU_GRD_LEVEL4&zoomlevel=7&x=61&y=39)
  
  
  * Method: `GET`
  
  
  * Parameter: `X-Content-Type-Options`
  
  
  
  
* URL: [https://localhost:8080/rifServices/studyResultRetrieval/ms/getTileMakerTiles?userID=peter&geographyName=SAHSULAND&geoLevelSelectName=SAHSU_GRD_LEVEL4&zoomlevel=3&x=0&y=4](https://localhost:8080/rifServices/studyResultRetrieval/ms/getTileMakerTiles?userID=peter&geographyName=SAHSULAND&geoLevelSelectName=SAHSU_GRD_LEVEL4&zoomlevel=3&x=0&y=4)
  
  
  * Method: `GET`
  
  
  * Parameter: `X-Content-Type-Options`
  
  
  
  
* URL: [https://localhost:8080/rifServices/studyResultRetrieval/ms/getTileMakerTiles?userID=peter&geographyName=SAHSULAND&geoLevelSelectName=SAHSU_GRD_LEVEL1&zoomlevel=7&x=60&y=42](https://localhost:8080/rifServices/studyResultRetrieval/ms/getTileMakerTiles?userID=peter&geographyName=SAHSULAND&geoLevelSelectName=SAHSU_GRD_LEVEL1&zoomlevel=7&x=60&y=42)
  
  
  * Method: `GET`
  
  
  * Parameter: `X-Content-Type-Options`
  
  
  
  
* URL: [https://localhost:8080/rifServices/studyResultRetrieval/ms/getTileMakerTiles?userID=peter&geographyName=SAHSULAND&geoLevelSelectName=SAHSU_GRD_LEVEL1&zoomlevel=7&x=61&y=39](https://localhost:8080/rifServices/studyResultRetrieval/ms/getTileMakerTiles?userID=peter&geographyName=SAHSULAND&geoLevelSelectName=SAHSU_GRD_LEVEL1&zoomlevel=7&x=61&y=39)
  
  
  * Method: `GET`
  
  
  * Parameter: `X-Content-Type-Options`
  
  
  
  
Instances: 352
  
### Solution
<p>Ensure that the application/web server sets the Content-Type header appropriately, and that it sets the X-Content-Type-Options header to 'nosniff' for all web pages.</p><p>If possible, ensure that the end user uses a standards-compliant and modern web browser that does not perform MIME-sniffing at all, or that can be directed by the web application/web server to not perform MIME-sniffing.</p>
  
### Other information
<p>This issue still applies to error type pages (401, 403, 500, etc) as those pages are often still affected by injection issues, in which case there is still concern for browsers sniffing pages away from their actual content type.</p><p>At "High" threshold this scanner will not alert on client or server error responses.</p>
  
### Reference
* http://msdn.microsoft.com/en-us/library/ie/gg622941%28v=vs.85%29.aspx
* https://www.owasp.org/index.php/List_of_useful_HTTP_headers

  
#### CWE Id : 16
  
#### WASC Id : 15
  
#### Source ID : 3

  
  
  
### Web Browser XSS Protection Not Enabled
##### Low (Medium)
  
  
  
  
#### Description
<p>Web Browser XSS Protection is not enabled, or is disabled by the configuration of the 'X-XSS-Protection' HTTP response header on the web server</p>
  
  
  
* URL: [https://localhost:8080/RIF4/dashboards/submission/](https://localhost:8080/RIF4/dashboards/submission/)
  
  
  * Method: `GET`
  
  
  * Parameter: `X-XSS-Protection`
  
  
  
  
* URL: [https://localhost:8080/RIF4/dashboards/submission/partials/rifp-dsub-status.html](https://localhost:8080/RIF4/dashboards/submission/partials/rifp-dsub-status.html)
  
  
  * Method: `GET`
  
  
  * Parameter: `X-XSS-Protection`
  
  
  
  
* URL: [https://localhost:8080/RIF4/dashboards/viewer/controllers/](https://localhost:8080/RIF4/dashboards/viewer/controllers/)
  
  
  * Method: `GET`
  
  
  * Parameter: `X-XSS-Protection`
  
  
  
  
* URL: [https://localhost:8080/RIF4/dashboards/viewer/partials/](https://localhost:8080/RIF4/dashboards/viewer/partials/)
  
  
  * Method: `GET`
  
  
  * Parameter: `X-XSS-Protection`
  
  
  
  
* URL: [https://localhost:8080/RIF4/utils/partials/rifp-util-yesno.html](https://localhost:8080/RIF4/utils/partials/rifp-util-yesno.html)
  
  
  * Method: `GET`
  
  
  * Parameter: `X-XSS-Protection`
  
  
  
  
* URL: [https://localhost:8080/RIF4/dashboards/login/](https://localhost:8080/RIF4/dashboards/login/)
  
  
  * Method: `GET`
  
  
  * Parameter: `X-XSS-Protection`
  
  
  
  
* URL: [https://localhost:8080/RIF4/dashboards/mapping/directives/](https://localhost:8080/RIF4/dashboards/mapping/directives/)
  
  
  * Method: `GET`
  
  
  * Parameter: `X-XSS-Protection`
  
  
  
  
* URL: [https://localhost:8080/RIF4](https://localhost:8080/RIF4)
  
  
  * Method: `GET`
  
  
  * Parameter: `X-XSS-Protection`
  
  
  
  
* URL: [https://localhost:8080/RIF4/css/](https://localhost:8080/RIF4/css/)
  
  
  * Method: `GET`
  
  
  * Parameter: `X-XSS-Protection`
  
  
  
  
* URL: [https://localhost:8080/rifServices/studySubmission/](https://localhost:8080/rifServices/studySubmission/)
  
  
  * Method: `GET`
  
  
  * Parameter: `X-XSS-Protection`
  
  
  
  
* URL: [https://localhost:8080/RIF4/dashboards/submission/partials/rifp-dsub-fromfile.html](https://localhost:8080/RIF4/dashboards/submission/partials/rifp-dsub-fromfile.html)
  
  
  * Method: `GET`
  
  
  * Parameter: `X-XSS-Protection`
  
  
  
  
* URL: [https://localhost:8080/RIF4/dashboards/mapping/partials/rifp-dmap-main.html](https://localhost:8080/RIF4/dashboards/mapping/partials/rifp-dmap-main.html)
  
  
  * Method: `GET`
  
  
  * Parameter: `X-XSS-Protection`
  
  
  
  
* URL: [https://localhost:8080/RIF4/backend/](https://localhost:8080/RIF4/backend/)
  
  
  * Method: `GET`
  
  
  * Parameter: `X-XSS-Protection`
  
  
  
  
* URL: [https://localhost:8080/RIF4/dashboards/submission/partials/rifp-dsub-comparea.html](https://localhost:8080/RIF4/dashboards/submission/partials/rifp-dsub-comparea.html)
  
  
  * Method: `GET`
  
  
  * Parameter: `X-XSS-Protection`
  
  
  
  
* URL: [https://localhost:8080/RIF4/dashboards/login/partials/](https://localhost:8080/RIF4/dashboards/login/partials/)
  
  
  * Method: `GET`
  
  
  * Parameter: `X-XSS-Protection`
  
  
  
  
* URL: [https://localhost:8080/RIF4/images/trees/](https://localhost:8080/RIF4/images/trees/)
  
  
  * Method: `GET`
  
  
  * Parameter: `X-XSS-Protection`
  
  
  
  
* URL: [https://localhost:8080/RIF4/utils/services/](https://localhost:8080/RIF4/utils/services/)
  
  
  * Method: `GET`
  
  
  * Parameter: `X-XSS-Protection`
  
  
  
  
* URL: [https://localhost:8080/RIF4/dashboards/login/controllers/](https://localhost:8080/RIF4/dashboards/login/controllers/)
  
  
  * Method: `GET`
  
  
  * Parameter: `X-XSS-Protection`
  
  
  
  
* URL: [https://localhost:8080/RIF4/dashboards/mapping/partials/](https://localhost:8080/RIF4/dashboards/mapping/partials/)
  
  
  * Method: `GET`
  
  
  * Parameter: `X-XSS-Protection`
  
  
  
  
* URL: [https://localhost:8080/RIF4/dashboards/submission/partials/rifp-dsub-trace.html](https://localhost:8080/RIF4/dashboards/submission/partials/rifp-dsub-trace.html)
  
  
  * Method: `GET`
  
  
  * Parameter: `X-XSS-Protection`
  
  
  
  
Instances: 64
  
### Solution
<p>Ensure that the web browser's XSS filter is enabled, by setting the X-XSS-Protection HTTP response header to '1'.</p>
  
### Other information
<p>The X-XSS-Protection HTTP response header allows the web server to enable or disable the web browser's XSS protection mechanism. The following values would attempt to enable it: </p><p>X-XSS-Protection: 1; mode=block</p><p>X-XSS-Protection: 1; report=http://www.example.com/xss</p><p>The following values would disable it:</p><p>X-XSS-Protection: 0</p><p>The X-XSS-Protection HTTP response header is currently supported on Internet Explorer, Chrome and Safari (WebKit).</p><p>Note that this alert is only raised if the response body could potentially contain an XSS payload (with a text-based content type, with a non-zero length).</p>
  
### Reference
* https://www.owasp.org/index.php/XSS_(Cross_Site_Scripting)_Prevention_Cheat_Sheet
* https://blog.veracode.com/2014/03/guidelines-for-setting-security-headers/

  
#### CWE Id : 933
  
#### WASC Id : 14
  
#### Source ID : 3
