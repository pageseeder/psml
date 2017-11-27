<xsl:stylesheet version="2.0"
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

 <!-- change all headings to level 3 -->
 <xsl:template match="heading">
   <heading level="3">
     <xsl:apply-templates select="node()" />
   </heading>
 </xsl:template>

 <!-- copy all other elements unchanged -->
 <xsl:template match="*">
    <xsl:copy>
       <xsl:copy-of select="@*" />
     <xsl:apply-templates select="node()" />
  </xsl:copy>
 </xsl:template>
</xsl:stylesheet>