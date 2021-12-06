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

  <!-- Media folder name -->
  <xsl:param name="_mediafoldername" as="xs:string"/>

  <!-- Process main document -->
  <xsl:template match="/">
    <document>
      <xsl:copy-of select="document/@*" />
      <xsl:apply-templates select="document/node()">
        <!-- Use -1 because top level heading is usually not included in the TOC -->
        <xsl:with-param name="level" select="-1" tunnel="yes" as="xs:integer" />
      </xsl:apply-templates>
    </document>
  </xsl:template>

  <!-- Handle component documents -->
  <xsl:template match="document">
    <xsl:param name="level" select="0" tunnel="yes" as="xs:integer"/>
    <xsl:variable name="path" select="concat(@folder, fn:generate-filename(.))" />
    <xsl:variable name="adjust" as="xs:integer">
      <xsl:choose>
        <xsl:when test="section/fragment[@id='1']//heading">
          <xsl:variable name="toplevel" select="(section/fragment[@id='1']//heading)[1]/@level" as="xs:integer" />
          <!-- If any  heading levels less than the top then don't adjust -->
          <xsl:value-of select="if (.//heading[xs:integer(@level) &lt; $toplevel]) then 0 else $toplevel - 1" />
        </xsl:when>
        <xsl:otherwise>0</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <blockxref frag="default" display="document" type="embed"
        href="{concat(fn:path-prefix((ancestor::document)[last()]), $path)}">
      <xsl:if test="@type">
        <xsl:attribute name="documenttype" select="@type" />
      </xsl:if>
      <xsl:if test="$adjust - $level - 1 > 0">
        <xsl:attribute name="level" select="$adjust - $level - 1" />
      </xsl:if>
      <xsl:value-of select="documentinfo/uri/@title" />
      <xsl:result-document href="{concat($_outputfolder,$path)}">
        <xsl:copy>
          <xsl:copy-of select="@*[not(name()='folder')]" />
          <xsl:apply-templates>
            <xsl:with-param name="level" select="$adjust" tunnel="yes" as="xs:integer" />
          </xsl:apply-templates>
        </xsl:copy>
      </xsl:result-document>
    </blockxref>
  </xsl:template>

  <!-- Handle inline documents -->
  <xsl:template match="inline[document]">
    <xsl:variable name="path" select="concat(document/@folder,
        if (document/@filename) then document/@filename else fn:generate-filename(document))" />
    <xref frag="default" display="document" type="none"
        href="{concat(fn:path-prefix((ancestor::document)[last()]), $path)}">
      <xsl:if test="document/@type">
        <xsl:attribute name="documenttype" select="document/@type" />
      </xsl:if>
      <xsl:value-of select="document/documentinfo/uri/@title" />
    </xref>
    <xsl:for-each select="document">
      <!-- don't output duplicate files -->
      <xsl:if test="not(@filename) or not(preceding::document[@filename=current()/@filename and @folder=current()/@folder])" >
        <xsl:result-document href="{concat($_outputfolder,$path)}">
          <xsl:copy>
            <xsl:copy-of select="@*[not(name()='folder' or name()='filename')]" />
            <xsl:apply-templates>
              <xsl:with-param name="level" select="0" tunnel="yes" as="xs:integer" />
            </xsl:apply-templates>
          </xsl:copy>
        </xsl:result-document>
      </xsl:if>
    </xsl:for-each>
  </xsl:template>

  <!-- modify heading level -->
  <xsl:template match="heading">
    <xsl:param name="level" select="0" tunnel="yes" as="xs:integer"/>
    <xsl:copy>
      <xsl:copy-of select="@*[not(name()='level' or name()='prefix')]" />
      <xsl:attribute name="level" select="if ($level > 0) then number(@level) - $level else @level" />
      <xsl:if test="@prefix and not(@numbered = 'true')">
        <xsl:attribute name="prefix" select="@prefix" />
      </xsl:if>
      <xsl:apply-templates select="node()" />
    </xsl:copy>
  </xsl:template>

  <!-- remove para prefix in case it was added by process task -->
  <xsl:template match="para">
    <xsl:copy>
      <xsl:copy-of select="@*[not(name()='prefix')]" />
      <xsl:if test="@prefix and not(@numbered = 'true')">
        <xsl:attribute name="prefix" select="@prefix" />
      </xsl:if>
      <xsl:apply-templates select="node()" />
    </xsl:copy>
  </xsl:template>

  <!-- modify image src -->
  <xsl:template match="image">
    <xsl:copy>
      <xsl:copy-of select="@*[not(name()='src')]" />
      <!-- when splitting PSML processed with ImageSrc.FILENAME the media folder needs to be added -->
      <xsl:variable name="src" select="concat(if (starts-with(@src, concat($_mediafoldername,'/'))) then '' else concat($_mediafoldername,'/'), @src)" />
      <xsl:attribute name="src" select="concat(fn:path-prefix((ancestor::document)[last()]), $src)" />
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
              <xsl:value-of select="encode-for-uri($_outputfilename)" />
            </xsl:otherwise>
          </xsl:choose>
        </xsl:variable>
        <xref frag="{($anchor/ancestor::fragment)[last()]/@id}" display="manual" type="none"
            href="{concat(fn:path-prefix((ancestor::document)[last()]), $path)}" title="{normalize-space(.)}">
          <xsl:value-of select="." />
        </xref>
      </xsl:when>
      <xsl:otherwise>
        <xref frag="default" display="manual" type="none"
            href="{@href}" title="{normalize-space(.)}">
          <xsl:value-of select="." />
        </xref>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- adjust internal xref targets -->
  <xsl:template match="xref[starts-with(@href,'#')]">
    <xsl:variable name="anchor" select="//fragment-anchor[@id = substring-after(current()/@href,'#')]" />
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
              <xsl:value-of select="encode-for-uri($_outputfilename)" />
            </xsl:otherwise>
          </xsl:choose>
        </xsl:variable>
        <xref frag="{($anchor/ancestor::fragment | $anchor/ancestor::properties-fragment |
                    $anchor/ancestor::media-fragment)[last()]/@id}"
            href="{concat(fn:path-prefix((ancestor::document)[last()]), $path)}">
          <xsl:copy-of select="@*[name()!='frag' and name()!='href']" />
          <xsl:value-of select="." />
        </xref>
      </xsl:when>
      <xsl:otherwise>
        <xref frag="default" href="{@href}">
          <xsl:copy-of select="@*[name()!='frag' and name()!='href']" />
          <xsl:value-of select="." />
        </xref>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- adjust relative xref targets to other documents -->
  <xsl:template match="xref[not(starts-with(@href,'#') or starts-with(@href,'/') or @external='true')]">
    <xref href="{concat(fn:path-prefix((ancestor::document)[last()]), @href)}">
      <xsl:copy-of select="@*[name()!='href']" />
      <xsl:value-of select="." />
    </xref>
  </xsl:template>

  <!-- remove anchors -->
  <xsl:template match="anchor">
    <xsl:apply-templates select="node()" />
  </xsl:template>

  <!-- remove fragment anchors -->
  <xsl:template match="fragment-anchor">
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

  <!-- return path prefix from this document to root (e.g. '', '../', '../../') -->
  <xsl:function name="fn:path-prefix" as="xs:string">
    <xsl:param name="doc" as="element(document)?" />
    <xsl:variable name="prefix">
      <xsl:if test="$doc/@folder">
        <xsl:for-each select="tokenize($doc/@folder,'/')[position()!=last()]">
          <xsl:text>../</xsl:text>
        </xsl:for-each>
      </xsl:if>
    </xsl:variable>
    <xsl:value-of select="$prefix" />
  </xsl:function>
</xsl:stylesheet>
