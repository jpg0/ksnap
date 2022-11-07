import java.io.File
import java.util.concurrent.TimeUnit

class SnapraidCommand(private val binary: File, private val conf: File) {

    fun defaultParams() = listOf<String>(
        "-conf", conf.absolutePath
    )

    private fun run(op: SnapraidOp, params: List<String>): String {
        val proc = ProcessBuilder(listOf(binary.absolutePath) + defaultParams() + op.name + params)
            //.directory("/")
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .redirectError(ProcessBuilder.Redirect.PIPE)
            .start()

        proc.waitFor(60, TimeUnit.MINUTES)
        return proc.inputStream.bufferedReader().readText()
    }

    fun sync() = run(SnapraidOp.sync, emptyList())
}

enum class SnapraidOp {
    sync
}