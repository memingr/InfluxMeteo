import com.influxdb.client.domain.WritePrecision
import com.influxdb.client.kotlin.InfluxDBClientKotlin
import com.influxdb.client.kotlin.InfluxDBClientKotlinFactory
import java.io.File
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

fun readHeader(s: String) = s.split("\t").map { it.replace(" ", "\\ ").replace(",", "\\,").replace("=", "\\=") }


fun getClient():InfluxDBClientKotlin {
    val token = System.getenv("INFLUX_TOKEN")
    val url = System.getenv("INFLUX_URL")
    return InfluxDBClientKotlinFactory.create(url, token.toCharArray(), "Letadlo")
}

suspend fun processInputDir(config: Config) {

    val client = getClient()

    File(config.inputDir).walkTopDown()
        .filter { it.name.startsWith(config.fileNamePrefix) }
        .forEach { fl ->
            println(fl)
            convertHisFile(fl, client, config)
        }
}

suspend fun convertHisFile(
    fl: File,
    client: InfluxDBClientKotlin,
    config: Config
) {
    val cldef = config.columnDef.split(",")
    var columns = emptyList<String>()
    val lines = emptyList<String>().toMutableList()
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneOffset.UTC)

    val writeApi = client.getWriteKotlinApi()

    fl.useLines {
        it.drop(1).forEachIndexed { lineNum, line ->
            if (lineNum == 0) {
                columns = readHeader(line)
            } else {
                val pom = Radek()
                line.split("\t").forEachIndexed { index, s ->
                    val columnName = columns[index]
                    when (cldef[index]) {

                        "t" -> pom.timestamp =
                            LocalDateTime.parse(s, formatter).toEpochSecond(ZoneOffset.UTC) * 1_000_000_000
                        "_" -> pom.tags[columnName] = s
                        "n" -> pom.measures.add(Radek.Measure(columnName, s))
                        "s" -> pom.measures.add(Radek.Measure(columnName, "\"$s\""))
                    }
                }

                config.modifier(pom)
                val tagsStr =
                    if (pom.tags.isEmpty()) "" else pom.tags.map { k -> "${k.key}=${k.value}" }.joinToString(",", ",")

                val tagsFinal = if (config.tags.isBlank()) tagsStr else "$tagsStr,${config.tags}"
                val fieldStr = pom.measures
                    .filter { it.myValue.isNotBlank() }
                    .joinToString(",") { k -> "${k.name}=${k.myValue}" }

                if (fieldStr.isNotBlank()) lines.add("${config.fileNamePrefix}$tagsFinal $fieldStr ${pom.timestamp}")
            }
        }
    }
    lines.windowed(5000, 5000, true).forEach {
        writeApi.writeRecords(it, WritePrecision.NS, "l3", "Letadlo")
    }
}
