package uz.pdp.messanger.Retrofit



import retrofit2.http.Headers
import retrofit2.http.POST

import retrofit2.http.Body


import retrofit2.Call
import uz.pdp.messanger.Constants
import uz.pdp.messanger.models.NotificationModels.RequestModel
import uz.pdp.messanger.models.NotificationModels.ResponseModel


interface ApiService {

    @Headers("Content-Type:application/json","Authorization: key=${Constants.SERVER_KEY}")
    @POST("fcm/send")
     fun sendNotification(@Body requestModel: RequestModel?):Call<ResponseModel>

}