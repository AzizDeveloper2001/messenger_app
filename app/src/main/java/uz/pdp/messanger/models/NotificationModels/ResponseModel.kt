package uz.pdp.messanger.models.NotificationModels

data class ResponseModel(
	val canonicalIds: Int? = null,
	val success: Int? = null,
	val failure: Int? = null,
	val results: List<ResultsItem?>? = null,
	val multicastId: Long? = null
)

data class ResultsItem(
	val messageId: String? = null
)

