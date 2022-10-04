package ru.hse.cppr.service.notifications

enum class NotificationActions(val value: String) {
    SEND_MESSAGE("отправил(а) сообщение:" ),
    CREATED_PROJECT_REQUEST("создал(а) заявку на проект"),
    UPDATED_PROFILE("изменил(а) информацию в своем профиле"),
    UPDATED_ORGANISATION("изменил(а) информацию об организации"),
    CREATED_QUESTIONNAIRE("создал(а) новый опросник"),
}