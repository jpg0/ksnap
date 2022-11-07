import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.prompt
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.clikt.parameters.types.int

class KSnapMain : CliktCommand() {
    val snapraidConf by option(help="snapraid config file").file(canBeDir = false).required()
    val snapraidBinary by option(help="snapraid binary executable").file(canBeDir = false).required()
    val logLevel by option(help="logging level by name").choice("md5", "sha1")

    override fun run() {
        val cmd = SnapraidCommand(snapraidBinary, snapraidConf)

        logger().info(cmd.sync())
    }
}

fun main(args: Array<String>) = KSnapMain().main(args)