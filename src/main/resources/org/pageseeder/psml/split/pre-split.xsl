<!--
  XSLT to pre-split a single PSML file according to rules in a configuration file.

  @author Philip Rutherford
-->
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:config="http://pageseeder.org/psml/config"
                exclude-result-prefixes="#all">

  <xsl:import href="config.xsl" />
  
  <!-- Output folder URL -->
  <xsl:param name="_outputfolder" as="xs:string"/>
  
  <!-- Configuration file URL -->
  <xsl:param name="_configfileurl" as="xs:string" />

  <!-- Generate main document -->
  <xsl:template match="/">
    <document level="portable"
        type="{if (config:main-container()/@type!='') then config:main-container()/@type else 'references'}">
      <documentinfo>
        <uri>
          <xsl:if test="document/documentinfo/uri/@title">
            <xsl:attribute name="title" select="document/documentinfo/uri/@title"/>
          </xsl:if>
          <xsl:if test="config:main-container()/@labels != ''">
            <labels><xsl:value-of select="config:main-container()/@labels"/></labels>
          </xsl:if>
        </uri>
      </documentinfo>
      
      <!-- output frontmatter -->
      <xsl:variable name="frontmatter"
          select="not((//fragment/*)[1][config:split-document(.)])" />
      <section id="title">
        <fragment id="1">
          <xsl:for-each-group select="//fragment/*" group-starting-with="*[config:split-document(.)]">
            <xsl:if test="position() = 1 and $frontmatter">
               <xsl:apply-templates select="current-group()" />
            </xsl:if>
          </xsl:for-each-group>
        </fragment>
      </section>
      <toc/>
      
      <!-- output components -->
      <section id="xrefs">
        <xref-fragment id="2">
          <xsl:for-each-group select="//fragment/*" group-starting-with="*[config:split-document(.)]">
            <xsl:if test="not(position() = 1) or not($frontmatter)">
              <xsl:variable name="first" select="current-group()[1]" />
              <xsl:variable name="config" select="config:split-document($first)" />
              
              <!-- output component document -->
              <document level="portable">
                <xsl:if test="$config/@type != ''">
                  <xsl:attribute name="type" select="$config/@type"/>
                </xsl:if>
                <xsl:if test="$config/@folder != ''">
                  <xsl:attribute name="folder" select="concat($config/@folder,'/')"/>
                </xsl:if>
                <documentinfo>
                  <uri>
                    <xsl:if test="normalize-space($first) != ''">
                      <xsl:attribute name="title" select="normalize-space($first)"/>
                    </xsl:if>
                    <xsl:if test="$config/@labels != ''">
                      <labels><xsl:value-of select="$config/@labels"/></labels>
                    </xsl:if>
                  </uri>
                </documentinfo>
                
                <!-- output component title -->
                <section id="title">
                  <fragment id="1">
                    <xsl:apply-templates select="$first" />
                  </fragment>
                </section>

                <!-- output component content -->
                <section id="content">
                  <xsl:for-each-group select="current-group()[not(position() = 1)]"
                        group-starting-with="*[config:split-fragment(.)]">
                    <xsl:variable name="config-frag" select="config:split-fragment(current-group()[1])" />
                    <fragment id="{position() + 1}">
                      <xsl:if test="$config-frag/@type != ''">
                        <xsl:attribute name="type" select="$config-frag/@type"/>
                      </xsl:if>
                      <xsl:if test="$config-frag/@labels != ''">
                        <xsl:attribute name="labels" select="$config-frag/@labels"/>
                      </xsl:if>
                      <xsl:apply-templates select="current-group()" />
                    </fragment>
                  </xsl:for-each-group>
                </section>
              </document>
            </xsl:if>
          </xsl:for-each-group>
        </xref-fragment>
      </section>
    </document>    
  </xsl:template>
  
  <!-- handle inline labels -->
  <xsl:template match="inline">
    <xsl:variable name="config" select="config:inline-document(.)" />
    <xsl:copy>
      <xsl:copy-of select="@*" />
      <xsl:choose>
        <xsl:when test="$config">
        
          <!-- output placeholder document -->
          <document level="portable">
            <xsl:if test="$config/@type != ''">
              <xsl:attribute name="type" select="$config/@type"/>
            </xsl:if>
            <xsl:if test="$config/@folder != ''">
              <xsl:attribute name="folder" select="concat($config/@folder,'/')"/>
            </xsl:if>
            <documentinfo>
              <uri>
                <xsl:if test="normalize-space(.) != ''">
                  <xsl:attribute name="title" select="normalize-space(.)"/>
                </xsl:if>
                <xsl:if test="$config/@labels != ''">
                  <labels><xsl:value-of select="$config/@labels"/></labels>
                </xsl:if>
                <xsl:if test="normalize-space(.) != ''">
                  <description><xsl:value-of select="normalize-space(.)"/></description>
                </xsl:if>
              </uri>
            </documentinfo>
            <section id="title" />
          </document>
        </xsl:when>
        <xsl:otherwise>
          <xsl:apply-templates select="node()" />
        </xsl:otherwise>
      </xsl:choose>
    </xsl:copy>
  </xsl:template>  

  <!-- ignore xref transclusion content -->
  <xsl:template match="xref[@type='transclude']">
    <xsl:copy>
      <xsl:copy-of select="@*" />
    </xsl:copy>
  </xsl:template>  
  
  <!-- copy all other elements unchanged -->
  <xsl:template match="*">
    <xsl:copy>
      <xsl:copy-of select="@*" />
      <xsl:apply-templates select="node()" />
    </xsl:copy>
  </xsl:template>  

</xsl:stylesheet>
