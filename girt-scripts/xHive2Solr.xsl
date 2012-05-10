<?xml version="1.0" encoding="UTF-8"?>
<!-- created by sc on 13-05-2009 -->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"
        cdata-section-elements="Entry"/>
    <xsl:template match="/">
        <add>
            <xsl:for-each select="//Entry/*">
                <doc>
                    <!-- Title -->
                    <xsl:for-each select="Title[@authority='sowiport']">
                        <xsl:choose>
                            <xsl:when test="@language='de'">
                                <field name="title_de">
                                    <xsl:value-of select="@text"/>
                                </field>        
                            </xsl:when>
                            <xsl:when test="@language='en'">
                                <field name="title_en">
                                    <xsl:value-of select="@text"/>
                                </field>        
                            </xsl:when>
                            <xsl:otherwise>
                                <field name="title">
                                    <xsl:value-of select="@text"/>
                                </field>
                            </xsl:otherwise>
                        </xsl:choose>                                            
                    </xsl:for-each>
                    
                    <!-- Abstract -->
                    <xsl:for-each select="Description[@role='abstract']">
                        <xsl:choose>
                            <xsl:when test="@language='de'">
                                <field name="abstract_de">
                                    <xsl:value-of select="@text"/>
                                </field>        
                            </xsl:when>
                            <xsl:when test="@language='en'">
                                <field name="abstract_en">
                                    <xsl:value-of select="@text"/>
                                </field>        
                            </xsl:when>
                            <xsl:otherwise>
                                <field name="abstract">
                                    <xsl:value-of select="@text"/>
                                </field>
                            </xsl:otherwise>
                        </xsl:choose>                                            
                    </xsl:for-each>
                    
                    <!-- Subject -->
                    <xsl:for-each select="Subject[@authority='native' or @authority='thesoz']">
                        <xsl:choose>
                            <xsl:when test="@language='de'">
                                <field name="subject_de">
                                    <xsl:value-of select="@text"/>
                                </field>        
                            </xsl:when>
                            <xsl:when test="@language='en'">
                                <field name="subject_en">
                                    <xsl:value-of select="@text"/>
                                </field>        
                            </xsl:when>
                            <xsl:otherwise>
                                <field name="subject">
                                    <xsl:value-of select="@text"/>
                                </field>
                            </xsl:otherwise>
                        </xsl:choose>                                            
                    </xsl:for-each>
                    
                    <xsl:for-each select="Subject[@authority='none']">
                        <field name="freekeyword">
                            <xsl:value-of select="@text"/>
                        </field>
                    </xsl:for-each>

					<!-- Method -->
				   <xsl:for-each select="Method[@authority='thesoz']">
                        <field name="method">
                            <xsl:value-of select="@text"/>
                        </field>
                    </xsl:for-each>
                    
                    <!-- Classification -->
                    <xsl:for-each select="Classification">
                        <xsl:choose>
                            <xsl:when test="@language='de'">
                                <field name="classification_de">
                                    <xsl:value-of select="@text"/>
                                </field>        
                            </xsl:when>
                            <xsl:when test="@language='en'">
                                <field name="classification_en">
                                    <xsl:value-of select="@text"/>
                                </field>        
                            </xsl:when>
                            <xsl:otherwise>
                                <field name="classification">
                                    <xsl:value-of select="@text"/>
                                </field>
                            </xsl:otherwise>
                        </xsl:choose>                                            
                    </xsl:for-each>

                    <!-- Classification Code -->
                    <xsl:for-each select="Classification[@authority='thesoz']">	                    
	                    <field name="classification_code">
							<xsl:value-of select="@id"/>
					    </field>                                                            
                    </xsl:for-each>

                    <!-- ID -->
                    <field name="id">
                        <xsl:value-of select="@id"/>
                    </field>

                    <!-- AcquisitionID - Used for GIRT DOCID generation -->
                    <field name="acquisition_id">
                        <xsl:value-of select="Identifier[@origin='reffull_recordinfo_acquisitionnumber'][@role='acquisition']/@text"/>
                    </field>
					                    
                    <!-- Language -->
                    <field name="language">
                        <xsl:value-of select="Language[@authority='sowiport']/@text"/>
                    </field>

                    <!-- Collection -->
                    <field name="collection">
                        <xsl:value-of select="Metadata/Collection/@text"/>
                    </field>

                    <!-- Doctype -->
                    <field name="doctype">
                        <xsl:value-of select="Type[@authority='sowiport'][@role='doctype']/@text"/>
                    </field>

                    <!-- Pubyear -->
                    <field name="pubyear">
                        <xsl:value-of select="Time[@authority='sowiport'][@role='issued']/@text"/>
                    </field>

                    <!-- ISSN -->
                    <xsl:for-each select="Identifier[@role='issn']">
                        <field name="issn">
                            <xsl:value-of select="@text"/>
                        </field>
                    </xsl:for-each>
                    
                    <!-- ISBN -->
                    <xsl:for-each select="Identifier[@role='isbn']">
                        <field name="isbn">
                            <xsl:value-of select="@text"/>
                        </field>
                    </xsl:for-each>
                    
                    <!-- Source -->
                    <xsl:for-each select="Host">
                        <field name="source">
                            <xsl:value-of select="@text | Publication/Title/@text"/>
                        </field>
                    </xsl:for-each>

                    <!-- Source_volume -->
                    <xsl:for-each select="Host">
                        <field name="source_volume">
                            <xsl:value-of select="Volume/@text"/>
                        </field>
                    </xsl:for-each>
                    
                    <!-- issue -->
                    <xsl:for-each select="Host">
                        <field name="source_issue">
                            <xsl:value-of select="Issue/@text"/>
                        </field>
                    </xsl:for-each>
                    
                    <!-- Creator -->
                    <xsl:for-each select="Person[@role='author']">
                        <field name="author">
                            <xsl:value-of select="@text"/>
                        </field>
                    </xsl:for-each>

                    <!-- Editor -->
                    <!-- Auch noch im Host suchen? -->
                    <xsl:for-each select="Person[@role='editor']">
                        <field name="editor">
                            <xsl:value-of select="@text"/>
                        </field>
                    </xsl:for-each>

                    <!-- Location -->
                    <xsl:for-each select="Location">
                        <field name="location">
                            <xsl:value-of select="@text | Country/@text"/>
                        </field>
                    </xsl:for-each>                                                                 
                    
					<!-- Method -->
					<xsl:for-each select="Method">
                        <field name="method">
                            <xsl:value-of select="@text"/>
                        </field>
                    </xsl:for-each>
                    
                    <!-- pages -->
                    <field name="pages">
                        <xsl:value-of select="Format[@role='pages']"/>
                    </field>

                    <!-- Publisher -->
                    <field name="publisher">
                        <xsl:value-of select="Institution[@origin='furtherindications_publisher'][@role='publisher']/@text"/>
                    </field>


                </doc>
            </xsl:for-each>
        </add>
    </xsl:template>
</xsl:stylesheet>
