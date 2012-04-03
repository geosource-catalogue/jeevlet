<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xsl:output indent="yes"/>
  <!-- Parameters -->
  <xsl:param name="user"/>
  <xsl:param name="password"/>
  <xsl:param name="idNode"/>
  <xsl:param name="gnnodeprefix"/>
  <xsl:param name="dbDriver"/>
  <xsl:param name="dbUrl"/>
  <xsl:param name="poolSize"/>

  <!-- ================================================================= -->

  <xsl:template match="/">
    <xsl:apply-templates select="/geonet"/>
  </xsl:template>

  <!-- ================================================================= -->

  <xsl:template match="/geonet">
    <xsl:copy>
      <xsl:copy-of select="general"/>
      <default>
        <xsl:copy-of select="default/service"/>
        <xsl:copy-of select="default/startupErrorService"/>
        <xsl:copy-of select="default/language"/>
        <xsl:copy-of select="default/localized"/>
        <xsl:copy-of select="default/contentType"/>
        <gui>
          <xsl:copy-of select="default/gui/*[@name!='config']"/>
          <xml name="config" localized="false">
            <xsl:attribute name="file">
              <xsl:value-of select="concat('WEB-INF-',$idNode,'/config-gui.xml')"/>
            </xsl:attribute>
          </xml>
        </gui>
        <xsl:copy-of select="default/error"/>
      </default>
      <resources>
        <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
        <!-- db connection -->
        <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
        <resource enabled="true">
          <name>main-db</name>
          <provider>jeeves.resources.dbms.ApacheDBCPool</provider>
          <config>
            <user>
              <xsl:value-of select="$user"/>
            </user>
            <password>
              <xsl:value-of select="$password"/>
            </password>
            <driver>
              <xsl:value-of select="$dbDriver"/>
            </driver>
            <url>
              <xsl:value-of select="$dbUrl"/>
            </url>
            <poolSize>
              <xsl:value-of select="$poolSize"/>
            </poolSize>
            <maxWait>3600</maxWait>
          </config>
        </resource>
      </resources>
      <appHandler class="org.fao.geonet.Geonetwork">
        <xsl:copy-of
          select="appHandler/param[@name!='luceneDir' 
							and @name!='guiConfig'
							and @name!='luceneConfig'
							and @name!='metadataNotifierConfig'
							and @name!='summaryConfig']"/>
		<param name="{$gnnodeprefix}-{$idNode}.dir">
          <xsl:attribute name="value">
            <xsl:value-of select="concat('../../data/',$idNode)"/>
          </xsl:attribute>
        </param>
        <param name="luceneConfig">
          <xsl:attribute name="value">
            <xsl:value-of select="concat('WEB-INF-',$idNode,'/config-lucene.xml')"/>
          </xsl:attribute>
        </param>
        <param name="guiConfig">
          <xsl:attribute name="value">
            <xsl:value-of select="concat('WEB-INF-',$idNode,'/config-gui.xml')"/>
          </xsl:attribute>
        </param>
        <param name="metadataNotifierConfig">
          <xsl:attribute name="value">
            <xsl:value-of select="concat('WEB-INF-',$idNode,'/config-notifier.xml')"/>
          </xsl:attribute>
        </param>
        <param name="summaryConfig">
          <xsl:attribute name="value">
            <xsl:value-of select="concat('WEB-INF-',$idNode,'/config-summary.xml')"/>
          </xsl:attribute>
        </param>
      </appHandler>
      <xsl:copy-of select="schedules"/>

      <xsl:copy-of select="services"/>

      <xsl:copy-of select="include"/>
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>
