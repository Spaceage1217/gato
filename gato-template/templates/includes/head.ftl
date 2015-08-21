[#include "/gato-lib/templates/includes/head.ftl"]

[#macro templatejs scripts]
	[@javascript scripts = [
		'gato-template/js/main-menus.js',
		'gato-template/js/post-load.js',
		'gato-faq-hierarchy/js/frontend.js',
		'gato-event-calendar/js/frontend.js'
	]+scripts /]
[/#macro]

[#macro templatecss files]
	[@css files = [
		'/gato-template/css/common.css'
	]+files /]
[/#macro]

[#macro templatehead publisher="Texas State University"]
	[@meta publisher /]
	[@customCSS page ancestorstopdown /]
	[@customJS page ancestorstopdown /]
	[@title publisher /]
	[@cms.page /]
[/#macro]

[#macro title rootorg]
	<title>
		${page.title}
		[#if !isHomePage]: ${ancestorstopdown?first.title}[/#if]
		[#if rootorg == "Texas State University"]: ${rootorg}[/#if]
	</title>
[/#macro]

[#macro meta publisher="Texas State University"]
	<meta http-equiv="Content-Type" content="text/html;charset=utf-8">
	<meta http-equiv="X-UA-Compatible" content="IE=edge" />
	[#if page.metaNoIndex?has_content]
		<meta name="ROBOTS" content="NOINDEX" />
	[/#if]
	[#if page.metaDescription?has_content]
		<meta name="description" content="${page.metaDescription}" />
		<meta name="DC.description" content="${page.metaDescription}" />
	[/#if]
	[#if page.metaKeywords?has_content]
		<meta name="keywords" content="${page.metaKeywords}" />
		<meta name="DC.subject" content="${page.metaKeywords}" />
	[/#if]
	<meta name="DC.publisher" content="${publisher}" />
	<meta name="DC.creator" content="${page}" />
	<meta name="DC.date" content="${cmsfn.metaData(page, 'mgnl:lastModified')}" />
	[#local lastmod = cmsfn.metaData(page, 'mgnl:lastModified')?datetime("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")]
	[#--TODO[#if contentIsStale]--]
		<meta name="contentIsStale" content="true" />
	[#--[/#if]--]
	[#--TODO <meta name="staleTimer" content="${inheritedcurrency}"/> --]
[/#macro]

[#macro sidebarmodal skipsocial=false]
[/#macro]

[#macro googleanalytics]
[/#macro]

[#assign page = cmsfn.page(content)]
[#assign ancestorstopdown = cmsfn.ancestors(page)]
[#assign ancestorsbottomup = cmsfn.ancestors(page)?reverse]
[#assign isHomePage = !ancestorstopdown?has_content]
