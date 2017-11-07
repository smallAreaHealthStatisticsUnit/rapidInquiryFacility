
# ZAP Scanning Report

Test 1: Traditional spider


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
  
  
  
* URL: [https://localhost:8080/RIF4/](https://localhost:8080/RIF4/)
  
  
  * Method: `GET`
  
  
  * Parameter: `X-Frame-Options`
  
  
  
  
Instances: 1
  
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
  
  
  
  
* URL: [https://localhost:8080/RIF4/libs/standalone/ui-layout.css](https://localhost:8080/RIF4/libs/standalone/ui-layout.css)
  
  
  * Method: `GET`
  
  
  * Parameter: `Cache-Control`
  
  
  
  
* URL: [https://localhost:8080/RIF4/css/rifx-css-material.css](https://localhost:8080/RIF4/css/rifx-css-material.css)
  
  
  * Method: `GET`
  
  
  * Parameter: `Cache-Control`
  
  
  
  
* URL: [https://localhost:8080/RIF4/css/rifx-css-leaflet.css](https://localhost:8080/RIF4/css/rifx-css-leaflet.css)
  
  
  * Method: `GET`
  
  
  * Parameter: `Cache-Control`
  
  
  
  
* URL: [https://localhost:8080/RIF4/libs/standalone/angular-material.min.css](https://localhost:8080/RIF4/libs/standalone/angular-material.min.css)
  
  
  * Method: `GET`
  
  
  * Parameter: `Cache-Control`
  
  
  
  
* URL: [https://localhost:8080/RIF4/libs/standalone/leaflet.fullscreen.css](https://localhost:8080/RIF4/libs/standalone/leaflet.fullscreen.css)
  
  
  * Method: `GET`
  
  
  * Parameter: `Cache-Control`
  
  
  
  
* URL: [https://localhost:8080/RIF4/css/rifx-css-uigrids.css](https://localhost:8080/RIF4/css/rifx-css-uigrids.css)
  
  
  * Method: `GET`
  
  
  * Parameter: `Cache-Control`
  
  
  
  
* URL: [https://localhost:8080/RIF4/libs/leaflet-slider.css](https://localhost:8080/RIF4/libs/leaflet-slider.css)
  
  
  * Method: `GET`
  
  
  * Parameter: `Cache-Control`
  
  
  
  
* URL: [https://localhost:8080/RIF4/libs/standalone/bootstrap.min.css](https://localhost:8080/RIF4/libs/standalone/bootstrap.min.css)
  
  
  * Method: `GET`
  
  
  * Parameter: `Cache-Control`
  
  
  
  
* URL: [https://localhost:8080/RIF4/css/rifx-css-d3.css](https://localhost:8080/RIF4/css/rifx-css-d3.css)
  
  
  * Method: `GET`
  
  
  * Parameter: `Cache-Control`
  
  
  
  
* URL: [https://localhost:8080/RIF4/libs/ngNotificationsBar.css](https://localhost:8080/RIF4/libs/ngNotificationsBar.css)
  
  
  * Method: `GET`
  
  
  * Parameter: `Cache-Control`
  
  
  
  
* URL: [https://localhost:8080/RIF4/](https://localhost:8080/RIF4/)
  
  
  * Method: `GET`
  
  
  * Parameter: `Cache-Control`
  
  
  
  
* URL: [https://localhost:8080/RIF4/css/rifx-css-main.css](https://localhost:8080/RIF4/css/rifx-css-main.css)
  
  
  * Method: `GET`
  
  
  * Parameter: `Cache-Control`
  
  
  
  
* URL: [https://localhost:8080/RIF4/css/rifx-css-bootstrap.css](https://localhost:8080/RIF4/css/rifx-css-bootstrap.css)
  
  
  * Method: `GET`
  
  
  * Parameter: `Cache-Control`
  
  
  
  
* URL: [https://localhost:8080/RIF4/libs/standalone/leaflet.draw.css](https://localhost:8080/RIF4/libs/standalone/leaflet.draw.css)
  
  
  * Method: `GET`
  
  
  * Parameter: `Cache-Control`
  
  
  
  
* URL: [https://localhost:8080/RIF4/css/rifx-css-modals.css](https://localhost:8080/RIF4/css/rifx-css-modals.css)
  
  
  * Method: `GET`
  
  
  * Parameter: `Cache-Control`
  
  
  
  
* URL: [https://localhost:8080/RIF4/libs/standalone/leaflet.css](https://localhost:8080/RIF4/libs/standalone/leaflet.css)
  
  
  * Method: `GET`
  
  
  * Parameter: `Cache-Control`
  
  
  
  
* URL: [https://localhost:8080/RIF4/libs/standalone/ui-grid.min.css](https://localhost:8080/RIF4/libs/standalone/ui-grid.min.css)
  
  
  * Method: `GET`
  
  
  * Parameter: `Cache-Control`
  
  
  
  
* URL: [https://localhost:8080/RIF4/css/rifx-css-uilayout.css](https://localhost:8080/RIF4/css/rifx-css-uilayout.css)
  
  
  * Method: `GET`
  
  
  * Parameter: `Cache-Control`
  
  
  
  
Instances: 19
  
### Solution
<p>Whenever possible ensure the cache-control HTTP header is set with no-cache, no-store, must-revalidate; and that the pragma HTTP header is set with no-cache.</p>
  
### Reference
* https://www.owasp.org/index.php/Session_Management_Cheat_Sheet#Web_Content_Caching

  
#### CWE Id : 525
  
#### WASC Id : 13
  
#### Source ID : 3

  
  
  
### Web Browser XSS Protection Not Enabled
##### Low (Medium)
  
  
  
  
#### Description
<p>Web Browser XSS Protection is not enabled, or is disabled by the configuration of the 'X-XSS-Protection' HTTP response header on the web server</p>
  
  
  
* URL: [https://localhost:8080/rifServices/studyResultRetrieval/](https://localhost:8080/rifServices/studyResultRetrieval/)
  
  
  * Method: `GET`
  
  
  * Parameter: `X-XSS-Protection`
  
  
  
  
* URL: [https://localhost:8080/rifServices/studySubmission/](https://localhost:8080/rifServices/studySubmission/)
  
  
  * Method: `GET`
  
  
  * Parameter: `X-XSS-Protection`
  
  
  
  
* URL: [https://localhost:8080/RIF4/](https://localhost:8080/RIF4/)
  
  
  * Method: `GET`
  
  
  * Parameter: `X-XSS-Protection`
  
  
  
  
Instances: 3
  
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

  
  
  
### X-Content-Type-Options Header Missing
##### Low (Medium)
  
  
  
  
#### Description
<p>The Anti-MIME-Sniffing header X-Content-Type-Options was not set to 'nosniff'. This allows older versions of Internet Explorer and Chrome to perform MIME-sniffing on the response body, potentially causing the response body to be interpreted and displayed as a content type other than the declared content type. Current (early 2014) and legacy versions of Firefox will use the declared content type (if one is set), rather than performing MIME-sniffing.</p>
  
  
  
* URL: [https://localhost:8080/RIF4/dashboards/submission/directives/rifd-dsub-onreadfile.js](https://localhost:8080/RIF4/dashboards/submission/directives/rifd-dsub-onreadfile.js)
  
  
  * Method: `GET`
  
  
  * Parameter: `X-Content-Type-Options`
  
  
  
  
* URL: [https://localhost:8080/RIF4/css/rifx-css-leaflet.css](https://localhost:8080/RIF4/css/rifx-css-leaflet.css)
  
  
  * Method: `GET`
  
  
  * Parameter: `X-Content-Type-Options`
  
  
  
  
* URL: [https://localhost:8080/RIF4/utils/services/rifs-util-gis.js](https://localhost:8080/RIF4/utils/services/rifs-util-gis.js)
  
  
  * Method: `GET`
  
  
  * Parameter: `X-Content-Type-Options`
  
  
  
  
* URL: [https://localhost:8080/RIF4/libs/standalone/angular-material.min.css](https://localhost:8080/RIF4/libs/standalone/angular-material.min.css)
  
  
  * Method: `GET`
  
  
  * Parameter: `X-Content-Type-Options`
  
  
  
  
* URL: [https://localhost:8080/RIF4/utils/directives/rifd-util-info.js](https://localhost:8080/RIF4/utils/directives/rifd-util-info.js)
  
  
  * Method: `GET`
  
  
  * Parameter: `X-Content-Type-Options`
  
  
  
  
* URL: [https://localhost:8080/RIF4/libs/standalone/angular-aria.min.js](https://localhost:8080/RIF4/libs/standalone/angular-aria.min.js)
  
  
  * Method: `GET`
  
  
  * Parameter: `X-Content-Type-Options`
  
  
  
  
* URL: [https://localhost:8080/RIF4/libs/sstatistics.js](https://localhost:8080/RIF4/libs/sstatistics.js)
  
  
  * Method: `GET`
  
  
  * Parameter: `X-Content-Type-Options`
  
  
  
  
* URL: [https://localhost:8080/RIF4/libs/standalone/FileSaver.min.js](https://localhost:8080/RIF4/libs/standalone/FileSaver.min.js)
  
  
  * Method: `GET`
  
  
  * Parameter: `X-Content-Type-Options`
  
  
  
  
* URL: [https://localhost:8080/RIF4/utils/services/rifs-util-leafletdraw.js](https://localhost:8080/RIF4/utils/services/rifs-util-leafletdraw.js)
  
  
  * Method: `GET`
  
  
  * Parameter: `X-Content-Type-Options`
  
  
  
  
* URL: [https://localhost:8080/RIF4/libs/standalone/angular-material.min.js](https://localhost:8080/RIF4/libs/standalone/angular-material.min.js)
  
  
  * Method: `GET`
  
  
  * Parameter: `X-Content-Type-Options`
  
  
  
  
* URL: [https://localhost:8080/RIF4/css/rifx-css-material.css](https://localhost:8080/RIF4/css/rifx-css-material.css)
  
  
  * Method: `GET`
  
  
  * Parameter: `X-Content-Type-Options`
  
  
  
  
* URL: [https://localhost:8080/RIF4/utils/controllers/rifc-util-mapping.js](https://localhost:8080/RIF4/utils/controllers/rifc-util-mapping.js)
  
  
  * Method: `GET`
  
  
  * Parameter: `X-Content-Type-Options`
  
  
  
  
* URL: [https://localhost:8080/RIF4/libs/l.control.geosearch.js](https://localhost:8080/RIF4/libs/l.control.geosearch.js)
  
  
  * Method: `GET`
  
  
  * Parameter: `X-Content-Type-Options`
  
  
  
  
* URL: [https://localhost:8080/RIF4/dashboards/export/services/rifs-expt-exportstate.js](https://localhost:8080/RIF4/dashboards/export/services/rifs-expt-exportstate.js)
  
  
  * Method: `GET`
  
  
  * Parameter: `X-Content-Type-Options`
  
  
  
  
* URL: [https://localhost:8080/RIF4/dashboards/submission/controllers/rifc-dsub-save.js](https://localhost:8080/RIF4/dashboards/submission/controllers/rifc-dsub-save.js)
  
  
  * Method: `GET`
  
  
  * Parameter: `X-Content-Type-Options`
  
  
  
  
* URL: [https://localhost:8080/RIF4/utils/directives/rifd-util-d3histBreaks.js](https://localhost:8080/RIF4/utils/directives/rifd-util-d3histBreaks.js)
  
  
  * Method: `GET`
  
  
  * Parameter: `X-Content-Type-Options`
  
  
  
  
* URL: [https://localhost:8080/RIF4/dashboards/submission/controllers/rifc-dsub-studyarea.js](https://localhost:8080/RIF4/dashboards/submission/controllers/rifc-dsub-studyarea.js)
  
  
  * Method: `GET`
  
  
  * Parameter: `X-Content-Type-Options`
  
  
  
  
* URL: [https://localhost:8080/RIF4/dashboards/mapping/services/rifs-dmap-mappingstate.js](https://localhost:8080/RIF4/dashboards/mapping/services/rifs-dmap-mappingstate.js)
  
  
  * Method: `GET`
  
  
  * Parameter: `X-Content-Type-Options`
  
  
  
  
* URL: [https://localhost:8080/RIF4/dashboards/submission/controllers/rifc-dsub-reset.js](https://localhost:8080/RIF4/dashboards/submission/controllers/rifc-dsub-reset.js)
  
  
  * Method: `GET`
  
  
  * Parameter: `X-Content-Type-Options`
  
  
  
  
* URL: [https://localhost:8080/RIF4/libs/standalone/Leaflet.fullscreen.min.js](https://localhost:8080/RIF4/libs/standalone/Leaflet.fullscreen.min.js)
  
  
  * Method: `GET`
  
  
  * Parameter: `X-Content-Type-Options`
  
  
  
  
Instances: 110
  
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
