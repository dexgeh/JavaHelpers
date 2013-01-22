package helpers.xml;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

public class XMLTransform {
	public static Transformer getTransformer(String xslFileName) throws TransformerConfigurationException, TransformerFactoryConfigurationError {
		return TransformerFactory
			.newInstance()
			.newTransformer(
					new StreamSource(new File(xslFileName)));
	}
	public static Transformer getTransformer(InputStream xslIS) throws TransformerConfigurationException, TransformerFactoryConfigurationError {
		return TransformerFactory
			.newInstance()
			.newTransformer(
					new StreamSource(xslIS));
	}
	public static void spacesNormalizer(InputStream is, OutputStream os) throws TransformerConfigurationException, TransformerException, TransformerFactoryConfigurationError {
		getTransformer(
				XMLTransform.class.getResourceAsStream(
						"/helpers/xml/SpacesNormalizer.xsl"))
				.transform(new StreamSource(is), new StreamResult(os));
	}
	public static void namespaceRemover(InputStream is, OutputStream os) throws TransformerConfigurationException, TransformerException, TransformerFactoryConfigurationError {
		getTransformer(
				XMLTransform.class.getResourceAsStream(
						"/helpers/xml/NamespaceRemover.xsl"))
				.transform(new StreamSource(is), new StreamResult(os));
	}
	
	public static void cleanup(InputStream is, OutputStream os) throws TransformerConfigurationException, TransformerException, TransformerFactoryConfigurationError {
		getTransformer(
				XMLTransform.class.getResourceAsStream(
						"/helpers/xml/Cleanup.xsl"))
				.transform(new StreamSource(is), new StreamResult(os));
	}
}
