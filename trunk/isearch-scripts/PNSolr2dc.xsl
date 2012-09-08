<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0">
    <xsl:output indent="yes" method="xml"/>
    <xsl:template match="/">

        <harvest>
            <OAI-PMH>
                <ListRecords>
                    <xsl:apply-templates select="//doc"/>
                </ListRecords>
            </OAI-PMH>
        </harvest>

    </xsl:template>

    <xsl:template match="doc">
        <record>
            <header> </header>
            <metadata>
                <oai_dc:dc xmlns:dc="http://purl.org/dc/elements/1.1/"
                    xmlns:oai_dc="http://www.openarchives.org/OAI/2.0/oai_dc/"
                    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                    xsi:schemaLocation="http://www.openarchives.org/OAI/2.0/oai_dc/        http://www.openarchives.org/OAI/2.0/oai_dc.xsd">
                    
                    <dc:identifier>
                        <xsl:value-of select="field[@name='id']/text()"/>
                    </dc:identifier>
                    
                    <dc:title>
                        <xsl:value-of select="field[@name='title']/text()"/>
                    </dc:title>
                    
                    <xsl:for-each select="field[@name='author']">
                        <dc:creator>
                            <xsl:value-of select="text()"/>
                        </dc:creator>
                    </xsl:for-each>
                    
                    <xsl:for-each select="field[@name='abstract']">
                        <dc:description>
                            <xsl:value-of select="text()"/>
                        </dc:description>
                    </xsl:for-each>
                    
                    <xsl:for-each select="field[@name='source']">
                        <dc:source>
                            <xsl:value-of select="text()"/>
                        </dc:source>
                    </xsl:for-each>
                    
                    <!-- dummy subject since the arXiv doesn't include this -->                  
                    <dc:subject>dummy subject</dc:subject>
                    
                </oai_dc:dc>
            </metadata>
        </record>

    </xsl:template>

</xsl:stylesheet>
