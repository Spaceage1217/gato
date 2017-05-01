[#include "/gato-template/templates/includes/head.ftl"]

<!DOCTYPE HTML>
<html lang="en">
  <head>
    [@templatejs scripts=['gato-lib/js/jquery-ui/jquery-ui.min.js','gato-template-tsus2017/js/tsus.js']/]
    <link rel="stylesheet" type="text/css" href="${gf.resourcePath()}/gato-template-tsus2017/css/tsus-standard.compiled.css"/>
    [@templatehead publisher="Texas State University System"/]
  </head>
  <body>
    [#include "includes/header.ftl"]
    <div class="container standard-container">
        <div class="page_content">
            [#assign hideSidebar = content.hideSidebar!false]
            [@cms.area name="contentParagraph" contextAttributes={"hideSidebar":hideSidebar} /]
            [#if hideSidebar == false]
            <aside class="sidebar">
                <div class="sidebar-section">
                    <h3 class="sidebar-section-title">
                        Categories
                    </h3>
                    <ul class="categories">
                        [@cms.area name="categories" /]
                    </ul>
                </div>
                <div class="sidebar-section">
                    <h3 class="sidebar-section-title">
                       Staff Directory
                    </h3>
                </div>
                <div class="sidebar-section">
                    <h3 class="sidebar-section-title">
                       Quick Links
                    </h3>
                    <ul class="quick-links">
                        [@cms.area name="quick-links" /]
                    </ul>
                </div>
                <div class="sidebar-section">
                    <h3 class="sidebar-section-title">
                       Contact
                    </h3>
                </div>
            </aside>
            [/#if]
        </div>
    </div>
    [#include "includes/footer.ftl"]
    <!-- if the sidebar is hidden, add a sidebar modal so they can edit it for child pages if necessary.  
    See 2015 template and old TSUS template -->
    [@cssjsmodals /]
  </body>
</html>