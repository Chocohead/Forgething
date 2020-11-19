package net.dblsaiko.forgething;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

public class Utils {
	public static void closeQuietly(Throwable t, Closeable thing) {
		if (thing != null) {
			try {
				thing.close();
			} catch (IOException e) {
				t.addSuppressed(e);
			}
		}
	}

	public static <T> List<T> map(JsonArray array, Function<JsonElement, T> mapper) {
		List<T> out = new ArrayList<>(array.size());

		for (JsonElement element : array) {
			out.add(mapper.apply(element));
		}

		return out;
	}
}