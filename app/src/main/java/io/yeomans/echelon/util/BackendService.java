package io.yeomans.echelon.util;


import io.yeomans.echelon.models.BackendSpotifyAuth;
import io.yeomans.echelon.models.Token;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Created by jason on 6/25/16.
 */
public interface BackendService {

    @POST("spotify-auth")
    Call<Token> authSpotify(@Body BackendSpotifyAuth createUser);

}
