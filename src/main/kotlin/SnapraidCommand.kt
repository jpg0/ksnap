import java.io.File
import java.io.Reader
import java.rmi.UnexpectedException
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

class SnapraidCommand(private val binary: File, private val conf: File) {

    fun defaultParams() = listOf<String>(
        "-c", conf.absolutePath
    )

    private fun <T> run(op: SnapraidOp, params: List<String>, callback: (Reader, Int) -> T): T {

        val cmdline = listOf(binary.absolutePath) + defaultParams() + op.name + params

        logger().debug("Running ${cmdline.joinToString(" ")}")

        val proc = ProcessBuilder(cmdline)
            //.directory("/")
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .redirectError(ProcessBuilder.Redirect.PIPE)
            .start()

        if (!proc.waitFor(60, TimeUnit.MINUTES)) {
            throw TimeoutException("Snapraid has not returned after timeout")
        }

        if(proc.exitValue() == 1) {
            logger().warn("Snapraid command returned non-zero (1) exit code!")
            logger().warn(proc.inputStream.bufferedReader().readLines().joinToString(separator = "\n"))
            
            throw UnexpectedException("Snapraid command failed")
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