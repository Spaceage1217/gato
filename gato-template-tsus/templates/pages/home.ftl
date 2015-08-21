[#include "/gato-template/templates/includes/head.ftl"]

<!DOCTYPE HTML>
	<head>
		[@templatejs scripts=[
			'gato-templates-tsus/js/stretch-institutions.js'
		]/]
		[@templatecss files=[
			'gato-templates-tsus/css/meta-home.css'
		]/]
		[@templatehead publisher="Texas State University System"/]
	</head>
	
	<body>
		[#include "includes/topbanner.ftl"]
		<h1 id="maincontent" tabindex="-1" class="visuallyhidden">${content.title}</h1>

		<div class="slideshow-bg">
			[@cms.area name="slideshow" /]
		</div>
		<div class="tsus-home-content-bg">
			<div class="tsus-home-content">
				<div class="tsus-home-contentbox left">
					[@cms.area name="tsushomeleft" /]
				</div>
				<div class="tsus-home-contentbox middle">
					[@cms.area name="tsushomemiddle" /]
				</div>
				<div class="tsus-home-contentbox right">
					[@cms.area name="tsushomeright" /]
				</div>
			</div>
		</div>
		<ul class="tsus-institution-logos">
			[@cms.area name="tsuslogos" /]
		</ul>
		[#include "includes/footer.ftl"]
		[@googleanalytics /]
		
		[#if cmsfn.isEditMode()]
			<div id="gato-customjs-modal" class="gato-customjs-column">		
				[@cms.area name="customjs" /]
			</div>
			<div id="gato-customcss-modal" class="gato-customjs-column">
				[@cms.area name="customcss" /]
			</div>
			[@sidebarmodal skipsocial=true /]
		[/#if]

	</body>
</html>
