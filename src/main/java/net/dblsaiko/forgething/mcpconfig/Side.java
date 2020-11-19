package net.dblsaiko.forgething.mcpconfig;

import java.util.Locale;

import com.google.gson.annotations.SerializedName;

public enum Side {
	@SerializedName("client")
	CLIENT,
	@SerializedName("server")
	SERVER,
	@SerializedName("joined")
	JOINED;

	public String getName() {
		return name().toLowerCase(Locale.ENGLISH);
	}
}