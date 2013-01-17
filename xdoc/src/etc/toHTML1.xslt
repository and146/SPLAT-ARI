<?xml version="1.0"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:java="http://xml.apache.org/xalan/java"
                xmlns:ImageIcon="xalan://javax.swing.ImageIcon"
                xmlns:File="xalan://java.io.File"
                exclude-result-prefixes="java ImageIcon File">

  <xsl:param name="JAVADOCS"
             select="'http://andromeda.star.bris.ac.uk/starjavadocs/'"/>
  <xsl:param name="VERSION" select="'???'"/>
  <xsl:param name="BASEDIR" select="'.'"/>
  <xsl:param name="COVERIMAGE" select="''"/>
  <xsl:param name="CSS_HREF" select="''"/>
  <xsl:param name="DOCTYPE" select="'Starlink User Note'"/>

  <!-- Top level element -->

  <xsl:template match="sun">
    <html>
      <head>
        <xsl:call-template name="cssStylesheet"/>
        <title>
          <xsl:apply-templates select="docinfo/title"/>
        </title>
      </head>
      <body>
        <xsl:apply-templates select="docinfo"/>
        <hr/>
        <h2>Contents</h2>
        <xsl:apply-templates select="docbody" mode="toc"/>
        <hr/>
        <xsl:apply-templates select="docbody"/>
        <xsl:call-template name="pageFooter"/>
      </body>
    </html>
  </xsl:template>


  <!-- normal processing -->

  <xsl:template match="docinfo">
    <h1 align="center">
      <xsl:apply-templates select="title"/>
      <br/>
      <xsl:text>Version </xsl:text>
      <xsl:call-template name="getVersion"/>
    </h1>
    <xsl:if test="$COVERIMAGE">
      <div align="center">
        <xsl:call-template name="outImg">
          <xsl:with-param name="src" select="$COVERIMAGE"/>
          <xsl:with-param name="alt" select="'Cover image'"/>
        </xsl:call-template>
      </div>
    </xsl:if>
    <hr/>
    <p>
      <i>
        <xsl:value-of select="$DOCTYPE"/>
        <xsl:text> </xsl:text>
        <xsl:apply-templates select="docnumber"/>
        <br/>
        <xsl:apply-templates select="authorlist"/>
        <br/>
        <xsl:apply-templates select="docdate"/>
        <br/>
        <xsl:apply-templates select="history"/>
      </i>
    </p>
  </xsl:template>

  <xsl:template match="author">
    <xsl:apply-templates/>
    <xsl:if test="following-sibling::author">
      <xsl:text>, </xsl:text>
    </xsl:if>
  </xsl:template>

  <xsl:template match="contact">
    <xsl:apply-templates/>
    <br/>
  </xsl:template>

  <xsl:template match="abstract">
    <h2><a name="abstract"/>Abstract</h2>
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="p|px">
    <p><xsl:apply-templates/></p>
  </xsl:template>

  <xsl:template match="dd/p[position()=1]">
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="ul|ol|li|dl|dd|blockquote|code|em|strong|sub|sup">
    <xsl:copy>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="m">
    <i>
      <xsl:apply-templates/>
    </i>
  </xsl:template>

  <xsl:template match="label">
    <b><xsl:apply-templates/></b>
  </xsl:template>

  <xsl:template match="var">
    <i><xsl:apply-templates/></i>
  </xsl:template>

  <xsl:template match="img">
    <xsl:call-template name="outImg">
      <xsl:with-param name="src" select="@src"/>
    </xsl:call-template>
  </xsl:template>

  <xsl:template match="figure">
    <div align="center">
      <xsl:call-template name="outImg">
        <xsl:with-param name="src" select="figureimage/@src"/>
        <xsl:with-param name="alt" select="caption/px[position()=1]"/>
      </xsl:call-template>
      <xsl:apply-templates select="caption"/>
    </div>
  </xsl:template>

  <xsl:template match="caption">
    <b>
      <xsl:apply-templates/>
    </b>
  </xsl:template>

  <xsl:template match="verbatim">
    <pre><xsl:apply-templates/></pre>
  </xsl:template>

  <xsl:template match="blockcode">
    <pre><xsl:apply-templates/></pre>
  </xsl:template>

  <xsl:template match="hidden"/>
  <xsl:template match="imports"/>

  <xsl:template match="dt">
    <xsl:copy>
      <strong>
        <xsl:apply-templates/>
      </strong>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="ref">
    <xsl:element name="a">
      <xsl:attribute name="href">
        <xsl:call-template name="getRef">
          <xsl:with-param name="node" select="id(@id)"/>
        </xsl:call-template>
      </xsl:attribute>
      <xsl:choose>
        <xsl:when test="string(.)">
          <xsl:apply-templates/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:apply-templates mode="sectype" select="id(@id)"/>
          <xsl:text> </xsl:text>
          <xsl:apply-templates mode="ref" select="id(@id)"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:element>
  </xsl:template>

  <xsl:template match="webref">
    <a href="{@url}">
      <xsl:choose>
        <xsl:when test="string(.)">
          <xsl:apply-templates/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="@url"/>
        </xsl:otherwise>
      </xsl:choose>
    </a>
  </xsl:template>

  <xsl:template match="docxref">
    <xsl:element name="a">
      <xsl:attribute name="href">
        <xsl:call-template name="docRefUrl">
          <xsl:with-param name="doc" select="@doc"/>
          <xsl:with-param name="loc" select="@loc"/>
        </xsl:call-template>
      </xsl:attribute>
      <xsl:choose>
        <xsl:when test="string(.)">
          <xsl:apply-templates/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:call-template name="docRefText">
            <xsl:with-param name="doc" select="@doc"/>
            <xsl:with-param name="loc" select="@loc"/>
          </xsl:call-template>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:element>
  </xsl:template>

  <xsl:template match="javadoc">
    <xsl:element name="a">
      <xsl:attribute name="href">
        <xsl:choose>
          <xsl:when test="@docset">
            <xsl:value-of select="@docset"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="$JAVADOCS"/>
          </xsl:otherwise>
        </xsl:choose>
        <xsl:value-of select="translate(@class, '.', '/')"/>
        <xsl:choose>
          <xsl:when test="@class='.'">
            <xsl:value-of select="'index.html'"/>
          </xsl:when>
          <xsl:when test="substring(@class, string-length(@class))='.'">
            <xsl:value-of select="'package-summary.html'"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="'.html'"/>
          </xsl:otherwise>
        </xsl:choose>
        <xsl:if test="@member"> 
          <xsl:value-of select="'#'"/>
          <xsl:value-of select="normalize-space(@member)"/>
        </xsl:if>
      </xsl:attribute>
      <xsl:choose>
        <xsl:when test="@codetext">
          <code>
            <xsl:value-of select="@codetext"/>
          </code>
        </xsl:when>
        <xsl:when test="string(.)">
          <xsl:apply-templates/>
        </xsl:when>
        <xsl:otherwise>
          <code>
            <xsl:choose>
              <xsl:when test="substring(@class, string-length(@class))='.'">
                <xsl:value-of 
                     select="substring(@class, 1, string-length(@class)-1)"/>
              </xsl:when>
              <xsl:when test="@member">
                <xsl:choose>
                  <xsl:when test="contains(@member, '(')">
                    <xsl:value-of select="substring-before(@member, '(')"/>
                  </xsl:when>
                  <xsl:otherwise>
                    <xsl:value-of select="@member"/>
                  </xsl:otherwise>
                </xsl:choose>
              </xsl:when>
              <xsl:otherwise>
                <xsl:call-template name="lastPart">
                  <xsl:with-param name="text" select="@class"/>
                </xsl:call-template>
              </xsl:otherwise>
            </xsl:choose>
          </code>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:element>
  </xsl:template>


  <!-- titles -->

  <xsl:template 
      match="docbody/sect/subhead/title
            |appendices/sect/subhead/title
            |appendices/sect/subsect/subhead/title">
    <hr/>
    <h2>
      <xsl:element name="a">
        <xsl:attribute name="name">
          <xsl:call-template name="getId">
            <xsl:with-param name="node" select="../.."/>
          </xsl:call-template>
        </xsl:attribute>
        <xsl:apply-templates mode="ref" select="../.."/>
        <xsl:text> </xsl:text>
        <xsl:apply-templates/>
      </xsl:element>
    </h2>
  </xsl:template>

  <xsl:template
      match="docbody/sect/subsect/subhead/title
            |appendices/sect/subsect/subsubsect/subhead/title">
    <h3>
      <xsl:element name="a">
        <xsl:attribute name="name">
          <xsl:call-template name="getId">
            <xsl:with-param name="node" select="../.."/>
          </xsl:call-template>
        </xsl:attribute>
        <xsl:apply-templates mode="ref" select="../.."/>
        <xsl:text> </xsl:text>
        <xsl:apply-templates/>
      </xsl:element>
    </h3>
  </xsl:template>

  <xsl:template
      match="docbody/sect/subsect/subsubsect/subhead/title
            |appendices/sect/subsect/subsubsect/subsubsubsect/subhead/title">
    <h4>
      <xsl:element name="a">
        <xsl:attribute name="name">
          <xsl:call-template name="getId">
            <xsl:with-param name="node" select="../.."/>
          </xsl:call-template>
        </xsl:attribute>
        <xsl:apply-templates mode="ref" select="../.."/>
        <xsl:text> </xsl:text>
        <xsl:apply-templates/>
      </xsl:element>
    </h4>
  </xsl:template>

  <xsl:template
       match="docbody/sect/subsect/subsubsect/subsubsubsect/subhead/title">
    <h4>
      <xsl:element name="a">
        <xsl:attribute name="name">
          <xsl:call-template name="getId">
            <xsl:with-param name="node" select="../.."/>
          </xsl:call-template>
        </xsl:attribute>
        <xsl:apply-templates mode="ref" select="../.."/>
        <xsl:text> </xsl:text>
        <xsl:apply-templates/>
      </xsl:element>
    </h4>
  </xsl:template>


  <!-- table of contents -->

  <xsl:template mode="toc" match="docbody">
    <ul>
      <xsl:apply-templates mode="toc" 
                           select="abstract|sect|appendices/sect"/>
    </ul>
  </xsl:template>

  <xsl:template mode="toc" match="title">
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template mode="toc" match="abstract">
    <li>
      <xsl:element name="a">
        <xsl:attribute name="href">
          <xsl:call-template name="getRef"/>
        </xsl:attribute>
        <xsl:text>Abstract</xsl:text>
      </xsl:element>
    </li>
  </xsl:template>

  <xsl:template mode="toc" match="sect">
    <li>
      <xsl:element name="a">
        <xsl:attribute name="href">
          <xsl:call-template name="getRef"/>
        </xsl:attribute>
        <xsl:apply-templates mode="ref" select="."/>
        <xsl:text> </xsl:text>
        <xsl:apply-templates mode="toc" select="subhead/title"/>
      </xsl:element>
    </li>
    <xsl:if test="not(@tocleaf='yes')">
      <xsl:if test="subsect">
        <ul>
          <xsl:apply-templates mode="toc" select="subsect"/>
        </ul>
      </xsl:if>
    </xsl:if>
  </xsl:template>

  <xsl:template mode="toc" match="subsect">
    <li>
      <xsl:element name="a">
        <xsl:attribute name="href">
          <xsl:call-template name="getRef"/>
        </xsl:attribute>
        <xsl:apply-templates mode="ref" select="."/>
        <xsl:text> </xsl:text>
        <xsl:apply-templates mode="toc" select="subhead/title"/>
      </xsl:element>
    </li>
    <xsl:if test="not(@tocleaf='yes')">
      <xsl:if test="subsubsect">
        <ul>
          <xsl:apply-templates mode="toc" select="subsubsect"/>
        </ul>
      </xsl:if>
    </xsl:if>
  </xsl:template>

  <xsl:template mode="toc" match="subsubsect">
    <li>
      <xsl:element name="a">
        <xsl:attribute name="href">
          <xsl:call-template name="getRef"/>
        </xsl:attribute>
        <xsl:apply-templates mode="ref" select="."/>
        <xsl:text> </xsl:text>
        <xsl:apply-templates mode="toc" select="subhead/title"/>
      </xsl:element>
    </li>
    <xsl:if test="not(@tocleaf='yes')">
      <xsl:if test="subsubsubsect">
        <ul>
          <xsl:apply-templates mode="toc" select="subsubsubsect"/>
        </ul>
      </xsl:if>
    </xsl:if>
  </xsl:template>

  <xsl:template mode="toc" match="subsubsubsect">
    <li>
      <xsl:element name="a">
        <xsl:attribute name="href">
          <xsl:call-template name="getRef"/>
        </xsl:attribute>
        <xsl:apply-templates mode="ref" select="."/>
        <xsl:text> </xsl:text>
        <xsl:apply-templates mode="toc" select="subhead/title"/>
      </xsl:element>
    </li>
  </xsl:template>


  <!-- section numbering -->

  <xsl:template mode="ref" match="sect">
    <xsl:number count="sect"/>
  </xsl:template>

  <xsl:template mode="ref" match="appendices/sect">
    <xsl:number count="sect" format="A"/>
  </xsl:template>

  <xsl:template mode="ref" match="subsect">
    <xsl:apply-templates mode="ref" select=".."/>
    <xsl:text>.</xsl:text>
    <xsl:number count="subsect"/>
  </xsl:template>

  <xsl:template mode="ref" match="subsubsect">
    <xsl:apply-templates mode="ref" select=".."/>
    <xsl:text>.</xsl:text>
    <xsl:number count="subsubsect"/>
  </xsl:template>

  <xsl:template mode="ref" match="subsubsubsect">
    <xsl:apply-templates mode="ref" select=".."/>
    <xsl:text>.</xsl:text>
    <xsl:number count="subsubsubsect"/>
  </xsl:template>


  <!-- named section reference -->

  <xsl:template mode="nameref" match="sect|subsect|subsubsect|subsubsubsect">
    <xsl:apply-templates mode="nameref" select="subhead/title"/>
  </xsl:template>

  <xsl:template mode="nameref" match="appendices">
    <xsl:text>Appendices</xsl:text>
  </xsl:template>

  <xsl:template mode="nameref" match="title">
    <xsl:apply-templates mode="nameref"/>
  </xsl:template>

  <xsl:template mode="nameref" match="abstract">
    <xsl:text>Abstract</xsl:text>
  </xsl:template>

  <xsl:template mode="nameref" match="docbody|docinfo">
    <xsl:text>Top</xsl:text>
  </xsl:template>

  <!-- section type description -->

  <xsl:template mode="sectype" match="sect|subsect|subsubsect|subsubsubsect">
    <xsl:choose>
      <xsl:when test="ancestor::appendices">
        <xsl:text>Appendix</xsl:text>
      </xsl:when>
      <xsl:when test="ancestor-or-self::sect">
        <xsl:text>Section</xsl:text>
      </xsl:when>
      <xsl:otherwise>
        <xsl:text>item??</xsl:text>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- subroutines -->

  <xsl:template name="lastPart">
    <xsl:param name="text"/>
    <xsl:choose>
      <xsl:when test="contains($text, '.')">
        <xsl:call-template name="lastPart">
          <xsl:with-param name="text" select="substring-after($text, '.')"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$text"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="getId">
    <xsl:param name="node" select="."/>
    <xsl:choose>
      <xsl:when test="$node/@id">
        <xsl:value-of select="$node/@id"/>
      </xsl:when>
      <xsl:when test="name($node)='abstract'">
        <xsl:text>abstract</xsl:text>
      </xsl:when>
      <xsl:when test="name($node)='sun' or
                      name($node)='docinfo' or
                      name($node)='docbody'">
        <xsl:text>sun</xsl:text>
        <xsl:value-of select="/sun/docinfo/docnumber"/>
      </xsl:when>
      <xsl:when test="name($node)='sect' or
                      name($node)='subsect' or
                      name($node)='subsubsect' or
                      name($node)='subsubsubsect'">
        <xsl:text>sec</xsl:text>
        <xsl:apply-templates mode="ref" select="$node"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="generate-id($node)"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="getRef">
    <xsl:param name="node" select="."/>
    <xsl:text>#</xsl:text>
    <xsl:call-template name="getId">
      <xsl:with-param name="node" select="$node"/>
    </xsl:call-template>
  </xsl:template>

  <xsl:template name="pageFooter">
    <hr/>
    <i>
      <xsl:apply-templates select="/sun/docinfo/title"/>
      <br/>
      <xsl:value-of select="$DOCTYPE"/>
      <xsl:apply-templates select="/sun/docinfo/docnumber"/>
      <br/>
      <xsl:apply-templates select="/sun/docinfo/contactlist"/>
    </i>
  </xsl:template>

  <xsl:template name="getVersion">
    <xsl:choose>
      <xsl:when test="/sun/docinfo/softwareversion">
        <xsl:apply-templates select="/sun/docinfo/softwareversion"/>
      </xsl:when>
      <xsl:when test="$VERSION">
        <xsl:value-of select="$VERSION"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:text>???</xsl:text>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="docRefText">
    <xsl:param name="doc"/>
    <xsl:param name="loc"/>
    <xsl:choose>
      <xsl:when test="$doc='sun243'">
        <xsl:text>SUN/243</xsl:text>
      </xsl:when>
      <xsl:when test="$doc='sun252'">
        <xsl:text>SUN/252</xsl:text>
      </xsl:when>
      <xsl:when test="$doc='sun253'">
        <xsl:text>SUN/253</xsl:text>
      </xsl:when>
      <xsl:when test="$doc='sun256'">
        <xsl:text>SUN/256</xsl:text>
      </xsl:when>
      <xsl:otherwise>
        <xsl:message terminate="yes">
          <xsl:text>Unknown document ID $doc</xsl:text>
        </xsl:message>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="docRefUrl">
    <xsl:param name="doc"/>
    <xsl:param name="loc"/>
    <xsl:choose>
      <xsl:when test="$doc='sun243'">
        <xsl:text>http://www.starlink.ac.uk/star/docs/sun243.htx/sun243.html</xsl:text>
      </xsl:when>
      <xsl:when test="$doc='sun252'">
        <xsl:text>http://www.starlink.ac.uk/stil/sun252/</xsl:text>
      </xsl:when>
      <xsl:when test="$doc='sun253'">
        <xsl:text>http://www.starlink.ac.uk/topcat/sun253/</xsl:text>
      </xsl:when>
      <xsl:when test="$doc='sun256'">
        <xsl:text>http://www.starlink.ac.uk/stilts/sun256/</xsl:text>
      </xsl:when>
      <xsl:otherwise>
        <xsl:message terminate="yes">
          <xsl:text>Unknown document ID $doc</xsl:text>
        </xsl:message>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:if test="$loc">
      <xsl:value-of select="$loc"/>
      <xsl:text>.html</xsl:text>
    </xsl:if>
  </xsl:template>

  <xsl:template name="outImg">
    <xsl:param name="src"/>
    <xsl:param name="alt"/>
    <xsl:element name="img">
      <xsl:attribute name="src">
        <xsl:value-of select="$src"/>
      </xsl:attribute>
      <xsl:attribute name="alt">
        <xsl:value-of select="$alt"/>
      </xsl:attribute>
      <xsl:attribute name="align">
        <xsl:text>middle</xsl:text>
      </xsl:attribute>
      <xsl:if test="function-available('ImageIcon:getIconWidth')">
        <xsl:variable name="srcFile" select="File:new($BASEDIR,$src)"/>
        <xsl:variable name="srcLoc" select="string(java:toString($srcFile))"/>
        <xsl:variable name="icon" select="ImageIcon:new($srcLoc)"/>
        <xsl:variable name="width" select="java:getIconWidth($icon)"/>
        <xsl:variable name="height" select="java:getIconHeight($icon)"/>
        <xsl:if test="$width&gt;=0">
          <xsl:attribute name="width">
            <xsl:value-of select="$width"/>
          </xsl:attribute>
        </xsl:if>
        <xsl:if test="$height&gt;=0">
          <xsl:attribute name="height">
            <xsl:value-of select="$height"/>
          </xsl:attribute>
        </xsl:if>
      </xsl:if>
    </xsl:element>
  </xsl:template>

  <xsl:template name="cssStylesheet">
    <xsl:if test="$CSS_HREF">
      <xsl:element name="link">
        <xsl:attribute name="rel">stylesheet</xsl:attribute>
        <xsl:attribute name="type">text/css</xsl:attribute>
        <xsl:attribute name="href">
          <xsl:value-of select="$CSS_HREF"/>
        </xsl:attribute>
      </xsl:element>
    </xsl:if>
  </xsl:template>

</xsl:stylesheet>
