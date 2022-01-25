import com.influxdb.client.write.Point

suspend fun main(args: Array<String>) {

    val inDir = "C://Users/memingr/projects/Tomas/data"

    val reg =
        Regex("(METAR|METAR COR|SPECI) (\\D{4}) (\\d{2}\\d{2}\\d{2}Z) ((?:VRB|\\d{3})\\d{2}(?:G\\d{2})?KT(?: \\d{3}V\\d{3})?) (.*) (M?\\d{2}\\/M?\\d{2}).*(Q\\d{4}) ?(.*) (RMK.*)")

    val configs = listOf(
        Config("RAW_TEMPERATURE", "t,,_,n,,,n,,n,,n,,n,", inDir),
        Config("REPORTS", "t,_,s,", inDir) { pom ->
            pom.measures.first { it.name == "MESSAGE" }.also {
                reg.find(it.myValue)?.groupValues?.get(6)?.replace("M", "-")?.split("/")?.let {

                    pom.measures.add(Radek.Measure("TEMPERATURE", it[0]))
                    pom.measures.add(Radek.Measure("DEW_POINT", it[1]))
                }
            }
        },
        Config("RAIN_METGARDEN", "t,_,n,n,n,n,n,n,n,n,n,n,n,", inDir),
        Config("ATC_WIND_RWY06", "t,n,,n,,n,,n,,n,,n,,n,,n,,n,,s,,s,,s,", inDir, "SITE=06")
    )

    configs.filter { listOf("RAW_TEMPERATURE", "REPORTS").contains(it.fileNamePrefix) }.forEach {
        processInputDir(it)
    }
}

class Radek {
    var timestamp: Number? = null
    var measures = mutableListOf<Measure>()
    var tags = mutableMapOf<String, String>()

    class Measure(val name: String, val myValue: String)
}

class Config(
    val fileNamePrefix: String,
    val columnDef: String,
    val inputDir: String,
    val tags: String = "",
    val modifier: (Radek) -> Any = {}
) {
}