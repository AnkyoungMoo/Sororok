package com.nexters.sororok.service;

import com.nexters.sororok.model.LoginRequestModel;
import com.nexters.sororok.model.LoginResponseModel;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;

public interface LoginService {

    String BASE_URL = "http://45.63.120.140:40005/";

    Retrofit loginRetrofit = new Retrofit.Builder()
            .baseUrl(LoginService.BASE_URL)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    @POST("member/login")
    Call<LoginResponseModel> login(@Body LoginRequestModel body);

    // 이미지 없이 필수 요건들만 들어간 경우
    @Multipart
    @PUT("member/join")
    Call<LoginResponseModel> signUp(@Part("phone") String phone,
                                    @Part("name") String name,
                                    @Part("email") String email,
                                    @Part("loginType") String loginType,
                                    @Part("loginUid") String loginUid
    );

    // 이미지가 파일인 경우
    @Multipart
    @PUT("member/join")
    Call<LoginResponseModel> signUp(@Part("phone") String phone,
                                    @Part("name") String name,
                                    @Part("email") String email,
                                    @Part("loginType") String loginType,
                                    @Part("loginUid") String loginUid,
                                    @Part MultipartBody.Part image
    );

    // 이미지가 URL인 경우
    @Multipart
    @PUT("member/join")
    Call<LoginResponseModel> signUp(@Part("phone") String phone,
                                    @Part("name") String name,
                                    @Part("email") String email,
                                    @Part("loginType") String loginType,
                                    @Part("loginUid") String loginUid,
                                    @Part("imageUrl") String imageUrl
    );
}
