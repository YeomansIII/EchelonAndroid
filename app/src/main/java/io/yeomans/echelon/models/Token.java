package io.yeomans.echelon.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by jason on 6/25/16.
 */
public class Token {

  @SerializedName("token")
  @Expose
  private String token;

  @SerializedName("error")
  @Expose
  private String error;

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }

  public String getError() {
    return error;
  }

  public void setError(String error) {
    this.error = error;
  }
}
