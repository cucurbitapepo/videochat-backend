package com.videochatapi.service;

import com.videochatapi.controller.livekit.LiveKitApi;
import com.videochatapi.props.AppProperties;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Response;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Service
public class LiveKitService {

  private final LiveKitApi liveKitApi;
  private final String apiKey;
  private final String apiSecret;
  private final String liveKitUrl;
  private final LiveKitTokenService tokenService;

  public LiveKitService(AppProperties appProperties, LiveKitTokenService liveKitTokenService) {
    this.apiKey = appProperties.getLivekit().getApiKey();
    this.apiSecret = appProperties.getLivekit().getApiSecret();
    this.liveKitUrl = appProperties.getLivekit().getInternalUrl() + "/";
    this.tokenService = liveKitTokenService;

    HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
    logging.setLevel(HttpLoggingInterceptor.Level.BODY);

    OkHttpClient client = new OkHttpClient.Builder()
            .addInterceptor(new AuthenticationInterceptor(tokenService))
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();

    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(liveKitUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    this.liveKitApi = retrofit.create(LiveKitApi.class);
  }

  public LiveKitApi.Room createRoom(String roomName, int maxParticipants) {
    try {
      Call<LiveKitApi.Room> call = liveKitApi.createRoom(
              new LiveKitApi.CreateRoomRequest(roomName, 300, maxParticipants)
      );
      Response<LiveKitApi.Room> response = call.execute();

      if (response.isSuccessful() && response.body() != null) {
        return response.body();
      } else {
        throw new RuntimeException("Failed to create room: " +
                                   (response.errorBody() != null ? response.errorBody().string() : "Unknown error"));
      }
    } catch (Exception e) {
      throw new RuntimeException("Error creating room", e);
    }
  }

  public void deleteRoom(String roomName) {
    try {
      LiveKitApi.DeleteRoomRequest deleteRoomRequest = new LiveKitApi.DeleteRoomRequest(roomName);
      Call<Void> call = liveKitApi.deleteRoom(deleteRoomRequest);
      Response<Void> response = call.execute();

      if (!response.isSuccessful()) {
        throw new RuntimeException("Failed to delete room: " +
                                   (response.errorBody() != null ? response.errorBody().string() : "Unknown error"));
      }
    } catch (Exception e) {
      throw new RuntimeException("Error deleting room", e);
    }
  }

  private static class AuthenticationInterceptor implements Interceptor {
    private final LiveKitTokenService tokenService;

    public AuthenticationInterceptor(LiveKitTokenService tokenService) {
      this.tokenService = tokenService;
    }

    @Override
    public okhttp3.Response intercept(Chain chain) throws IOException {
      String apiToken = tokenService.generateApiToken();

      Request request = chain.request();
      Request authenticatedRequest = request.newBuilder()
              .header("Authorization", "Bearer " + apiToken) //
              .build();
      return chain.proceed(authenticatedRequest);
    }
  }
}