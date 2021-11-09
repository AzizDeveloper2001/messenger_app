package uz.pdp.messanger.models.NotificationModels

data class RequestModel(
	val notification: Notification? = null,
	val to: String? = null
)

data class Notification(
	val title: String? = null,
	val body: String? = null
)

