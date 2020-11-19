package net.dblsaiko.forgething;

import java.net.MalformedURLException;
import java.net.URL;

public class Urls {
	public static URL get(String path) {
		try {
			return new URL(path);
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}
}
