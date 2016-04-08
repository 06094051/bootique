package com.nhl.bootique.resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

import org.junit.Test;

public class ResourceFactoryTest {

	private static String fileUrl(String path) {
		try {
			return new File(path).toURI().toURL().toExternalForm();
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}

	private static String jarEntryUrl(String jarPath, String entryPath) {
		return String.format("jar:%s!/%s", fileUrl(jarPath), entryPath);
	}

	static String resourceContents(String resourceId) throws IOException {
		URL url = new ResourceFactory(resourceId).getUrl();

		assertNotNull(url);

		try (Scanner scanner = new Scanner(url.openStream(), "UTF-8")) {
			return scanner.useDelimiter("\\Z").nextLine();
		}
	}

	@Test
	public void testResourceAsUrl_File() throws IOException {
		assertEquals("a: b", resourceContents("src/test/resources/com/nhl/bootique/config/test1.yml"));
	}

	@Test
	public void testResourceAsUrl_FileUrl() throws IOException {
		String fileUrl = fileUrl("src/test/resources/com/nhl/bootique/config/test2.yml");
		assertEquals("c: d", resourceContents(fileUrl));
	}

	@Test
	public void testResourceAsUrl_JarUrl() throws IOException {
		String jarUrl = jarEntryUrl("src/test/resources/com/nhl/bootique/config/test3.jar", "com/foo/test3.yml");
		assertEquals("e: f", resourceContents(jarUrl));
	}

	@Test
	public void testResourceAsUrl_ClasspathUrl() throws IOException {
		String cpUrl = "classpath:com/nhl/bootique/config/test2.yml";
		assertEquals("c: d", resourceContents(cpUrl));
	}

	@Test(expected = RuntimeException.class)
	public void testResourceAsUrl_ClasspathUrlWithSlash() throws IOException {
		String cpUrl = "classpath:/com/nhl/bootique/config/test2.yml";
		resourceContents(cpUrl);
	}
}
