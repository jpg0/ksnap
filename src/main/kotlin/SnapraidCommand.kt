import java.io.File
import java.io.OutputStream
import java.io.Reader
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

class SnapraidCommand(private val binary: File, private val conf: File) {

    fun defaultParams() = listOf<String>(
        "-conf", conf.absolutePath
    )

    private fun <T> run(op: SnapraidOp, params: List<String>, callback: (Reader, Int) -> T): T {
        val proc = ProcessBuilder(listOf(binary.absolutePath) + defaultParams() + op.name + params)
            //.directory("/")
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .redirectError(ProcessBuilder.Redirect.PIPE)
            .start()

        if (!proc.waitFor(60, TimeUnit.MINUTES)) {
            throw TimeoutException("Snapraid has not returned after timeout")
        }

        return callback(proc.inputStream.bufferedReader(), proc.exitValue())
    }

//    fun sync() = run(SnapraidOp.sync, emptyList())

    fun diff() = run(SnapraidOp.diff, emptyList()) { r: Reader, _: Int -> DiffResult(r) }
}

class DiffResult(reader: Reader) {
    val opToCount: Map<DiffOp, Int>

    init {
        val map = mutableMapOf(*DiffOp.values().map { it to 0 }.toTypedArray())

        for (line in reader.readLines()) {

            logger().debug("> $line")

            if (line.isBlank()) {
                break
            } else if (
                line.startsWith("Loading state from") || line.startsWith("Comparing...")
            ) {
                continue
            }

            val (opName, path) = line.split(' ', limit = 2)

            map[DiffOp.valueOf(opName)] = map[DiffOp.valueOf(opName)]!! + 1
        }

        opToCount = map.toMap()
    }

    fun added() = opToCount[DiffOp.added]!!
}

enum class DiffOp {
    added
}

enum class SnapraidOp {
    sync, diff
}