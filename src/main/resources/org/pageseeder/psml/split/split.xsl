<!--
  XSLT to split a pre-split PSML into multiple files.

  @author Philip Rutherford
-->
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:fn="http://pageseeder.org/psml/function"
                exclude-result-prefixes="#all">
  
  <!-- Output folder URL -->
  <xsl:param name="_outputfolder" as="xs:string"/>

  <!-- Output filename -->
  <xsl:param name="_outputfilename" as="xs:string"/>
  
  <!-- Process main document -->
  <xsl:template match="/">
    <document>
      <xsl:copy-of select="document/@*" />
      <xsl:apply-templates select="document/node()" />
    </document>
  </xsl:template>
  
  <!-- Handle component documents -->
  <xsl:template match="document">
    <xsl:variable name="path" select="concat(@folder, fn:generate-filename(.))" />
    <xsl:variable name="level" as="xs:integer">
      <xsl:choose>
        <xsl:when test="section/fragment[@id='1']//heading">
          <xsl:variable name="toplevel" select="(section/fragment[@id='1']//heading)[1]/@level" as="xs:integer" />
          <!-- If any  heading levels less than the top then don't adjust -->
          <xsl:value-of select="if (.//heading[xs:integer(@level) &lt; $toplevel]) then 0 else $toplevel - 1" />
        </xsl:when>
        <xsl:otherwise>0</xsl:otherwise>
      </xsl:choose>     
    </xsl:variable>
    <blockxref frag="default" display="document" type="embed" href="{$path}">
      <xsl:if test="$level > 0">
        <xsl:attribute name="level" select="$level" />
      </xsl:if>
      <xsl:result-document href="{concat($_outputfolder,$path)}">
        <xsl:copy>
          <xsl:copy-of select="@*[not(name()='folder')]" />
          <xsl:apply-templates>
            <xsl:with-param name="level" select="$level" tunnel="yes" as="xs:integer" />
          </xsl:apply-templates>
        </xsl:copy>
      </xsl:result-document>
    </blockxref>
  </xsl:template>    

  <!-- Handle inline documents -->
  <xsl:template match="inline[document]">
    <xsl:variable name="path" select="concat(document/@folder, fn:generate-filename(document))" />
    <xref frag="default" display="document" type="none"
        href="{concat(if (ancestor::document[@folder]) then '../' else '', $path)}">
      <xsl:value-of select="document/documentinfo/uri/@title" />
    </xref>    
    <xsl:for-each select="document">
      <xsl:result-document href="{concat($_outputfolder,$path)}">
        <xsl:copy>
          <xsl:copy-of select="@*[not(name()='folder')]" />
          <xsl:apply-templates>
            <xsl:with-param name="level" select="0" tunnel="yes" as="xs:integer" />
          </xsl:apply-templates>
        </xsl:copy>
      </xsl:result-document>
    </xsl:for-each>
  </xsl:template>    

  <!-- modify heading level -->
  <xsl:template match="heading">
    <xsl:param name="level" select="0" tunnel="yes" as="xs:integer"/>
    <xsl:copy>
      <xsl:copy-of select="@*[not(name()='level')]" />
      <xsl:attribute name="level" select="if ($level > 0) then number(@level) - 1 else @level" />
      <xsl:apply-templates select="node()" />
    </xsl:copy>
  </xsl:template>

  <!-- modify image src -->
  <xsl:template match="image">
    <xsl:copy>
      <xsl:copy-of select="@*[not(name()='src')]" />
      <xsl:attribute name="src" select="concat(if (ancestor::document[@folder]) then '../' else '', @src)" />
      <xsl:apply-templates select="node()" />
    </xsl:copy>
  </xsl:template>
  
  <!-- replace internal links with xrefs -->
  <xsl:template match="link[starts-with(@href,'#')]">
    <xsl:variable name="anchor" select="//anchor[@name = substring-after(current()/@href,'#')]" />
    <xsl:choose>
      <xsl:when test="$anchor">
        <xsl:variable name="document" select="($anchor/ancestor::document)[last()]" />
        <xsl:variable name="path">
          <xsl:choose>
            <xsl:when test="count($anchor/ancestor::document) > 1">
              <xsl:value-of
                select="concat($document/@folder, fn:generate-filename($document))" />
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="$_outputfilename" />
            </xsl:otherwise>
          </xsl:choose>
        </xsl:variable>
        <xref frag="{($anchor/ancestor::fragment)[last()]/@id}" display="manual" type="none"
            href="{concat(if (ancestor::document[@folder]) then '../' else '', $path)}" title="{normalize-space(.)}">
          <xsl:value-of select="." />
        </xref>
      </xsl:when>
      <xsl:otherwise>
        <xsl:copy>
          <xsl:copy-of select="@*" />
          <xsl:apply-templates select="node()" />
        </xsl:copy>       
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <!-- remove anchors -->
  <xsl:template match="anchor">
    <xsl:apply-templates select="node()" />
  </xsl:template>
  
  <!-- copy all other elements unchanged -->
  <xsl:template match="*">
    <xsl:copy>
      <xsl:copy-of select="@*" />
      <xsl:apply-templates select="node()" />
    </xsl:copy>
  </xsl:template>
  
  <!-- return generated filename for document ([@type]-NNN.psml) -->
  <xsl:function name="fn:generate-filename" as="xs:string">
    <xsl:param name="doc" as="element(document)" />    
    <xsl:choose>
      <xsl:when test="$doc/@type">
        <xsl:value-of select="concat($doc/@type, '-',
            format-number(count($doc/preceding::document[@type=$doc/@type]) + 1, '000'), '.psml')"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="concat('component-',
            format-number(count($doc/preceding::document[not(@type)]) + 1, '000'),'.psml')"/>
      </xsl:otherwise>
    </xsl:choose>   
  </xsl:function>

</xsl:stylesheet>
