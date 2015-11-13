[#include "/gato-lib/templates/includes/accordion.ftl"]

[#if content.title?has_content]
  <h2>${content.title}</h2>
[/#if]

[#if model.error?has_content]

  <div class="txst-events-error">
    There was an error retrieving the event feed.
  </div>

[#elseif model.items?has_content]

  [#list model.items as item]
    [#assign eventClass=item.cancelled?string('txst-eventdetail-cancelled','vevent')]
  
    <div class="txst-eventdetail gato-accordion ${eventClass}" 
        data-start-collapsed="${model.collapsed?string('true', 'false')}">

      <div class="gato-accordion-header">
        <h3 class="txst-eventdetail-title">
          ${item.cancelled?string('CANCELLED - ','')}
          <span class="summary">
            ${item.title}
          </span>
        </h3>
        <div class="txst-eventdetail-dates">
          <abbr class="dtstart" title="${item.machineStartDate}">
            ${item.humanStartDate}
          </abbr>
          [#if item.showEndDate]
            &ndash;
            <abbr class="dtend" title="${item.machineEndDate}">
              ${item.humanEndDate}
            </abbr>
          [/#if]
        </div>
      </div>

      <div class="gato-accordion-content eq-parent">

        <div class="eq-mn-1-1 eq-ml-3-12 eq-xl-2-12">
          [#if item.image?has_content]
            <div class="txst-eventdetail-thumbnail">
              <img alt="${item.title}" src="${item.image}" />
            </div>
          [/#if]

          <a title="add ${item.title} to calendar"
            href="${item.calendarUrl}"
            class="txst-eventdetail-addtocalendar">
            <i class="fa fa-calendar" aria-hidden="true"></i>
            <span class="linktext">add to calendar</span>
          </a>
        </div>
        
        <div class="eq-sm-1-1 eq-ml-9-12 eq-xl-10-12">
          <dl class="txst-eventdetail-detailsbox">
            [#if item.facility?has_content]
              <dt>Location:</dt>
              <dd>${item.facility}</dd>
            [/#if]
            
            <dt>Cost:</dt>
            <dd>${item.cost}</dd>

            <dt>Contact:</dt>
            <dd>${item.contact}</dd>

            <dt>Campus Sponsor:</dt>
            <dd>${item.sponsor}</dd>
          </dl>
        </div>
      
        [#if item.description?has_content]
          <div class="eq-lg-1-1">
            <div class="txst-eventdetail-description">
              ${item.description}
              [#if item.link?has_content]
                <a href="${item.link}" class="url">
                  Click here for more information
                </a>
              [/#if]
            </div>
          </div>
        [/#if]
           
        <a class="txst-eventdetail-morelink eq-sm-1-1" href="${item.url}">
          <span class="linktext">more about event</span>
          <i class="fa fa-angle-right" aria-hidden="true"></i>
        </a>   
      </div>
    </div>
  [/#list]

[#else]

  <div class="txst-events-empty">
    No results.
  </div>

[/#if]
