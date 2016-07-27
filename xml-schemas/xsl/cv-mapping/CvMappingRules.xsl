<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

	<xsl:template match="CvMappingRuleList">
	  <html>
	  	<head>
	  		<title>CV mapping</title>
	  	</head>
	  	<body>
		  	<p>
		  		<xsl:apply-templates select="CvMappingRule"/>
	  		</p>
	  	</body> 
	  </html>  
	</xsl:template>
	
	<xsl:template match="CvMappingRule">
	   			<h2>
	   				<xsl:value-of select="@id" />
	   			</h2>
				<ul>
					<li>Path: &#160;&#160;&#160;<xsl:value-of select="@cvElementPath"/></li>
					<li>Scope: <xsl:value-of select="@scopePath"/></li>
					<li>Requirement: <xsl:value-of select="@requirementLevel"/></li>
					<li>Boolean logic: <xsl:value-of select="@cvTermsCombinationLogic"/></li>
					<xsl:choose>
						<xsl:when test="@name">
							<li>Name: <xsl:value-of select="@name"/></li>
						</xsl:when>
						<xsl:otherwise>
						</xsl:otherwise>
					</xsl:choose>
				</ul>			
		  		
		  			
		<h3><i>Allowed terms:</i></h3>
		<table border="1" cellpadding="2" width="100%" style="border-style:solid; border-collapse:collapse;">
			<tr>
				<th>CV ID</th>
				<th>Accession</th>
				<th>Preferred name</th>
				<th>Specified term allowed?</th>
				<th>Child terms allowed?</th>
				<th>Allow more than one term?</th>
				<th>Use accession or preferred name?</th>
			</tr>
			<xsl:apply-templates select="CvTerm"/>
		</table>
	   	<br /> <br />
		<hr />
		<br /> <br />
	</xsl:template>

	<xsl:template match="CvTerm">
	  <tr align="center">
	  	<td align="left"><xsl:value-of select="@cvIdentifierRef"/></td>
	  	<td align="left"><xsl:value-of select="@termAccession"/></td>
	  	<td align="left"><xsl:value-of select="@termName"/></td>
	  	
	  	<xsl:choose>
			  <xsl:when test="@useTerm='true'">
			  	<td><font color="blue">yes</font></td>
			  </xsl:when>
	  		<xsl:when test="@useTerm='false'">
			  	<td><font color="red">no</font></td>
	  		</xsl:when>
	  	</xsl:choose>
	  	
	  	<xsl:choose>
	  		<xsl:when test="@allowChildren='true'">
	  			<td><font color="blue">yes</font></td>
	  		</xsl:when>
	  		<xsl:when test="@allowChildren='false'">
	  			<td><font color="red">no</font></td>
	  		</xsl:when>
	  	</xsl:choose>
	  	
	  	<xsl:choose>
	  		<xsl:when test="@isRepeatable='true'">
	  			<td><font color="blue">yes</font></td>
	  		</xsl:when>
	  		<xsl:when test="@isRepeatable='false'">
	  			<td><font color="red">no</font></td>
	  		</xsl:when>
	  		<xsl:otherwise>
	  			<td><font color="red"><xsl:value-of select="@isRepeatable"/></font></td>
	  		</xsl:otherwise>
	  	</xsl:choose>
	  	
	  	<xsl:choose>
			  <xsl:when test="@useTermName='true'">
			  	<td>preferred name</td>
			  </xsl:when>
			  <xsl:otherwise>
			  	<td>accession number</td>
			  </xsl:otherwise>
			</xsl:choose>
	  </tr>
	</xsl:template>

</xsl:stylesheet>
