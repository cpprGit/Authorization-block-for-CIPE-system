package ru.hse.cppr.service.organisation

import org.jooq.Configuration
import ru.hse.cppr.service.Service
import java.util.*

interface OrganisationRelationshipsService: Service {

    fun getAncestors(id: UUID): MutableList<Map<String, Any>>

    fun getDescendants(id: UUID?): MutableList<Map<String, Any>>

    fun addRelation(parentId: UUID, childId: UUID, configuration: Configuration): Map<String, String>

    fun getAncestors(id: UUID?, configuration: Configuration): MutableList<Map<String, Any>>

}