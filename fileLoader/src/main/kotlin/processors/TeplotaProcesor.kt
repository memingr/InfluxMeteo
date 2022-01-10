package processors

import com.influxdb.annotations.Column
import com.influxdb.annotations.Measurement
import com.influxdb.client.domain.WritePrecision
import com.influxdb.client.kotlin.InfluxDBClientKotlinFactory
import com.influxdb.query.FluxRecord
import getClient
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.runBlocking
import java.time.Instant
import kotlin.math.abs


@Measurement(name = "breach_temperature")
data class BreachTemp(
    @Column val state: String,
    @Column(timestamp = true) val time: Instant
)

fun main() = runBlocking {

    val dateFrom = "2021-01-01T00:00:00.000Z"
    val dateTo = "2021-02-01T00:00:00.000Z"
    val client = getClient()

    val writeApi = client.getWriteKotlinApi()

    val lastMsg = CasTeplota()
    val lastTemp = CasTeplota()
    var current: CasTeplota
    var breachStart = Instant.MAX
    var breachStop: Instant
    var breach = false

    client.use {

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
            .collect {
                current = if (it.measurement == "REPORTS") {
                    lastMsg.nastav(it)
                } else {
                    lastTemp.nastav(it)
                }
                if (abs(lastMsg.teplota - lastTemp.teplota) > 1 && lastMsg.teplota > Double.MIN_VALUE && lastTemp.teplota > Double.MIN_VALUE) {
                    if (!breach) {
                        breachStart = current.cas
                        writeApi.writeMeasurement(BreachTemp("WARN", it.time!!), WritePrecision.NS, "reports")
                        breach = true
                    }
                    println("msg: ${lastMsg.cas} - ${lastMsg.teplota} <-> ${lastTemp.cas} - ${lastTemp.teplota}")

                } else {
                    if (breach) {
                        breachStop = current.cas
                        breach = false
                        writeApi.writeMeasurement(BreachTemp("OK", it.time!!), WritePrecision.NS, "reports")
                        println("msg: ${lastMsg.cas} - ${lastMsg.teplota} <-> ${lastTemp.cas} - ${lastTemp.teplota}")
                        println("breach: $breachStart - $breachStop")
                    }
                }
            }
    }
}

class CasTeplota {
    var teplota = Double.MIN_VALUE
    var cas = Instant.MIN

    fun nastav(fluxRecord: FluxRecord): CasTeplota {
        teplota = fluxRecord.value as Double
        cas = fluxRecord.time
        return this
    }
}

