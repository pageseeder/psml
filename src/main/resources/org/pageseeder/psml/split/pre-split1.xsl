<!--
  XSLT to flatten a single PSML file and insert <start-document> tags for the pre-split process.

  @author Philip Rutherford
-->
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:config="http://pageseeder.org/psml/config"
                exclude-result-prefixes="#all">

  <xsl:import href="config.xsl" />

  <!-- Configuration file URL -->
  <xsl:param name="_configfileurl" as="xs:string" />

  <!-- Generate main document -->
  <xsl:template match="/">
    <document>
      <xsl:copy-of select="document/@*" />
      <xsl:apply-templates select="document/documentinfo" />
      <section>
        <fragment>
          <!-- Ignore top level blockxref with content and content of non-top level blockxref
               because their fragment content is included elsewhere -->
          <xsl:for-each-group select="(//fragment/* | //properties-fragment | //media-fragment)[not(self::blockxref/*
                or ancestor::blockxref[not(parent::fragment or parent::xref-fragment)])]"
              group-starting-with="*[config:split-document(.) or config:split-container(.)]">
            <xsl:variable name="first" select="current-group()[1]" />
            <!-- Add start-document attribute if not first group -->
            <xsl:choose>
              <xsl:when test="position() != 1">
                <xsl:for-each select="$first">
                  <xsl:copy>
                    <xsl:copy-of select="@*" />
                      <xsl:attribute name="start-document" select="if (config:split-container($first)/@contains)
                          then '-container' else config:split-document($first)/@type" />
                      <xsl:call-template name="add-fragment-anchor" />
                    <xsl:apply-templates select="node()" />
                  </xsl:copy>
                </xsl:for-each>
               </xsl:when>
               <xsl:otherwise>
                 <xsl:apply-templates select="$first" />
               </xsl:otherwise>
            </xsl:choose>
            <xsl:apply-templates select="current-group()[position() > 1]" />
          </xsl:for-each-group>
        </fragment>
      </section>
    </document>
  </xsl:template>

  <!-- Remove non-top level blockxref with content but keep it's content -->
  <xsl:template match="blockxref[*]">
      <xsl:call-template name="add-fragment-anchor" />
      <xsl:apply-templates select=".//fragment/*" />
  </xsl:template>

  <!-- Copy all other elements unchanged -->
  <xsl:template match="*">
    <xsl:copy>
      <xsl:copy-of select="@*" />
      <xsl:call-template name="add-fragment-anchor" />
      <xsl:apply-templates select="node()" />
    </xsl:copy>
  </xsl:template>

  <!-- Adds fragment anchors to preserve xrefs -->
  <xsl:template name="add-fragment-anchor">
    <xsl:choose>
      <xsl:when test="self::properties-fragment or self::media-fragment">
        <fragment-anchor id="{@id}" />
      </xsl:when>
      <xsl:when test="local-name(..) = 'fragment' and not(preceding-sibling::*)">
        <fragment-anchor id="{../@id}" />
        <!-- if first fragment in document add default anchor -->
        <xsl:if test="(ancestor::document[1]//fragment)[1]/@id = current()/../@id">
          <fragment-anchor id="{ancestor::document[1]/@id}" />
        </xsl:if>
      </xsl:when>
    </xsl:choose>
  </xsl:template>

</xsl:stylesheet>
