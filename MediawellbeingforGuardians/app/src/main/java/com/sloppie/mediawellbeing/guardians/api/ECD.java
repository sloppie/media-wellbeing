package com.sloppie.mediawellbeing.guardians.api;

import com.sloppie.mediawellbeing.guardians.api.model.Guardian;
import com.sloppie.mediawellbeing.guardians.api.model.LabelledImage;
import com.sloppie.mediawellbeing.guardians.api.model.Profile;
import com.sloppie.mediawellbeing.guardians.api.model.UserActivity;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.HeaderMap;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;

public interface ECD {
    @FormUrlEncoded
    @POST("api/guardian/new")
    Call<Guardian> createGuardian(@FieldMap Map<String, String> fieldMap);

    @FormUrlEncoded
    @POST("api/guardian/login")
    Call<Guardian> loginUser(@FieldMap Map<String, String> credentials);

    @GET("api/guardian/profiles")
    Call<List<Profile>> getProfiles(@HeaderMap Map<String, String> headerMap);

    @GET("api/guardian/activity")
    Call<List<UserActivity>> fetchUserActivity(@HeaderMap Map<String, String> headerMap);

    @PUT("api/activity/review")
    Call<UserActivity> reviewActivity(@HeaderMap Map<String, String> headerMap, @Body UserActivity userActivity);

    @Multipart
    @POST("label")
    Call<LabelledImage> labelImage(@Part MultipartBody.Part image);
}
