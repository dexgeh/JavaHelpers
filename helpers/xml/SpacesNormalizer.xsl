<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
	<xsl:output method="xml" omit-xml-declaration="no" indent="yes" />
	<xsl:strip-space elements="*" />
	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:choose>
				<xsl:when test="text()">
					<xsl:value-of select="normalize-space(.)" />
				</xsl:when>
				<xsl:otherwise>
					<xsl:apply-templates select="@*|node()" />
				</xsl:otherwise>
			</xsl:choose>
		</xsl:copy>
	</xsl:template>
</xsl:stylesheet>
