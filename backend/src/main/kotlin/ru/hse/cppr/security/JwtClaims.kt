package ru.hse.cppr.security

import ru.hse.cppr.application.AppConfig.getClaimNamespace

enum class JwtClaims(val value: String) {
/*    ID(getClaimNamespace()+"user_id"),
    ROLE(getClaimNamespace()+"role"),
    NAME(getClaimNamespace()+"name")*/
    ID("user_id"),
    ROLE("role"),
    NAME("name")
}