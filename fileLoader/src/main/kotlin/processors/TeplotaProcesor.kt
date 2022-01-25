package processors

import com.influxdb.annotations.Column
import com.influxdb.annotations.Measurement
import com.influxdb.client.domain.WritePrecision
import com.influxdb.client.kotlin.WriteKotlinApi
import com.influxdb.query.FluxRecord
import getClient
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.runBlocking
import java.time.Instant
import kotlin.math.abs

// tag::code[]

fun main() = runBlocking {

    val dateFrom = "2021-01-01T00:00:00.000Z"
    val dateTo = "2021-02-01T00:00:00.000Z"

    val lastMsg = CasTeplota()
    val lastTemp = CasTeplota()
    var breach = false

    getClient().use {
        val writeApi = it.getWriteKotlinApi()
        it.getQueryKotlinApi().query(
            """option v = {timeRangeStart: $dateFrom, timeRangeStop: $dateTo}
                     from(bucket: "l3") 
                      |> range(start: v.timeRangeStart, stop: v.timeRangeStop)
                      |> filter(fn: (r) => r["_measurement"] == "REPORTS" or r["_measurement"] == "RAW_TEMPERATURE")
                      |> filter(fn: (r) =>  r["_field"] == "TT" or r["_field"] == "TEMPERATURE")
                      |> group(columns: [])
                      |> sort(columns: ["_time"])"""
        )
            .consumeAsFlow()
            .collect { record ->
                when (record.measurement) {
                    "REPORTS" -> lastMsg.nastav(record)
                    else -> lastTemp.nastav(record)
                }
                if (abs(lastMsg.teplota - lastTemp.teplota) > 1 && lastMsg.cas != null && lastTemp.cas != null) {
                    if (!breach) {
                        saveResult(writeApi, record, breach)
                        breach = true
                    }
                    logMsg(lastMsg, lastTemp)

                } else if (breach) {
                    saveResult(writeApi, record, breach)
                    breach = false
                    logMsg(lastMsg, lastTemp)
                }
            }
    }
}

private fun logMsg(lastMsg: CasTeplota, lastTemp: CasTeplota) {
    println("msg: ${lastMsg.cas} - ${lastMsg.teplota} <-> ${lastTemp.cas} - ${lastTemp.teplota}")
}

private suspend fun saveResult(writeApi: WriteKotlinApi, record: FluxRecord, res: Boolean) {
    writeApi.writeMeasurement(BreachTemp(if (res) "OK" else "WARN", record.time!!), WritePrecision.NS, "reports")
}

class CasTeplota {
    var teplota = Double.MIN_VALUE
    var cas: Instant? = null

    fun nastav(fluxRecord: FluxRecord) {
        teplota = fluxRecord.value as Double
        cas = fluxRecord.time
    }
}

@Measurement(name = "breach_temperature")
data class BreachTemp(
    @Column val state: String,
    @Column(timestamp = true) val time: Instant
)
// end::code[]
