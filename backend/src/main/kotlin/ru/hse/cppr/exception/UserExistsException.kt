package ru.hse.cppr.exception

import io.undertow.util.BadRequestException

class UserExistsException(message: String?) : BadRequestException(message) {
}