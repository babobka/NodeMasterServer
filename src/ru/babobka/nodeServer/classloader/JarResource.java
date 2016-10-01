package ru.babobka.nodeServer.classloader;

/**
 * Created by dolgopolov.a on 12.12.15.
 */

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 * JarResources: JarResources maps all resources included in a Zip or Jar file.
 * Additionaly, it provides a method to extract one as a blob.
 */
final class JarResource {

	// jar resource mapping tables
	private Map<String, Integer> htSizes = new HashMap<>();
	private Map<String, byte[]> htJarContents = new HashMap<>();

	// a jar file
	private String jarFileName;

	/**
	 * creates a JarResources. It extracts all resources from a Jar into an
	 * internal hashtable, keyed by resource names.
	 * 
	 * @param jarFileName
	 *            a jar or zip file
	 */
	public JarResource(String jarFileName) {
		this.jarFileName = jarFileName;
		init();
	}

	/**
	 * Extracts a jar resource as a blob.
	 * 
	 * @param name
	 *            a resource name.
	 */
	public byte[] getResource(String name) {
		return (byte[]) htJarContents.get(name);
	}

	/**
	 * initializes internal hash tables with Jar file resources.
	 */
	private void init() {
		ZipInputStream zis = null;
		FileInputStream fis = null;
		BufferedInputStream bis = null;
		ZipFile zf = null;
		try {
			// extracts just sizes only.
			zf = new ZipFile(jarFileName);
			Enumeration<?> e = zf.entries();
			while (e.hasMoreElements()) {
				ZipEntry ze = (ZipEntry) e.nextElement();

				htSizes.put(ze.getName(), (int) ze.getSize());
			}

			// extract resources and put them into the hashtable.
			fis = new FileInputStream(jarFileName);
			bis = new BufferedInputStream(fis);
			zis = new ZipInputStream(bis);
			ZipEntry ze = null;
			while ((ze = zis.getNextEntry()) != null) {
				if (ze.isDirectory()) {
					continue;
				}

				int size = (int) ze.getSize();
				// -1 means unknown size.
				if (size == -1) {
					size = htSizes.get(ze.getName()).intValue();
				}

				byte[] b = new byte[(int) size];
				int rb = 0;
				int chunk = 0;
				while ((size - rb) > 0) {
					chunk = zis.read(b, rb, size - rb);
					if (chunk == -1) {
						break;
					}
					rb += chunk;
				}

				// add to internal resource hashtable
				htJarContents.put(ze.getName(), b);

			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (zf != null) {
				try {
					zf.close();
				} catch (IOException e) {

					e.printStackTrace();
				}
			}
			if (zis != null) {
				try {
					zis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (bis != null) {
				try {
					bis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}
	}

}