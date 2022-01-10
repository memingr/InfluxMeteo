/*
 * Copyright (C) 2005 - 2020 TIBCO Software Inc. All rights reserved.
 * http://www.jaspersoft.com.
 *
 * Unless you have purchased a commercial license agreement from Jaspersoft,
 * the following license terms apply:
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package online.grimen.influxds

import net.sf.jasperreports.engine.JRDataset
import net.sf.jasperreports.engine.JRValueParameter
import net.sf.jasperreports.engine.JasperReportsContext
import net.sf.jasperreports.engine.query.AbstractQueryExecuterFactory

/**
 * @author Mikuláš Emingr
 */
class InfluxQueryExecuterFactory : AbstractQueryExecuterFactory() {

    override fun getBuiltinParameters(): Array<Any> = emptyArray()

    override fun createQueryExecuter(
        jasperReportsContext: JasperReportsContext,
        dataset: JRDataset,
        parameters: Map<String, JRValueParameter>
    ) = InfluxQueryExecuter(jasperReportsContext, dataset, parameters)

    override fun supportsQueryParameterType(className: String) = true
}