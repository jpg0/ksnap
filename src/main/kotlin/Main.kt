import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.clikt.parameters.types.file
import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.addFileSource
import com.sksamuel.hoplite.addResourceSource
import org.slf4j.LoggerFactory
import java.io.File

data class KSnapConf(
    val snapraidConf: File,
    val snapraidBinary: File,
    val logLevel: String
)

class KSnapMain : CliktCommand() {

    val config by option(help = "ksnap config file").file(canBeDir = false).required()
    val logLevel by option(help = "log level").choice("DEBUG", "INFO", "WARN", "ERROR")

    override fun run() {
        val config = ConfigLoaderBuilder.default()
            .addFileSource(config.absolutePath)
            .build()
            .loadConfigOrThrow<KSnapConf>()

        val cmd = SnapraidCommand(config.snapraidBinary, config.snapraidConf)

        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", logLevel ?: config.logLevel)

        logger().info(cmd.diff().added().toString())
    }
}

fun main(args: Array<String>) = KSnapMain().main(args)