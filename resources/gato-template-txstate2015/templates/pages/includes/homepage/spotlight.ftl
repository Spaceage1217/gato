[#assign slides = cmsfn.asContentMap(cmsfn.nodeByPath('/homepage-data/features/research-feature', 'gatoapps'))]
[#assign slides = cmsfn.children(slides, "mgnl:component")]
[#assign aspectratio = 16.0/9.0]

<div id="spotlight" class="content-row comp two-col">
    <div class="content-row-content">
        
      <div class="content-row-column">
        
        <div class="col-left research">
          <h2>Research</h2>
        
          <div class="research-slider-wrap feature">
            <div class="slide-nav"></div>
            <div class="slides">

              [#list slides as component]
                [#if isEnabled(component)]

                  <div class="slide" >
                    <figure class="feature research-slider">
                      
                      <div class="feature-img-wrap">
                      
                        <img src="${gf.getImgDefault(component.image, aspectratio)}" srcset="${gf.getSrcSet(component.image, aspectratio)}" alt="${component.alttext!}">
                      
                        [#if component.videourl?has_content]
                          <p class="feature-play-button">
                            <a href="${component.videourl}" aria-label="Play Video" 
                            data-embed="${gf.jsonGetString(gf.oEmbedAutodiscover(component.videourl), 'html')?html}"></a>
                          </p>
                        [/#if]
                      
                      </div>
                      
                      <figcaption>

                          [#if component.title?has_content]
                            <p class="feature-headline">

                              [#assign link = component.link!component.videourl!]
                              [#if link?has_content]
                                <a href="${gf.filterUrl(link)}">
                              [/#if]
                                  ${component.title}
                              [#if link?has_content]
                                </a>
                              [/#if]
                              
                            </p>
                          [/#if]

                          <p class="feature-text">${component.subtext!}</p>

                      </figcaption>
                    </figure>
                  </div>
                
                [/#if]
              [/#list]
            
            </div>
          
          </div>
        </div>
      </div>
    
      <div class="content-row-column">
        <div class="col-right pride">
          <h2>Pride</h2>
          
          <div class="bab">
            <figure>
              <img alt="demo" src="${ctx.contextPath}/.resources/gato-template-txstate2015/images/bab-1.jpg">
              <figcaption>
                <a href="//txstate.edu/beabobcat">Be a Bobcat</a>
              </figcaption>
            </figure>
          </div> 
          
          <div class="rs">
            <figure>
              <img alt="demo" src="${ctx.contextPath}/.resources/gato-template-txstate2015/images/rs-1.jpg">
              <figcaption>
                <a href="//txstate.edu/rising-stars">Rising Stars</a>
              </figcaption>
            </figure>
          </div>
              
        </div>
      </div>

  </div>
</div>
