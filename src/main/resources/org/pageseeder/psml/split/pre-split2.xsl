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
    <xsl:variable name="main-container" select="config:main-container()" />
    <document level="portable"
        type="{if ($main-container/@type!='') then $main-container/@type else 'references'}">
      <documentinfo>
        <uri>
          <xsl:if test="document/documentinfo/uri/@title">
            <xsl:attribute name="title" select="document/documentinfo/uri/@title"/>
          </xsl:if>
          <xsl:if test="$main-container/@labels != ''">
            <labels><xsl:value-of select="$main-container/@labels"/></labels>
          </xsl:if>
        </uri>
      </documentinfo>
      
      <!-- output front matter -->
      <xsl:variable name="frontmatter"
          select="not(config:split-container((//fragment/*)[1]))" />
      <section id="title">
        <fragment id="1">
          <xsl:for-each-group select="//fragment/*" group-starting-with="*[config:split-container(.)]">
            <xsl:if test="position() = 1 and $frontmatter">
               <xsl:apply-templates select="current-group()" />
            </xsl:if>
          </xsl:for-each-group>
        </fragment>
      </section>
      <toc/>
      
      <!-- output containers -->
      <section id="xrefs">
        <xref-fragment id="2">
          <xsl:for-each-group select="//fragment/*" group-starting-with="*[config:split-container(.)]">
            <xsl:if test="not(position() = 1) or not($frontmatter)">
              <xsl:variable name="first" select="current-group()[1]" />
              <xsl:variable name="config" select="config:split-container($first)" />
              <xsl:choose>
                <xsl:when test="deep-equal($config, $main-container)">
                  <!-- output components -->
                  <xsl:for-each-group select="current-group()" group-starting-with="*[config:split-document(.)]">                  
                    <xsl:call-template name="output-component" />
                  </xsl:for-each-group>
                </xsl:when>
                <xsl:otherwise>
                  <!-- output container document -->
                  <xsl:variable name="frontmatter"
                      select="not(config:split-document($first) and not(config:split-container($first)/start))" />
                  <xsl:variable name="endmatter" select="current-group()[config:continue-container(.)]" />
                  <document level="portable"
                      type="{if ($config/@type != '') then $config/@type else 'references'}">
                    <xsl:if test="$config/@folder != ''">
                      <xsl:attribute name="folder" select="concat($config/@folder,'/')"/>
                    </xsl:if>
                    <documentinfo>
                      <uri title="{if (normalize-space($first) != '' and $frontmatter) then normalize-space($first)
                          else if ($config/@type != '') then concat(upper-case(substring($config/@type,1,1)),substring($config/@type, 2))
                          else 'References'}">
                        <xsl:if test="$config/@labels != ''">
                          <labels><xsl:value-of select="$config/@labels"/></labels>
                        </xsl:if>
                      </uri>
                    </documentinfo>
                    
                    <!-- output container front matter -->
                    <section id="title">
                      <fragment id="1">
                        <xsl:for-each-group select="current-group()"
                            group-starting-with="*[config:split-document(.) and not(config:split-container(.)/start)]">
                          <xsl:if test="position() = 1 and $frontmatter">
                             <xsl:apply-templates select="current-group()" />
                          </xsl:if>
                        </xsl:for-each-group>
                      </fragment>
                    </section>
                    <toc/>
              
                    <!-- output container components -->
                    <section id="xrefs">
                      <xref-fragment id="2">
                        <!-- output components -->
                        <xsl:for-each-group select="current-group()"
                            group-starting-with="*[(config:split-document(.) and not(config:split-container(.)/start))
                                or config:continue-container(.)]">
                          <xsl:if test="(not(position() = 1) or not($frontmatter)) and
                              (not(position() = last()) or not($endmatter))">
                            <xsl:call-template name="output-component" />
                          </xsl:if>
                        </xsl:for-each-group>
                      </xref-fragment>
                    </section>
                  
                    <!-- output container end matter -->
                    <section id="content">
                      <xsl:for-each-group select="current-group()"
                          group-starting-with="*[config:split-document(.) or config:continue-container(.)]">
                        <xsl:if test="position() = last() and $endmatter">
                          <xsl:for-each-group select="current-group()"
                                              group-starting-with="*[config:split-fragment(.)]">
                            <xsl:variable name="config-frag" select="config:split-fragment(current-group()[1])" />
                            <fragment id="{position() + 2}">
                              <xsl:if test="$config-frag/@type != ''">
                                <xsl:attribute name="type" select="$config-frag/@type"/>
                              </xsl:if>
                              <xsl:if test="$config-frag/@labels != ''">
                                <xsl:attribute name="labels" select="$config-frag/@labels"/>
                              </xsl:if>
                              <xsl:apply-templates select="current-group()" />
                            </fragment>
                          </xsl:for-each-group>
                        </xsl:if>
                      </xsl:for-each-group>
                    </section>
                  </document>
                </xsl:otherwise>
              </xsl:choose>
            </xsl:if>
          </xsl:for-each-group>
        </xref-fragment>
      </section>
    </document>    
  </xsl:template>
  
  <!-- output component -->
  <xsl:template name="output-component">
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
          <xsl:choose>
            <xsl:when test="$first/heading and normalize-space($first/heading[1]) != ''">
              <xsl:attribute name="title" select="normalize-space($first/heading[1])"/>
            </xsl:when>
            <xsl:when test="normalize-space($first) != ''">
              <xsl:attribute name="title" select="normalize-space($first)"/>
            </xsl:when>
            </xsl:choose>
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
  </xsl:template>
  
  <!-- handle inline labels -->
  <xsl:template match="inline">
    <xsl:variable name="inline" select="config:document-inline(.)" />
    <xsl:variable name="config" select="$inline/.." />
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
            <xsl:variable name="filename">
              <xsl:choose>
                <xsl:when test="$inline[@as='filename' or @as='title-filename']">
                  <xsl:value-of select="normalize-space(string-join(text(), ''))"/>
                </xsl:when>
                <xsl:when test="$inline/inline[@as='filename' or @as='title-filename']
                    and inline[@label=$inline/inline/@label]">
                  <xsl:value-of select="normalize-space(inline[@label=$inline/inline/@label])"/>
                </xsl:when>
              </xsl:choose>
            </xsl:variable>
            <xsl:if test="$filename != ''">
              <xsl:attribute name="filename"
                  select="lower-case(concat(translate($filename,' ','_'),'.psml'))"/>
            </xsl:if>
            <documentinfo>
              <xsl:variable name="title">
                <xsl:choose>
                  <xsl:when test="$inline[@as='title' or @as='title-filename']">
                    <xsl:value-of select="if ($inline/@as='title-filename')
                        then concat('[',string-join(text(), ''),']')
                        else text()"/>
                  </xsl:when>
                  <xsl:when test="$inline/inline[@as='title' or @as='title-filename']
                      and inline[@label=$inline/inline/@label]">
                    <xsl:value-of select="if ($inline/inline/@as='title-filename')
                        then concat('[',inline[@label=$inline/inline/@label],']')
                        else inline[@label=$inline/inline/@label]"/>
                  </xsl:when>
                  <xsl:when test="normalize-space(.) != ''">
                    <xsl:value-of select="normalize-space(.)"/>
                  </xsl:when>
                </xsl:choose>
              </xsl:variable>
              <uri>
                <xsl:if test="$title != ''">
                  <xsl:attribute name="title" select="$title"/>
                </xsl:if>
                <xsl:if test="$config/@labels != ''">
                  <labels><xsl:value-of select="$config/@labels"/></labels>
                </xsl:if>
                <xsl:if test="$title != ''">
                  <description><xsl:value-of select="$title"/></description>
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
   
  <!-- copy all other elements removing @start-document -->
  <xsl:template match="*">
    <xsl:copy>
      <xsl:copy-of select="@*[name()!='start-document']" />
      <xsl:apply-templates select="node()" />
    </xsl:copy>
  </xsl:template>  

</xsl:stylesheet>
