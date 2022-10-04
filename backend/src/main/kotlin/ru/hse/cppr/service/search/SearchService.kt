package ru.hse.cppr.service.search

import ru.hse.cppr.service.Service
import java.util.*

interface SearchService: Service {

    fun search(params: Map<String, Deque<String>>): Map<String, Any>?
}