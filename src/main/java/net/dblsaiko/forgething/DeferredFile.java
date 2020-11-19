package net.dblsaiko.forgething;

import java.nio.file.Path;
import java.util.Optional;

public interface DeferredFile {
	Optional<Path> get();
}
