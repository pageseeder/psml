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
                xmlns:config="http://pageseeder.org/psml/config"
                exclude-result-prefixes="#all">
 
  <!-- The configuration file -->
  <xsl:variable name="config-doc" select="document($_configfileurl)" as="node()"/>
                
  <!-- Returns the container config element with no children -->
  <xsl:function name="config:main-container" as="element(container)?">
    <xsl:sequence select="($config-doc/split-config/container[not(*)])[1]" />
  </xsl:function>
  
  <!-- Returns the document config element matching the given element -->
  <xsl:function name="config:split-document" as="element(document)?">
    <xsl:param name="el" as="element()" />
    
    <xsl:variable name="block-match"
        select="($config-doc/split-config/document/block[@label=$el/@label])[1]" />
    <xsl:variable name="numbered-heading-match"
        select="($config-doc/split-config/document/heading[@level=$el/@level and @numbered='true' and $el/@numbered])[1]" />
    <xsl:variable name="not-numbered-heading-match"
        select="($config-doc/split-config/document/heading[@level=$el/@level and @numbered='false' and not($el/@numbered)])[1]" />
    <xsl:variable name="heading-match"
        select="($config-doc/split-config/document/heading[@level=$el/@level and not(@numbered)])[1]" />
    <xsl:choose>
      <!-- check block label -->
      <xsl:when test="local-name($el) = 'block' and $block-match">
        <xsl:sequence select="$block-match/.." />
      </xsl:when>
      <!-- check numbered heading -->
      <xsl:when test="local-name($el) = 'heading' and $numbered-heading-match">
        <xsl:sequence select="$numbered-heading-match/.." />
      </xsl:when>
      <!-- check not numbered heading -->
      <xsl:when test="local-name($el) = 'heading' and $not-numbered-heading-match">
        <xsl:sequence select="$not-numbered-heading-match/.." />
      </xsl:when>
      <!-- check heading -->
      <xsl:when test="local-name($el) = 'heading' and $heading-match">
        <xsl:sequence select="$heading-match/.." />
      </xsl:when>
    </xsl:choose>
  </xsl:function>

  <!-- Returns the document config element matching the given inline element -->
  <xsl:function name="config:inline-document" as="element(document)?">
    <xsl:param name="el" as="element(inline)" />
    
    <xsl:sequence select="($config-doc/split-config/document/inline[@label=$el/@label])[1]/.." />
  </xsl:function>
  
  <!-- Returns the fragment config element matching the given element -->
  <xsl:function name="config:split-fragment" as="element(fragment)?">
    <xsl:param name="el" as="element()" />
    
    <xsl:variable name="block-match"
        select="($config-doc/split-config/fragment/block[@label=$el/@label])[1]" />
    <xsl:variable name="numbered-heading-match"
        select="($config-doc/split-config/fragment/heading[@level=$el/@level and @numbered='true' and $el/@numbered])[1]" />
    <xsl:variable name="not-numbered-heading-match"
        select="($config-doc/split-config/fragment/heading[@level=$el/@level and @numbered='false' and not($el/@numbered)])[1]" />
    <xsl:variable name="heading-match"
        select="($config-doc/split-config/fragment/heading[@level=$el/@level and not(@numbered)])[1]" />
    <xsl:variable name="numbered-para-match"
        select="($config-doc/split-config/fragment/para[@numbered='true' and $el/@numbered])[1]" />
    <xsl:variable name="not-numbered-para-match"
        select="($config-doc/split-config/fragment/para[@numbered='false' and not($el/@numbered)])[1]" />
    <xsl:variable name="para-match"
        select="($config-doc/split-config/fragment/para[not(@numbered)])[1]" />
    <xsl:choose>
      <!-- check block label -->
      <xsl:when test="local-name($el) = 'block' and $block-match">
        <xsl:sequence select="$block-match/.." />
      </xsl:when>
      <!-- check numbered heading -->
      <xsl:when test="local-name($el) = 'heading' and $numbered-heading-match">
        <xsl:sequence select="$numbered-heading-match/.." />
      </xsl:when>
      <!-- check not numbered heading -->
      <xsl:when test="local-name($el) = 'heading' and $not-numbered-heading-match">
        <xsl:sequence select="$not-numbered-heading-match/.." />
      </xsl:when>
      <!-- check heading -->
      <xsl:when test="local-name($el) = 'heading' and $heading-match">
        <xsl:sequence select="$heading-match/.." />
      </xsl:when>
      <!-- check numbered para -->
      <xsl:when test="local-name($el) = 'para' and $numbered-para-match">
        <xsl:sequence select="$numbered-para-match/.." />
      </xsl:when>
      <!-- check not numbered para -->
      <xsl:when test="local-name($el) = 'para' and $not-numbered-para-match">
        <xsl:sequence select="$not-numbered-para-match/.." />
      </xsl:when>
      <!-- check para -->
      <xsl:when test="local-name($el) = 'para' and $para-match">
        <xsl:sequence select="$para-match/.." />
      </xsl:when>
    </xsl:choose>
  </xsl:function>
 
</xsl:stylesheet>
