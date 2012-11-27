<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:d="http://nlp.stanford.edu/CoreNLP/v1">

  <xsl:output method="html"/>

  <xsl:template match="/">
    <html>
      <body>
        <hr size="3" color="#333333"/>

        <xsl:for-each select="root/document/sentences/sentence">

          <p>
            <xsl:apply-templates select=".">
              <xsl:with-param name="position" select="position()"/>
            </xsl:apply-templates>
          </p>
          
        </xsl:for-each>

      </body>
    </html>
  </xsl:template>

  <xsl:template match="root/document/sentences/sentence">
    <xsl:param name="position" select="'0'"/>

    <h4>Sentence #
      <xsl:value-of select="$position"/>
    </h4>
    <br />

    <xsl:for-each select="tokens/token">
      
      <xsl:choose>
        <xsl:when test='link'>
          
          <a>|
            <xsl:attribute name="href">
              http://www.freebase.com
              <xsl:value-of select="link" />
            </xsl:attribute>
            <xsl:value-of select="text"/>
            <xsl:text> </xsl:text>
          |</a>      
          
        </xsl:when>
        <xsl:otherwise>
          
          <xsl:value-of select="text"/>
          <xsl:text> </xsl:text>
          
        </xsl:otherwise>
      </xsl:choose>

    </xsl:for-each>
  
  
  </xsl:template>


</xsl:stylesheet>
