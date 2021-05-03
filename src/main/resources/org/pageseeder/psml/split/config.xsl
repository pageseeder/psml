<?xml version="1.0" encoding="utf-8"?>
<!--
  XSLT module providing functions to access the configuration.

  All functions in this module rely on the configuration document. Only functions from the
  `http://pageseeder.org/psml/config` namespace can dispense with providing the configuration
  as parameter.

  @author Philip Rutherford
-->
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:config="http://pageseeder.org/psml/config"
                exclude-result-prefixes="#all">

  <!-- The configuration file -->
  <xsl:variable name="config-doc" select="document($_configfileurl)" as="node()"/>

  <!-- Returns the container config element with no children -->
  <xsl:function name="config:main-container" as="element(container)?">
    <xsl:sequence select="($config-doc/split-config/container[not(*)])[1]" />
  </xsl:function>

  <!-- Returns whether the config has any document elements -->
  <xsl:function name="config:has-document" as="xs:boolean">
    <xsl:sequence select="not(empty($config-doc/split-config/document))" />
  </xsl:function>

  <!-- Returns the container config element with start matching the given element -->
  <xsl:function name="config:split-container" as="element(container)?">
    <xsl:param name="el" as="element()" />

    <!-- handle the case of a heading inside a block -->
    <xsl:variable name="h" select="if (local-name($el) = 'heading') then $el else ($el/heading)[1]" />
    <xsl:variable name="block-match"
        select="($config-doc/split-config/container/start/block[@label=$el/@label])[1]" />
    <xsl:variable name="numbered-heading-match"
        select="($config-doc/split-config/container/start/heading[@level=$h/@level and @numbered='true' and $h/@numbered])[1]" />
    <xsl:variable name="not-numbered-heading-match"
        select="($config-doc/split-config/container/start/heading[@level=$h/@level and @numbered='false' and not($h/@numbered)])[1]" />
    <xsl:variable name="heading-match"
        select="($config-doc/split-config/container/start/heading[@level=$h/@level and not(@numbered)])[1]" />
    <xsl:choose>
      <!-- check block label -->
      <xsl:when test="local-name($el) = 'block' and $block-match">
        <xsl:sequence select="$block-match/../.." />
      </xsl:when>
      <!-- check numbered heading -->
      <xsl:when test="$numbered-heading-match">
        <xsl:sequence select="$numbered-heading-match/../.." />
      </xsl:when>
      <!-- check not numbered heading -->
      <xsl:when test="$not-numbered-heading-match">
        <xsl:sequence select="$not-numbered-heading-match/../.." />
      </xsl:when>
      <!-- check heading -->
      <xsl:when test="$heading-match">
        <xsl:sequence select="$heading-match/../.." />
      </xsl:when>
      <!-- when the document type changes check for a container, otherwise return the main container -->
      <xsl:when test="$el/@start-document and
          not($el/preceding::*[@start-document][1][@start-document=$el/@start-document or @start-document='-container'])">
        <xsl:variable name="c" select="($config-doc/split-config/container[@contains=$el/@start-document])[1]" />
        <xsl:sequence select="if ($c) then $c else config:main-container()" />
      </xsl:when>
    </xsl:choose>
  </xsl:function>

  <!-- Returns the container config element with continue matching the given element -->
  <xsl:function name="config:continue-container" as="element(container)?">
    <xsl:param name="el" as="element()" />

    <!-- handle the case of a heading inside a block -->
    <xsl:variable name="h" select="if (local-name($el) = 'heading') then $el else ($el/heading)[1]" />
    <xsl:variable name="block-match"
        select="($config-doc/split-config/container/continue/block[@label=$el/@label])[1]" />
    <xsl:variable name="numbered-heading-match"
        select="($config-doc/split-config/container/continue/heading[@level=$h/@level and @numbered='true' and $h/@numbered])[1]" />
    <xsl:variable name="not-numbered-heading-match"
        select="($config-doc/split-config/container/continue/heading[@level=$h/@level and @numbered='false' and not($h/@numbered)])[1]" />
    <xsl:variable name="heading-match"
        select="($config-doc/split-config/container/continue/heading[@level=$h/@level and not(@numbered)])[1]" />
    <xsl:choose>
      <!-- check block label -->
      <xsl:when test="local-name($el) = 'block' and $block-match">
        <xsl:sequence select="$block-match/../.." />
      </xsl:when>
      <!-- check numbered heading -->
      <xsl:when test="$numbered-heading-match">
        <xsl:sequence select="$numbered-heading-match/../.." />
      </xsl:when>
      <!-- check not numbered heading -->
      <xsl:when test="$not-numbered-heading-match">
        <xsl:sequence select="$not-numbered-heading-match/../.." />
      </xsl:when>
      <!-- check heading -->
      <xsl:when test="$heading-match">
        <xsl:sequence select="$heading-match/../.." />
      </xsl:when>
    </xsl:choose>
  </xsl:function>

  <!-- Returns the document config element matching the given element -->
  <xsl:function name="config:split-document" as="element(document)?">
    <xsl:param name="el" as="element()" />

    <!-- handle the case of a heading inside a block -->
    <xsl:variable name="h" select="if (local-name($el) = 'heading') then $el else ($el/heading)[1]" />
    <xsl:variable name="block-match"
        select="($config-doc/split-config/document/block[@label=$el/@label])[1]" />
    <xsl:variable name="numbered-heading-match"
        select="($config-doc/split-config/document/heading[@level=$h/@level and @numbered='true' and $h/@numbered])[1]" />
    <xsl:variable name="not-numbered-heading-match"
        select="($config-doc/split-config/document/heading[@level=$h/@level and @numbered='false' and not($h/@numbered)])[1]" />
    <xsl:variable name="heading-match"
        select="($config-doc/split-config/document/heading[@level=$h/@level and not(@numbered)])[1]" />
    <xsl:choose>
      <!-- check block label -->
      <xsl:when test="local-name($el) = 'block' and $block-match">
        <xsl:sequence select="$block-match/.." />
      </xsl:when>
      <!-- check numbered heading -->
      <xsl:when test="$numbered-heading-match">
        <xsl:sequence select="$numbered-heading-match/.." />
      </xsl:when>
      <!-- check not numbered heading -->
      <xsl:when test="$not-numbered-heading-match">
        <xsl:sequence select="$not-numbered-heading-match/.." />
      </xsl:when>
      <!-- check heading -->
      <xsl:when test="$heading-match">
        <xsl:sequence select="$heading-match/.." />
      </xsl:when>
    </xsl:choose>
  </xsl:function>

  <!-- Returns the document config element matching the given inline element -->
  <xsl:function name="config:document-inline" as="element(inline)?">
    <xsl:param name="el" as="element(inline)" />

    <xsl:sequence select="($config-doc/split-config/document/inline[@label=$el/@label])[1]" />
  </xsl:function>

  <!-- Returns the fragment config element matching the given element -->
  <xsl:function name="config:split-fragment" as="element(fragment)?">
    <xsl:param name="el" as="element()" />

    <!-- handle the case of a heading inside a block -->
    <xsl:variable name="h" select="if (local-name($el) = 'heading') then $el else ($el/heading)[1]" />
    <!-- handle the case of a para inside a block -->
    <xsl:variable name="p" select="if (local-name($el) = 'para') then $el else ($el/para)[1]" />
    <xsl:variable name="block-match"
        select="($config-doc/split-config/fragment/block[@label=$el/@label])[1]" />
    <xsl:variable name="numbered-heading-match"
        select="($config-doc/split-config/fragment/heading[@level=$h/@level and @numbered='true' and $h/@numbered])[1]" />
    <xsl:variable name="not-numbered-heading-match"
        select="($config-doc/split-config/fragment/heading[@level=$h/@level and @numbered='false' and not($h/@numbered)])[1]" />
    <xsl:variable name="heading-match"
        select="($config-doc/split-config/fragment/heading[@level=$h/@level and not(@numbered)])[1]" />
    <xsl:variable name="numbered-para-match"
        select="($config-doc/split-config/fragment/para[@numbered='true' and $p/@numbered])[1]" />
    <xsl:variable name="not-numbered-para-match"
        select="($config-doc/split-config/fragment/para[@numbered='false' and $p and not($p/@numbered)])[1]" />
    <xsl:variable name="para-match"
        select="($config-doc/split-config/fragment/para[$p and not(@numbered)])[1]" />
    <xsl:choose>
      <!-- check block label -->
      <xsl:when test="local-name($el) = 'block' and $block-match">
        <xsl:sequence select="$block-match/.." />
      </xsl:when>
      <!-- check numbered heading -->
      <xsl:when test="$numbered-heading-match">
        <xsl:sequence select="$numbered-heading-match/.." />
      </xsl:when>
      <!-- check not numbered heading -->
      <xsl:when test="$not-numbered-heading-match">
        <xsl:sequence select="$not-numbered-heading-match/.." />
      </xsl:when>
      <!-- check heading -->
      <xsl:when test="$heading-match">
        <xsl:sequence select="$heading-match/.." />
      </xsl:when>
      <!-- check numbered para -->
      <xsl:when test="$numbered-para-match">
        <xsl:sequence select="$numbered-para-match/.." />
      </xsl:when>
      <!-- check not numbered para -->
      <xsl:when test="$not-numbered-para-match">
        <xsl:sequence select="$not-numbered-para-match/.." />
      </xsl:when>
      <!-- check para -->
      <xsl:when test="$para-match">
        <xsl:sequence select="$para-match/.." />
      </xsl:when>
      <!-- always split properties and media fragments -->
      <xsl:when test="($el | $el/preceding-sibling::*[1])[self::properties-fragment or self::media-fragment]">
        <fragment/>
      </xsl:when>
    </xsl:choose>
  </xsl:function>

</xsl:stylesheet>
