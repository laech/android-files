package l.files.operations;

import java.io.IOException;
import l.files.fs.Path

data class Failure(val path: Path, val cause: IOException)
