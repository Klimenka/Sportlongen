package nl.klimenko.sportlongen.service

import nl.klimenko.sportlongen.model.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface BackendService {

    @GET("api/v1/patients/{id}/completed-exercises")
    fun getCompletedExercises(@Header("Authorization") token: String?, @Path("id") id: Int): Call<List<CompletedExercise>>

    @GET("api/v1/completed-exercises/{id}")
    fun getCompletedExercise(@Header("Authorization") token: String?, @Path("id") id: Int): Call<CompletedExercise>

    @GET("api/v1/patients/{id}/notifications")
    fun getNotifications(@Header("Authorization") token: String?, @Path("id") id: Int): Call<List<Notification>>

    @GET("/api/v1/patients/{id}")
    fun getPatient(@Header("Authorization") token: String?, @Path("id") id: Int): Call<Patient>

    @POST("/api/v1/completed-exercises")
    fun postCompletedExercise(
        @Header("Authorization") token: String?,
        @Body completedExercise: CompletedExerciseForPost
    ): Call<CompletedExercise>

    @POST("/api/v1/exercise-logs")
    fun postExerciseLog(
        @Header("Authorization") token: String?,
        @Body ExerciseLog: ExerciseLog
    ): Call<ExerciseLog>

    @DELETE("/api/v1/completed-exercises/{id}")
    fun deleteCompletedExercise(
        @Header("Authorization") token: String?,
        @Path("id") id: Int
    ): Call<Void>

    @POST("api/v1/auth/login")
    fun login(@Body login: Login): Call<LoginResponse>

    @POST("api/v1/users/activate")
    fun registerUser(@Body register: Register): Call<RegisterResponse>

    @POST("api/v1/users/forget-password")
    fun forgetPassword(@Body forgotPasswordRequest: ForgotPasswordRequest): Call<ResponseBody>

    @POST("api/v1/users/reset-password")
    fun resetPassword(@Body resetPasswordRequest: ResetPasswordRequest): Call<ResponseBody>

    @PUT("api/v1/patients")
    fun createProfile(@Header("Authorization") token: String?, @Body patient: Patient): Call<Patient>

    @POST("api/v1/auth/logout")
    fun logout(@Header("Authorization") token: String?): Call<ResponseBody>

    @POST("api/v1/auth/refreshToken")
    fun refreshToken(@Header("x-no-refresh") noRefresh: Boolean, @Body value: RefreshTokenRequest): Call<LoginResponse>

}