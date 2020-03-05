package net.dblsaiko.forgething;

import java.net.URL;

public class MavenArtifactPath {

    private final String group;
    private final String name;
    private final String version;
    private final String classifier;
    private final String extension;

    private MavenArtifactPath(String group, String name, String version, String classifier, String extension) {
        this.group = group;
        this.name = name;
        this.version = version;
        this.classifier = classifier;
        this.extension = extension;
    }

    public String getDir() {
        return String.format("%s/%s/%s", group.replace('.', '/'), name, version);
    }

    public String getBaseFileName() {
        if (classifier.isEmpty()) {
            return String.format("%s-%s", name, version);
        } else {
            return String.format("%s-%s-%s", name, version, classifier);
        }
    }

    public String getPom() {
        return String.format("%s/%s-%s.pom", getDir(), name, version);
    }

    public String getArtifact() {
        return String.format("%s/%s.%s", getDir(), getBaseFileName(), extension);
    }

    public URL getUrlIn(String repo) {
        String url;
        if (repo.endsWith("/")) {
            url = String.format("%s%s", repo, getArtifact());
        } else {
            url = String.format("%s/%s", repo, getArtifact());
        }
        return Urls.get(url);
    }

    public static MavenArtifactPath of(String group, String name, String version) {
        return MavenArtifactPath.of(group, name, version, "");
    }

    public static MavenArtifactPath of(String group, String name, String version, String classifier) {
        return new MavenArtifactPath(group, name, version, classifier, "jar");
    }

    public static MavenArtifactPath of(String group, String name, String version, String classifier, String extension) {
        return new MavenArtifactPath(group, name, version, classifier, extension);
    }

    public static MavenArtifactPath from(String spec) {
        String extension = "jar";
        String[] split = spec.split(":", 4);
        {
            int idx;
            String last = split[split.length - 1];
            if ((idx = last.indexOf("@")) != -1) {
                extension = last.substring(idx + 1);
                split[split.length - 1] = last.substring(0, idx);
            }
        }
        if (split.length < 3) throw new IllegalArgumentException(String.format("invalid spec: '%s'", spec));
        if (split.length == 3) {
            return MavenArtifactPath.of(split[0], split[1], split[2], "", extension);
        } else {
            return MavenArtifactPath.of(split[0], split[1], split[2], split[3], extension);
        }
    }

}
