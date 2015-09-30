[#if content.link?contains("facebook")]
  [#assign iconclass="fa-facebook-square"]
  [#assign alttext="Facebook"]
[#elseif content.link?contains("twitter")]
  [#assign iconclass="fa-twitter-square"]
  [#assign alttext="Twitter"]
[#elseif content.link?matches(r"youtu\.?be")]
  [#assign iconclass="fa-youtube-square"]
  [#assign alttext="YouTube"]
[#elseif content.link?contains("instagram")]
  [#assign iconclass="fa-instagram"]
  [#assign alttext="Instagram"]
[/#if]
[#assign alttext = content.title!alttext!'Social Media Presence']
<a href="${content.link}" class="gato-sociallink">
  [#if content.icon?has_content]
    <img src="${damfn.getAssetLink(content.icon)}" alt="${alttext}" title="${alttext}" />
  [#else]
    <i class="fa ${iconclass!''}" aria-label="${alttext}"></i>
  [/#if]
  [#if !(content.icononly!false) && content.title?has_content]
    ${content.title}
  [/#if]
</a>
