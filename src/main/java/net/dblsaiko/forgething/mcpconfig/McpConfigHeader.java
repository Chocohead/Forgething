package net.dblsaiko.forgething.mcpconfig;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class McpConfigHeader {

    private final String gameVersion;

    private final JsonObject data;

    private final Set<String> clientLibraries;
    private final Set<String> serverLibraries;
    private final Set<String> joinedLibraries;

    public McpConfigHeader(String gameVersion, JsonObject data, Set<String> clientLibraries, Set<String> serverLibraries, Set<String> joinedLibraries) {
        this.gameVersion = gameVersion;
        this.data = data;
        this.clientLibraries = clientLibraries;
        this.serverLibraries = serverLibraries;
        this.joinedLibraries = joinedLibraries;
    }

    public String getGameVersion() {
        return gameVersion;
    }

    public JsonObject getData() {
        return data;
    }

    public Set<String> getClientLibraries() {
        return clientLibraries;
    }

    public Set<String> getServerLibraries() {
        return serverLibraries;
    }

    public Set<String> getJoinedLibraries() {
        return joinedLibraries;
    }

    public static McpConfigHeader from(JsonObject root) {
        int spec = root.get("spec").getAsInt();
        if (spec != 1) throw new IllegalStateException("file format version must be 1");
        String gameVersion = root.get("version").getAsString();
        JsonObject data = root.getAsJsonObject("data");
        Set<String> clientLibraries = Collections.emptySet();
        Set<String> serverLibraries = Collections.emptySet();
        Set<String> joinedLibraries = Collections.emptySet();

        if (root.has("libraries")) {
            JsonObject libraries = root.getAsJsonObject("libraries");
            if (libraries.has("client")) {
                clientLibraries = StreamSupport.stream(libraries.getAsJsonArray("client").spliterator(), true)
                    .map(JsonElement::getAsString)
                    .collect(Collectors.toSet());
            }
            if (libraries.has("server")) {
                serverLibraries = StreamSupport.stream(libraries.getAsJsonArray("server").spliterator(), true)
                    .map(JsonElement::getAsString)
                    .collect(Collectors.toSet());
            }
            if (libraries.has("joined")) {
                joinedLibraries = StreamSupport.stream(libraries.getAsJsonArray("joined").spliterator(), true)
                    .map(JsonElement::getAsString)
                    .collect(Collectors.toSet());
            }
        }
        return new McpConfigHeader(gameVersion, data, clientLibraries, serverLibraries, joinedLibraries);
    }

}
