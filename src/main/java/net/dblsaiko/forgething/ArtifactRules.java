package net.dblsaiko.forgething;

import com.google.gson.stream.JsonReader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ArtifactRules {

    private static final ArtifactRules EMPTY = new ArtifactRules(Collections.singletonList(new Rule(Action.ALLOW, null)));

    private final List<Rule> rules;

    public ArtifactRules(List<Rule> rules) {
        this.rules = rules;
    }

    public Action getAction(Os os) {
        return rules.stream()
            .filter($ -> $.matches(os))
            .findFirst()
            .map(Rule::getAction)
            .orElse(Action.DISALLOW);
    }

    public static ArtifactRules empty() {
        return EMPTY;
    }

    public static ArtifactRules parse(JsonReader jr) throws IOException {
        List<Rule> list = new ArrayList<>();
        jr.beginArray();
        while (jr.hasNext()) {
            jr.beginObject();
            Action action = null;
            Os os = null;
            while (jr.hasNext()) {
                String name = jr.nextName();
                switch (name) {
                    case "action":
                        String actionId = jr.nextString();
                        switch (actionId) {
                            case "allow":
                                action = Action.ALLOW;
                                break;
                            case "disallow":
                                action = Action.DISALLOW;
                                break;
                            default:
                                throw new IllegalStateException("Unsupported action " + actionId);
                        }
                        break;
                    case "os":
                        jr.beginObject();
                        while (jr.hasNext()) {
                            String s = jr.nextName();
                            if ("name".equals(s)) {
                                String osId = jr.nextString();
                                switch (osId) {
                                    case "osx":
                                        os = Os.MAC;
                                        break;
                                    case "windows":
                                        os = Os.WINDOWS;
                                    default:
                                        throw new IllegalStateException("Unsupported OS " + osId);
                                }
                            } else {
                                throw new IllegalStateException("Unsupported OS filter " + s);
                            }
                        }
                        jr.endObject();
                        break;
                    default:
                        throw new IllegalStateException("Invalid rule param " + name);
                }
            }
            jr.endObject();
            if (action == null) throw new IllegalStateException("Action not set");
            list.add(new Rule(action, os));
        }
        jr.endArray();
        Collections.reverse(list);
        return new ArtifactRules(list);
    }

    public static class Rule {

        private final Action action;
        private final Os os;

        public Rule(Action action, Os os) {
            this.action = action;
            this.os = os;
        }

        public Action getAction() {
            return action;
        }

        public boolean matches(Os os) {
            return this.os == null || this.os == os;
        }

    }

    public enum Action {
        ALLOW,
        DISALLOW,
    }

}
