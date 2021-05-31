package com.sloppie.mediawellbeing.api;

import com.sloppie.mediawellbeing.api.model.Child;
import com.sloppie.mediawellbeing.api.model.LabelledImage;
import com.sloppie.mediawellbeing.api.model.ModelResponse;
import com.sloppie.mediawellbeing.api.model.UserActivity;

import java.util.Map;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.HeaderMap;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;

public interface ECD {
    @Multipart
    @POST("ecd/scan")
    Call<ModelResponse> scanImage(
            @QueryMap Map<String, String> queryParams, @Part MultipartBody.Part image);

    @Multipart
    @POST("label")
    Call<LabelledImage> uploadImage(
            @Header("Authorization") String profileId, @Part MultipartBody.Part image);

    @POST("api/user/link")
    Call<Child> linkChildProfile(@HeaderMap Map<String, String> headerMap);

    @Multipart
    @POST("api/activity")
    Call<UserActivity> setImageForReview(
            @HeaderMap Map<String, String> headerMap,
            @QueryMap Map<String, String> queryMap,
            @Part MultipartBody.Part image
    );

    @GET("api/activity")
    Call<UserActivity> checkActivityReviewStatus(
            @HeaderMap Map<String, String> header, @QueryMap Map<String, String> queryMap);
}
