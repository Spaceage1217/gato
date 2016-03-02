/**
     * Gato CKEditor Custom Config File
     *
     * External plugins added through the server-side FieldFactory are automatically registered.
     * Other external plugins (e.g. client-only) may still be registered here (and subsequently added via config.extraPlugins).
     *
     * Based on the config file provided by Magnolia
     */
    CKEDITOR.editorConfig = function( config ) {

        //load image2 plugin and all of its dependencies
        CKEDITOR.plugins.addExternal("dialogui", CKEDITOR.vaadinDirUrl + "js/richeditorplugins/dialogui/", 'plugin.js');
        CKEDITOR.plugins.addExternal("dialog", CKEDITOR.vaadinDirUrl + "js/richeditorplugins/dialog/", 'plugin.js');
        CKEDITOR.plugins.addExternal("clipboard", CKEDITOR.vaadinDirUrl + "js/richeditorplugins/clipboard/", 'plugin.js');
        CKEDITOR.plugins.addExternal("lineutils", CKEDITOR.vaadinDirUrl + "js/richeditorplugins/lineutils/", 'plugin.js');
        CKEDITOR.plugins.addExternal("widget", CKEDITOR.vaadinDirUrl + "js/richeditorplugins/widget/", 'plugin.js');
        CKEDITOR.plugins.addExternal("image2", CKEDITOR.vaadinDirUrl + "js/richeditorplugins/image2/", 'plugin.js');
        CKEDITOR.plugins.addExternal("magnolialink", CKEDITOR.vaadinDirUrl + "js/magnolialink/", 'plugin.js');

         definition = {
                   alignment: true,
                   images: true,
                   lists: true,
                   source: true,
                   tables: true,

                   colors: "222222,501214,6a5638,363534,b30e1b",
                   fonts: null,
                   fontSizes: null
           }

           // MIRROR info.magnolia.ui.form.field.factory.RichTextFieldFactory
           removePlugins = [];

           // CONFIGURATION FROM DEFINITION

          if (definition.colors != null) {
                   config.colorButton_colors = definition.colors;
                   config.colorButton_enableMore = true;
           } else {
                   removePlugins.push("colorbutton");
                   removePlugins.push("colordialog");
           }
           if (definition.fonts != null) {
                   config.font_names = definition.fonts;
           } else {
                   config.removeButtons = "Font";
           }
           if (definition.fontSizes != null) {
                   config.fontSize_sizes = definition.fontSizes;
           } else {
                   config.removeButtons = "FontSize";
           }
           if (definition.fonts == null && definition.fontSizes == null) {
               removePlugins.push("font");
                   removePlugins.push("fontSize");
           }

           //get templates
           config.templates = 'gato_templates';
           config.templates_files = [CKEDITOR.vaadinDirUrl + 'js/gato_templates.js'];

           //set allowed styles.  by default, there are many more styles to choose from
           config.stylesSet = [];
                config.stylesSet.push({ name: "Paragraph", element: "p"});
                config.stylesSet.push({ name: "Title", element: "h2"});
                config.stylesSet.push({ name: "Subtitle", element: "h3"});
                config.stylesSet.push({ name: "Subsubtitle", element: "h4"});
                config.stylesSet.push({ name: "Preformatted Text", element: "pre"});
                config.stylesSet.push({ name: "Superscript", element: "sup"});
                config.stylesSet.push({ name: "Subscript", element: "sub"});
                config.stylesSet.push({ name: "Strike Out", element: "s"});
                config.stylesSet.push({ name: 'Computer Code',  element: 'code' });

           // DEFAULT CONFIGURATION FROM FIELD FACTORY
           removePlugins.push("elementspath");
           removePlugins.push("filebrowser");
           config.removePlugins = removePlugins.join(",");
           config.extraPlugins = "magnolialink,magnoliaFileBrowser,dialogui,dialog,clipboard,lineutils,widget,image2";

           config.width = 660;
           config.indentOffset = 4;
           config.indentUnit = 'rem';
           config.bodyClass = "txst-styledcontent txst-contentarea-paragraph";
           config.baseFloatZIndex = 150;
           config.resize_enabled = false;
           config.toolbar = "Magnolia";
           config.disableNativeSpellChecker = false;
           config.toolbar_Magnolia = [
                   { name: "basicstyles",   items: [ "Bold", "Italic", "HorizontalRule", "Blockquote", "RemoveFormat", "SpecialChar" ] },
                   { name: "links",         items: [ "Link", "InternalLink", "DamLink", "Unlink", "Anchor" ] },
                   { name: "clipboard",     items: [ "Copy", "Paste", "PasteText", "PasteFromWord" ] },
                   { name: "tables",        items: [ "Table" ] },
                   { name: "images",        items: ["Image", "Flash"]},
                   { name: "undo",          items: [ "Undo", "Redo" ] },
                   { name: "tools",         items: [ "Source" ] },
                   { name: "paragraph",     items: [ "JustifyLeft", "JustifyCenter", "JustifyRight", "JustifyBlock", "NumberedList", "BulletedList","Indent", "Outdent"] },
                   //{ name: "templates",     items: [ "Templates" ]},
                   { name: "styles",        items: [ "Styles"]},
                   { name: "colors",        items: [ "TextColor" ] }
           ];
           config.removeDialogTabs = 'image:advanced;link:advanced';


           //maybe not necessary
           CKEDITOR.on('instanceReady', function( ev ){
              var tablePlugin = ev.editor.getCommand('table');
              tablePlugin['allowedContent'] += ';table[class]{*}(*)';

           });

           //customize table dialog
           CKEDITOR.on('dialogDefinition', function( ev ) {

              var dialogName = ev.data.name;
              var dialogDefinition = ev.data.definition;

              if(dialogName === 'table' || dialogName === 'tableProperties') {
                //Remove height, width, cellpadding, cellspacing, alignment, caption, summary, and language direction
                var infoTab = dialogDefinition.getContents('info');
                infoTab.remove('txtWidth');
                infoTab.remove('txtHeight');
                infoTab.remove('txtCellPad');
                infoTab.remove('txtCellSpace');
                infoTab.remove('cmbAlign');
                infoTab.remove('txtCaption');
                infoTab.remove('txtSummary');
                var advTab = dialogDefinition.getContents('advanced');
                advTab.remove('advLangDir');

                //change border size to border on/off
                var borderField = infoTab.get('txtBorder');
                borderField['type'] = 'radio';
                borderField['items'] = [ [ 'On', 1 ], [ 'Off', 0 ] ];
                borderField['label'] = 'Border';

                function addTableCSSClass(val){
                  var cssClassesField = CKEDITOR.dialog.getCurrent().getContentElement( 'advanced', 'advCSSClasses' );
                  var currentClasses = cssClassesField.getValue();
                  if(currentClasses.indexOf(val) < 0){
                    cssClassesField.setValue(currentClasses + " " + val);
                  }
                }

                function removeTableCSSClass(val){
                  var cssClassesField = CKEDITOR.dialog.getCurrent().getContentElement( 'advanced', 'advCSSClasses' );
                  var currentClasses = cssClassesField.getValue();
                  cssClassesField.setValue(currentClasses.replace(val, ""));
                }

                function hasTableCSSClass(selectedTable,val){
                  var currentClasses = selectedTable.getAttribute("class");
                  return currentClasses.indexOf(val) > -1;
                }


                var selectHeaderColor = {
                  type: 'select',
                  id: 'selHeaderColor',
                  label: 'Header Color',
                  items: [ ['None',''], 
                         [ 'Gold', 'header-color-gold' ], 
                         [ 'Maroon', 'header-color-maroon' ], 
                         [ 'Charcoal','header-color-charcoal' ], 
                         [ 'Sandstone', 'header-color-sandstone' ] ],
                  'default': '',
                  onChange: function(api){
                    for(var i=0; i<this.items.length; i++){
                      var colorClass = this.items[i][1];
                      if(colorClass.length > 0)
                        removeTableCSSClass(colorClass);
                    }
                    addTableCSSClass(this.getValue());
                  },
                  setup: function (selectedTable){
                    //the default is None.  If the table has one of the 
                    //other color classes, set it.
                    for(var i=0; i< this.items.length; i++){
                      var colorClass = this.items[i][1];
                      if(colorClass.length > 0 && hasTableCSSClass(selectedTable, colorClass)){
                        this.setValue(colorClass);
                      }
                    }
                  }
                };
                if(!infoTab.get('selHeaderColor')){
                  infoTab.add(selectHeaderColor);
                }

                //Add alternate row color option
                //If the checkbox is selected, alternate-row-color is added to the 
                //css classes in the advanced tab
                var alternateRow = {
                  type: 'checkbox',
                  id: 'altRowColor',
                  label: 'Alternating Background Color',
                  'default': false,
                  onChange: function(api){
                    if(this.getValue())
                      addTableCSSClass("alternate-row-color");
                    else
                      removeTableCSSClass("alternate-row-color");
                  },
                  setup: function (selectedTable){
                    this.setValue(hasTableCSSClass(selectedTable, "alternate-row-color"));
                  }
                };
                if(!infoTab.get('altRowColor')){
                  infoTab.add(alternateRow);
                }
              }

              //Remove width, height, word-wrap, horizontal alignment, vertical alignment, border-color
              if(dialogName === 'cellProperties') {
                var infoTab = dialogDefinition.getContents('info');
                infoTab.remove('width');
                infoTab.remove('widthType');
                infoTab.remove('height');
                infoTab.remove('htmlHeightType');
                infoTab.remove('wordWrap');
                infoTab.remove('hAlign');
                infoTab.remove('vAlign');
                infoTab.remove('borderColor');
                infoTab.remove('borderColorChoose');
              }

           });

           // Allow all elements/styles/attrs/classes except for line-height style. CKeditor 4.4 has 
           // disallowedContent list which would be ideal for this, but since we're stuck with 4.3 for now
           // we'll do it the long way.
           //
           // List of element names are from CKEDITOR.dtd and list of CSS properties are from https://www.w3.org/Style/CSS/all-properties.en.json
           config.allowedContent = 'a abbr address area article aside audio b base bdi bdo blockquote body br button canvas caption cite code col colgroup command datalist dd del details dfn div dl dt em embed fieldset figcaption figure footer form h1 h2 h3 h4 h5 h6 head header hgroup hr html i iframe img input ins kbd keygen label legend li link map mark menu meta meter nav noscript object ol optgroup option output p param pre progress q rp rt ruby s samp script section select small source span strong style sub summary sup table tbody td textarea tfoot th thead time title tr track u ul var video wbr acronym applet basefont big center dialog dir font isindex noframes strike tt;' + // Allowed elements
                                   '*[*];' + // Allowed attributes
                                   '*(*);' + // Allowed class names
                                   // Allowed styles, currently allowing everything except for line-height
                                   '*{align-content, align-items, align-self, alignment, alignment-baseline, all, alt, animation, animation-delay, animation-direction, animation-duration, animation-fill-mode, animation-iteration-count, animation-name, animation-play-state, animation-timing-function, appearance, azimuth, backface-visibility, background, background-attachment, background-blend-mode, background-clip, background-color, background-image, background-origin, background-position, background-position-block, background-position-inline, background-position-x, background-position-y, background-repeat, background-size, baseline-shift, bookmark-label, bookmark-level, bookmark-state, border, border-bottom, border-bottom-color, border-bottom-left-radius, border-bottom-right-radius, border-bottom-style, border-bottom-width, border-boundary, border-clip, border-clip-bottom, border-clip-left, border-clip-right, border-clip-top, border-collapse, border-color, border-image, border-image-outset, border-image-repeat, border-image-slice, border-image-source, border-image-width, border-left, border-left-color, border-left-style, border-left-width, border-limit, border-radius, border-right, border-right-color, border-right-style, border-right-width, border-spacing, border-style, border-top, border-top-color, border-top-left-radius, border-top-right-radius, border-top-style, border-top-width, border-width, bottom, box-decoration-break, box-shadow, box-sizing, box-snap, box-suppress, break-after, break-before, break-inside, caption-side, caret, caret-animation, caret-color, caret-shape, chains, child-align, clear, clear-after, clip, clip-path, clip-rule, color, color-interpolation-filters, column-count, column-fill, column-gap, column-rule, column-rule-color, column-rule-style, column-rule-width, column-span, column-width, columns, content, continue, corner-shape, corners, counter-increment, counter-reset, counter-set, crop, cue, cue-after, cue-before, cursor, direction, display, dominant-baseline, elevation, empty-cells, filter, flex, flex-basis, flex-direction, flex-flow, flex-grow, flex-shrink, flex-wrap, float, float-defer, float-displace, float-offset, float-reference, flood-color, flood-opacity, flow, flow-from, flow-into, font, font-family, font-feature-settings, font-kerning, font-language-override, font-size, font-size-adjust, font-stretch, font-style, font-synthesis, font-variant, font-variant-alternates, font-variant-caps, font-variant-east-asian, font-variant-ligatures, font-variant-numeric, font-variant-position, font-weight, footnote-display, footnote-policy, glyph-orientation-vertical, grid, grid-area, grid-auto-columns, grid-auto-flow, grid-auto-rows, grid-column, grid-column-end, grid-column-gap, grid-column-start, grid-gap, grid-row, grid-row-end, grid-row-gap, grid-row-start, grid-template, grid-template-areas, grid-template-columns, grid-template-rows, hanging-punctuation, height, hyphenate-character, hyphenate-limit-chars, hyphenate-limit-last, hyphenate-limit-lines, hyphenate-limit-zone, hyphens, image-orientation, image-rendering, image-resolution, indent-edge-reset, initial-letter, initial-letter-align, initial-letter-wrap, isolation, justify-content, justify-items, justify-self, left, letter-spacing, lighting-color, line-break, line-grid, line-snap, list-style, list-style-image, list-style-position, list-style-type, margin, margin-bottom, margin-left, margin-right, margin-top, marker-side, marquee-direction, marquee-loop, marquee-speed, marquee-style, mask, mask-border, mask-border-mode, mask-border-outset, mask-border-repeat, mask-border-slice, mask-border-source, mask-border-width, mask-clip, mask-composite, mask-image, mask-mode, mask-origin, mask-position, mask-repeat, mask-size, mask-type, max-height, max-lines, max-width, max-zoom, min-height, min-width, min-zoom, mix-blend-mode, motion, motion-offset, motion-path, motion-rotation, move-to, nav-down, nav-left, nav-right, nav-up, object-fit, object-position, offset-after, offset-before, offset-end, offset-start, opacity, order, orientation, orphans, outline, outline-color, outline-offset, outline-style, outline-width, overflow, overflow-style, overflow-wrap, overflow-x, overflow-y, padding, padding-bottom, padding-left, padding-right, padding-top, page, page-break-after, page-break-before, page-break-inside, page-policy, pause, pause-after, pause-before, perspective, perspective-origin, pitch, pitch-range, play-during, polar-anchor, polar-angle, polar-distance, polar-origin, position, presentation-level, quotes, region-fragment, resize, resolution, rest, rest-after, rest-before, richness, right, rotation, rotation-point, ruby-align, ruby-merge, ruby-position, running, scroll-behavior, scroll-snap-align, scroll-snap-coordinate, scroll-snap-destination, scroll-snap-margin, scroll-snap-padding, scroll-snap-points-x, scroll-snap-points-y, scroll-snap-type, shape-image-threshold, shape-inside, shape-margin, shape-outside, size, speak, speak-as, speak-header, speak-numeral, speak-punctuation, speech-rate, stress, string-set, tab, tab-align, tab-leaders, tab-leaders-alignment, tab-position, tab-size, table-baseline, table-column-span, table-layout, table-row-span, text-align, text-align-all, text-align-last, text-combine-upright, text-decoration, text-decoration-color, text-decoration-line, text-decoration-skip, text-decoration-style, text-emphasis, text-emphasis-color, text-emphasis-position, text-emphasis-style, text-indent, text-justify, text-orientation, text-overflow, text-shadow, text-space-collapse, text-space-trim, text-spacing, text-transform, text-underline-position, text-wrap, top, transform, transform-box, transform-origin, transform-style, transition, transition-delay, transition-duration, transition-property, transition-timing-function, unicode-bidi, user-select, user-zoom, vertical-align, visibility, voice-balance, voice-duration, voice-family, voice-pitch, voice-range, voice-rate, voice-stress, voice-volume, volume, white-space, widows, width, will-change, word-break, word-spacing, word-wrap, wrap-after, wrap-before, wrap-flow, wrap-inside, wrap-through, writing-mode, z-index, zoom}';
   };
