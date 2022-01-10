package online.grimen.influxds

import com.influxdb.client.kotlin.InfluxDBClientKotlinFactory
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import net.sf.jasperreports.engine.JRDataSource
import net.sf.jasperreports.engine.JRDataset
import net.sf.jasperreports.engine.JRValueParameter
import net.sf.jasperreports.engine.JasperReportsContext
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource
import net.sf.jasperreports.engine.query.JRAbstractQueryExecuter
import java.util.Date

const val PROP_PREFIX = "org.letadlo.influx"
const val PROP_TOKEN = "$PROP_PREFIX.token"
const val PROP_URL = "$PROP_PREFIX.url"
const val PROP_ORGANIZATION = "$PROP_PREFIX.organization"


/**
 * @author Mikulas Emingr
 */
class InfluxQueryExecuter(
    jasperReportsContext: JasperReportsContext,
    dataset: JRDataset,
    parameters: Map<String, JRValueParameter>
) :
    JRAbstractQueryExecuter(jasperReportsContext, dataset, parameters) {

    override fun cancelQuery() = false

    override fun close() {}

    private fun getProperty(prop: String) = propertiesUtil.getProperty(prop)

    override fun createDatasource(): JRDataSource {
        val query = queryString

        return JRMapCollectionDataSource(runBlocking {
            InfluxDBClientKotlinFactory.create(
                getProperty(PROP_URL),
                getProperty(PROP_TOKEN).toCharArray(),
                getProperty(PROP_ORGANIZATION)
            ).use {
                it.getQueryKotlinApi().query(query).consumeAsFlow().map { it.values }.toList()
            }
        })
    }

    override fun getParameterReplacement(parameterName: String) =
        when (val param = getParameterValue(parameterName)) {
            is Date -> param.toInstant().toString()
            else -> param.toString()
        }

    init {
        parseQuery()
    }
}