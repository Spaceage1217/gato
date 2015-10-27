[#include "/gato-template/templates/includes/head.ftl"]

<!DOCTYPE HTML>
<html>
<head>
  [@templatejs scripts=[]/]
  <link rel="stylesheet" type="text/css" href="${gf.resourcePath()}/gato-template-ua/css/standard.compiled.css"/>
  [@templatehead /]
</head>

<body class="${cmsfn.isEditMode()?string('admin', '')}">
  <div id="ua-frame">
    [#include "../includes/header.ftl"]
    <div class="ua-banner">
      <div class="ua-banner-content">
        [@cms.area name="banners" /]
      </div>
    </div>
		<div class="ua-breadcrumbs-wide">
      [@breadcrumbs/]
    </div>

    <div class="gato-columnparent ${(content.hideSidebar!false)?string('gato-fullwidth','')}">
    	[#if !(content.hideSidebar!false)]
        <nav class="gato-navcolumn">
          [@cms.area name="navBlocks" /]
        </nav>
      [/#if]

      <div class="gato-contentcolumn txst-styledcontent">
				[#if content.title?has_content && !isHomePage]
					<h1 class="gato-page-title">${content.title}</h1>
				[/#if]
        [#if def.parameters.isMailTemplate!false]
          [@cms.area name="mail" /]
        [#else]
          [@cms.area name="contentParagraph" /]
        [/#if]
      </div>
    </div>
		[#include "../includes/footer.ftl"]
  </div>
  [@cssjsmodals /]
</body>
</html>
