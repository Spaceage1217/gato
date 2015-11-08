[#assign globalData = cmsfn.asContentMap(cmsfn.nodeByPath('/global-data', 'website'))]
<div class="super-user-menu">
    <div class="super-user-content constrain eq-parent">
        <div class="eq-mn-1-2">
            <ul class="super-list super-list-left">
                [#list cmsfn.children(globalData.superGroup1, "mgnl:component") as component]
                    <li>[@cms.component content=component/]</li>
                [/#list]
            </ul>
        </div><div class="eq-mn-1-2">
            <ul class="super-list super-list-right">
                [#list cmsfn.children(globalData.superGroup2, "mgnl:component") as component]
                    <li>[@cms.component content=component/]</li>
                [/#list]
                <li class="more-tools">
                    <a href="#">
                    More Tools
                    <i class="fa fa-caret-down"></i>
                    </a>
                    <ul class="super-list-sub">
                        [#list cmsfn.children(globalData.superGroup3, "mgnl:component") as component]
                            <li>[@cms.component content=component/]</li>
                        [/#list]
                    </ul>
                </li>
            </ul>
        </div>
    </div>
</div>