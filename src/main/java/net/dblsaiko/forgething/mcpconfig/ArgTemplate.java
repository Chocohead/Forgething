package net.dblsaiko.forgething.mcpconfig;

import net.dblsaiko.forgething.Main;

import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public interface ArgTemplate {

    default ArgTemplate resolveVariables(McpConfigHeader header, Function<String, ArgTemplate> vars) {
        return this;
    }

    default ArgTemplate resolveTaskOutput(Map<String, Path> prevTaskOutputs) {
        return this;
    }

    default ArgTemplate resolveSelfOutput(Path outputFile) {
        return this;
    }

    default Set<String> taskDependencies() {
        return Collections.emptySet();
    }

    default String unwrap() {
        throw new IllegalStateException(String.format("Can't unwrap '%s'. Used in wrong place or not resolved yet?", this));
    }

    static ArgTemplate parse(String str) {
        if ("{log}".equals(str)) {
            return LogFile.INSTANCE;
        } else if ("{output}".equals(str)) {
            return SelfOutput.INSTANCE;
        } else if (str.startsWith("{") && str.endsWith("Output}")) {
            return new TaskOutput(str.substring(1, str.length() - 7));
        } else if (str.startsWith("{") && str.endsWith("}")) {
            return new Variable(str.substring(1, str.length() - 1));
        } else {
            return new Literal(str);
        }
    }

    class Literal implements ArgTemplate {

        private final String text;

        public Literal(String text) {
            this.text = text;
        }

        @Override
        public String unwrap() {
            return text;
        }

    }

    class Variable implements ArgTemplate {

        private final String var;

        public Variable(String var) {
            this.var = var;
        }

        @Override
        public ArgTemplate resolveVariables(McpConfigHeader header, Function<String, ArgTemplate> vars) {
            ArgTemplate param = vars.apply(var);
            if (param != null) {
                return param;
            }
            if (header.getData().has(var)) {
                return new Literal(header.getData().get(var).getAsString());
            }
            throw new IllegalStateException("Could not resolve variable '" + var + "'.");
        }
    }

    class TaskOutput implements ArgTemplate {

        private final String task;

        public TaskOutput(String task) {
            this.task = task;
        }

        @Override
        public ArgTemplate resolveTaskOutput(Map<String, Path> prevTaskOutputs) {
            return new Literal(prevTaskOutputs.get(task).toString());
        }

        @Override
        public Set<String> taskDependencies() {
            return Collections.singleton(task);
        }

    }

    class SelfOutput implements ArgTemplate {

        public static final SelfOutput INSTANCE = new SelfOutput();

        @Override
        public ArgTemplate resolveSelfOutput(Path outputFile) {
            return new Literal(outputFile.toString());
        }

    }

    class LogFile implements ArgTemplate {

        public static final LogFile INSTANCE = new LogFile();

        @Override
        public String unwrap() {
            return Main.LOG_FILE.toAbsolutePath().toString();
        }

    }

}