package ru.hse.cppr.application

import arrow.core.Either
import kotlinx.coroutines.runBlocking
import org.koin.core.KoinProperties
import org.koin.core.parameter.parametersOf
import org.koin.standalone.KoinComponent
import org.koin.standalone.StandAloneContext.startKoin
import picocli.CommandLine
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import ru.hse.cppr.dependencies.*
import ru.hse.cppr.utils.createScope

@Command(name = "cppr-backend", mixinStandardHelpOptions = false, version = [
    "CPPR Backend application 0.1.0 made by:\n" +
    "1. i.komarov (Telegram: @ikomarov)\n" +
    "2. a.kazantceva (Telegram: @xoxo_anastasi_xoxo)\n" +
    "3. m.chernyshenko (Telegram: @blackbird_sings)"
])
internal class ITCommandLineApplication: Runnable, KoinComponent {

    @Option(names = ["-h", "--help"],
        usageHelp = true,
        description = [ "Print the CLI interface commands" ])
    private var help: Boolean = false

    @Option(names = [ "-v", "--version" ],
        versionHelp = true,
        description = [ "Print the CPPR Backend version" ])
    private var version: Boolean = false

    @Option(names = [ "-p", "--props" ],
        arity = "1",
        required = true,
        paramLabel = "PROPERTIES",
        description = [
            "Path to directory on the local machine that contains all .properties files used in the application. "
        ])
    private var propertiesDir: String = ""

    override fun run() {
        if (help) {
            CommandLine(this).usage(System.out)
            return
        }

        if (version) {
            CommandLine(this).printVersionHelp(System.out)
            return
        }

        startKoin(
            list = listOf(
                applicationModule,
                effectsModule    ,
                cachesModule     ,
                clientsModule    ,
                databaseModule
            ),
            properties = KoinProperties(
                useEnvironmentProperties = false,
                useKoinPropertiesFile = true,
                extraProperties = emptyMap()
            )
        )

        createScope(ApplicationScope::class) {
            parametersOf(propertiesDir)
        }

        Runtime.getRuntime().addShutdownHook(Thread {
            runBlocking {
                Either.catch { getKoin().close() }
            }
        })

        ITCommandLineApplicationRuntime().run()
    }
}