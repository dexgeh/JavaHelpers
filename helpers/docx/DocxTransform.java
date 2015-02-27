package helpers.docx;

import helpers.io.IO;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

public class DocxTransform {
	private static int debugLevel = 2;
	private static void debug(int n, String fmt, Object ...data) {
		if (n <= debugLevel) {
			System.out.println(
				String.format("[%s] %" + n + "s %s"
					, new Date().toString(), ""
					, data.length == 0 ? fmt : String.format(fmt, data)));
		}
	}
	private static XPathExpression xpath(String expr) throws XPathExpressionException {
		return XPathFactory.newInstance().newXPath().compile(expr);
	}
	private static Document getDocument(byte[] content) throws ParserConfigurationException, SAXException, IOException {
		return
			DocumentBuilderFactory
				.newInstance()
				.newDocumentBuilder()
				.parse(new ByteArrayInputStream(content));
	}
	private static byte[] replaceDocPropertyValuesInCustomXml(byte[] content, Map<String,String> fieldValues) throws Exception {
		debug(1, "replaceDocPropertyValuesInCustomXml()");
		Document document = getDocument(content);
		NodeList properties = (NodeList) xpath("/Properties/property").evaluate(document, XPathConstants.NODESET);
		int index = properties.getLength();
		while (index --> 0) {
			Element property = (Element) properties.item(index);
			String propName = property.getAttribute("name");
			debug(2, "propName = %s", propName);
			if (fieldValues.containsKey(propName)) {
				debug(2, "!!! replacing text");
				Text textNode = (Text) xpath("./lpwstr/text()").evaluate(property, XPathConstants.NODE);
				textNode.replaceWholeText(fieldValues.get(propName));
			}
		}
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		TransformerFactory.newInstance().newTransformer().transform(new DOMSource(document), new StreamResult(out));
		return out.toByteArray();
	}
	private static Pattern instrTextDocpPattern = Pattern.compile("^[ ]+DOCPROPERTY[ ]+\"?(.+?)\"?[ ]+\\\\\\*[ ]+MERGEFORMAT[ ]+$");
	private static byte[] replaceDocPropertyValuesInDocument(byte[] content, Map<String,String> fieldValues) throws Exception {
		debug(1, "replaceDocPropertyValuesInDocument()");
		Document document = getDocument(content);
		NodeList instrTexts = (NodeList) xpath("//r/instrText/text()").evaluate(document, XPathConstants.NODESET);
		int index = instrTexts.getLength();
		while (index --> 0) {
			Text instrText = (Text) instrTexts.item(index);
			debug(2, "instrText = %s", instrText.getData());
			Matcher instrTextDocpMatcher = instrTextDocpPattern.matcher(instrText.getData());
			if (instrTextDocpMatcher.find()) {
				debug(2, "pattern matched");
				String propName = instrTextDocpMatcher.group(1);
				if (fieldValues.containsKey(propName)) {
					debug(2, "!!! replacing text");
					Element r = (Element) instrText.getParentNode().getParentNode();
					XPathExpression textNodeFinder = xpath("../r/t/text()");
					Text textNode = (Text) textNodeFinder.evaluate(r, XPathConstants.NODE);
					textNode.replaceWholeText(fieldValues.get(propName));
				}
			}
		}
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		TransformerFactory.newInstance().newTransformer().transform(new DOMSource(document), new StreamResult(out));
		return out.toByteArray();
	}
	public static byte[] replaceDocProperties(byte[] docxFile, Map<String,String> fieldValues) throws Exception {
		debug(1, "replaceDocProperties(%s)", fieldValues.toString());
		ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(docxFile));
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ZipOutputStream zos = new ZipOutputStream(out);
		ZipEntry entry;
		while ((entry= zis.getNextEntry()) != null) {
			byte[] content = IO.read(zis);
			if (entry.getName().equals("docProps/custom.xml")) {
				content = replaceDocPropertyValuesInCustomXml(content, fieldValues);
			} else if (entry.getName().equals("word/document.xml") 
					|| entry.getName().startsWith("word/header")
					|| entry.getName().startsWith("word/footer")) {
				debug(1, "found %s", entry.getName());
				content = replaceDocPropertyValuesInDocument(content, fieldValues);
			}
			ZipEntry newEntry = new ZipEntry(entry.getName());
			zos.putNextEntry(newEntry);
			zos.write(content);
			zos.closeEntry();
		}
		zos.close();
		return out.toByteArray();
	}
}
