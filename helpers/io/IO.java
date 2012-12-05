package helpers.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;
import java.net.URL;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

public class IO {
	public static int BUFSIZE = 4096;
	// generic streams
	public static void copy(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[BUFSIZE];
		int len;
		while ((len = in.read(buffer)) != -1)
			out.write(buffer, 0, len);
	}
	public static void copy(Reader reader, Writer writer) throws IOException {
		char[] buffer = new char[BUFSIZE];
		int len;
		while ((len = reader.read(buffer)) != -1)
			writer.write(buffer, 0, len);
	}
	// files
	public static byte[] readFile(String fileName) throws IOException {
		FileInputStream fis = new FileInputStream(fileName);
		ByteArrayOutputStream baos = new ByteArrayOutputStream((int) new File(fileName).length());
		copy(fis, baos);
		fis.close();
		return baos.toByteArray();
	}
	public static String readFileAsString(String fileName, String charset) throws IOException {
		return new String(readFile(fileName), charset);
	}
	private static final String UTF8 = "UTF-8";
	public static String readFileAsUTF8String(String fileName) throws IOException {
		return new String(readFile(fileName), UTF8);
	}
	public static byte[] readResource(ClassLoader cl, String resource) throws IOException {
		InputStream is = cl.getResourceAsStream(resource);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		copy(is, baos);
		is.close();
		return baos.toByteArray();
	}
	public static void writeFile(String fileName, byte[] data) throws IOException {
		ByteArrayInputStream bais = new ByteArrayInputStream(data);
		FileOutputStream fos = new FileOutputStream(fileName);
		copy(bais, fos);
		fos.close();
		bais.close();
	}
	// urls
	private static HostnameVerifier disabledHostnameVerifier = new HostnameVerifier() {
		public boolean verify(String hostname, SSLSession session) {
			return true;
		}
	};
	private static final String https = "https://";
	public static byte[] readURL(String url) throws IOException {
		if (url.startsWith(https)) {
			HttpsURLConnection.setDefaultHostnameVerifier(disabledHostnameVerifier);
		}
		InputStream in = new URL(url).openStream();
		ByteArrayOutputStream baos = new ByteArrayOutputStream(BUFSIZE);
		copy(in, baos);
		in.close();
		return baos.toByteArray();
	}
	// files and urls
	public static void writeURLtoFile(String url, String fileName) throws IOException {
		InputStream in = new URL(url).openStream();
		FileOutputStream fos = new FileOutputStream(fileName);
		copy(in, fos);
		in.close();
		fos.close();
	}
	// objects
	public static void writeObject(Serializable o, OutputStream os) throws IOException {
		ObjectOutputStream oos = new ObjectOutputStream(os);
		oos.writeObject(o);
	}
	public static Serializable readObject(InputStream is) throws IOException, ClassNotFoundException {
		ObjectInputStream ois = new ObjectInputStream(is);
		return (Serializable) ois.readObject();
	}
	public static byte[] serialize(Serializable object) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream(BUFSIZE);
		writeObject(object, baos);
		return baos.toByteArray();
	}
	public static Serializable deserialize(byte[] data) throws IOException, ClassNotFoundException {
		ByteArrayInputStream bais = new ByteArrayInputStream(data);
		return readObject(bais);
	}
	// utils
	public static byte[] read(InputStream is) throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream(BUFSIZE);
		copy(is, baos);
		return baos.toByteArray();
	}
	public static byte[] read(Reader reader) throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream(BUFSIZE);
		OutputStreamWriter writer = new OutputStreamWriter(baos);
		copy(reader, writer);
		writer.flush();
		return baos.toByteArray();
	}
}
