package net.dblsaiko.forgething.mcpconfig.task;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

public interface Task {

    Path execute(Path output, Map<String, Path> prevTaskOutputs) throws IOException;

    default Set<String> getDependencies() {
        return Collections.emptySet();
    }

}
