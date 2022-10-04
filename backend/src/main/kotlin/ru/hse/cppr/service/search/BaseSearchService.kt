package ru.hse.cppr.service.search

import com.jsoniter.JsonIterator
import com.jsoniter.any.Any
import org.jooq.Record
import ru.hse.cppr.data.database_generated.Tables
import ru.hse.cppr.data.database_generated.tables.SchemaContent

abstract class BaseSearchService: SearchService {

    val schemaContent: SchemaContent = Tables.SCHEMA_CONTENT

    fun satisfiesFilterParamSearch(filterParams: String?, row: Record): Boolean {
        if (filterParams == null) {
            return true
        }
        val content = row[schemaContent.CONTENT]
        val jsonContent: Any
        try {
            jsonContent = JsonIterator.deserialize(content)
        } catch (e: Exception) {
            return false
        }

        val filters = filterParams.split("----")

        for (filter in filters) {
            val decodedFilter = java.net.URLDecoder.decode(filter, "UTF-8")
            val filterKey = decodedFilter.split("====")[0]
            val filterValue = decodedFilter.split("====")[1]

            var check = false

            if (!jsonContent[filterKey].toString().toLowerCase().contains(filterValue.toLowerCase())) {
                return false
            }
        }
        return true
    }
}