<link rel="import" href="${resource(dir: '/bower_components/core-toolbar', file: 'core-toolbar.html')}">
<link rel="import" href="${resource(dir: '/bower_components/core-drawer-panel', file: 'core-drawer-panel.html')}">
<link rel="import" href="${resource(dir: '/bower_components/core-header-panel', file: 'core-header-panel.html')}">
<vs:webresource dir="core-icon-button" file="core-icon-button.html"/>

<polymer-element name="vs-navbar" attributes="responsiveWidth mode sessionData userVS">
<template>
    <g:include view="/include/styles.gsp"/>
  <style>
    [drawer] { background-color: #ba0011; color: #f9f9f9; box-shadow: 1px 0 1px rgba(0, 0, 0, 0.1); }
    [main] { height: 100%; background-color: #fefefe;}
    core-toolbar { background-color: #ba0011; color: #fff; }
    core-header-panel #mainContainer { height: 1000px; }
    #drawerPanel:not([narrow]) #menuButton { display: none; }
    icon-button.white  { fill: #f9f9f9; }
    #drawer { width: 300px; }
  </style>
  <core-drawer-panel id="coreDrawerPanel" narrow="{{narrow}}" responsiveWidth="{{responsiveWidth}}">
    <div id="drawerItems" vertical layout drawer style="">
        <content select="[navigation], nav"></content>
    </div>
    <core-header-panel id="mainHeaderPanel" main mode="{{mode}}">
      <core-toolbar id="coreToolbar">
        <core-icon-button id="menuButton" icon="menu" on-tap="{{togglePanel}}"></core-icon-button>
        <content select="[tool]"></content>
        <core-icon-button id="searchButton" icon="search" on-tap="{{toogleSearchPanel}}" style="fill: #f9f9f9;"></core-icon-button>
      </core-toolbar>
      <content select="*"></content>
    </core-header-panel>
  </core-drawer-panel>
    <div id="searchPanel" class="" style="position: absolute;top:70px; left: 40%; background:#ba0011;
            padding:10px 10px 10px 10px;display:none; z-index: 10;">
        <input id="searchInput" type="text" placeholder="Search"
               style="width:160px; border-color: #f9f9f9;display:inline; vertical-align: middle;">
        <i id="searchPanelCloseIcon" on-click="{{toogleSearchPanel}}" class="fa fa-times text-right vs-navbar-icon"
           style="margin:0px 0px 0px 15px; display:inline;vertical-align: middle;"></i>
    </div>
</template>
<script>
  Polymer('vs-navbar', {
    sessionData:null,
    responsiveWidth: '100000px',// 100000px -> Always closed
    mode: 'seamed', //Used to control the header and scrolling behaviour of `core-header-panel`
    ready: function() { },
    togglePanel: function() {
        this.$.coreDrawerPanel.togglePanel();
    },
    openDrawer: function() {
      this.$.coreDrawerPanel.openDrawer();
    },
    searchVisible: function(isVisible) {
        if(isVisible) this.$.searchButton.style.display = 'block'
        else this.$.searchButton.style.display = 'none'
    },
    closeDrawer: function() {
        this.$.coreDrawerPanel.closeDrawer();
    },
    toogleSearchPanel:function () {
        if('block' == this.$.searchPanel.style.display) this.$.searchPanel.style.display = 'none'
        else this.$.searchPanel.style.display = 'block'
    }
  });
</script>
</polymer-element>
