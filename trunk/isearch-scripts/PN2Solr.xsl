<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0">
<xsl:output indent="yes" method="xml" />
    <xsl:template match="/DOCS">
    <add>
    <xsl:apply-templates select="DOC"/>    
    </add>
</xsl:template>
    
	<xsl:template match="DOC">
	<doc>
        <!-- DocID -->
        <field name="id">isearch-<xsl:value-of select="normalize-space(DOCNO/text())"/></field>
        
        <!-- COLLECTION -->
        <field name="collection">isearch-PN</field>
        
	    <!-- URL -->
        <field name="url"><xsl:value-of select="normalize-space(DOCUMENTLINK/text())"/></field>
        
        <!-- Classification -->
        <field name="classification"><xsl:value-of select="normalize-space(CATEGORY/text())"/></field>
        
        <!-- TITLE -->
        <field name="title"><xsl:value-of select="normalize-space(TITLE/text())"/></field>        
        
        <!-- AUTHORS -->
        <xsl:variable name="tokenizedAuthors" select="tokenize(AUTHOR/text(),'\n+')"/>        
        <xsl:for-each select="$tokenizedAuthors">
            <xsl:if test="not(contains(lower-case(.),'collaboration') 
                or contains(lower-case(.),'for the')) and string-length(normalize-space(.)) > 0">
                <field name="author"><xsl:value-of select="normalize-space(.)"/></field>
            </xsl:if>            
        </xsl:for-each>

        <!-- ABSTRACT -->
        <field name="abstract"><xsl:value-of select="normalize-space(DESCRIPTION/text())"/></field>

		<!-- RAWSOURCE -->
	    <field name="rawsource"><xsl:value-of select="normalize-space(VENUE/text())"/></field>		
		
        <!-- SOURCE -->
        <xsl:variable name="possibleSource" select="tokenize(VENUE/text(),',')"/>
        <xsl:if test="not(empty($possibleSource))">
            <xsl:variable name="rawJournal" select="replace($possibleSource[1],'[^A-Za-z^&amp;]',' ')"></xsl:variable>
            <field name="source"><xsl:value-of select="normalize-space($rawJournal)"/></field>
        </xsl:if>
        
        <!-- PUBYEAR in parantheses -->
        
        <xsl:variable name="maxYear">2013</xsl:variable>        
        <xsl:variable name="tokenizedPossibleYear" select="tokenize(VENUE/text(),'\(')"/>    
        <xsl:for-each select="$tokenizedPossibleYear">                             
            <xsl:variable name="possibleYear" select="substring-before(.,')')"/>            
            <xsl:if test="string-length($possibleYear)=4 and number($possibleYear)&lt;$maxYear">
                <field name="pubyear"><xsl:value-of select="$possibleYear"/></field>
            </xsl:if>
        </xsl:for-each>
        
        <!-- PUBYEAR at the end of VENUE -->
        <xsl:variable name="tokenizedPossibleYear2" select="tokenize(VENUE/text(),',')"/>    
        <xsl:if test="not(empty($tokenizedPossibleYear2))">
            <!-- Take the end of the venue string, remove all non-word chars and test if it is 4 chars long -->
            <xsl:variable name="possibleYear" select="replace(normalize-space($tokenizedPossibleYear2[last()]),'\W','')"/>            
            <xsl:if test="string-length($possibleYear)=4 and number($possibleYear)&lt;$maxYear">                
                <field name="pubyear"><xsl:value-of select="$possibleYear"/></field>
            </xsl:if>
        </xsl:if>        
	</doc>
    </xsl:template>
</xsl:stylesheet>
