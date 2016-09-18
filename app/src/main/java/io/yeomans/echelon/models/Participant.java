package io.yeomans.echelon.models;

/**
 * Created by jason on 10/5/15.
 */
public class Participant {
  private boolean online;
  private String display_name, ext_url, image_url, friend_code;

  public Participant() {

  }

  public Participant(String display_name, String ext_url, String image_url) {
    this.display_name = display_name;
    this.ext_url = ext_url;
    this.image_url = image_url;
  }

  public boolean isOnline() {
    return online;
  }

  public void setOnline(boolean online) {
    this.online = online;
  }

  public String getDisplay_name() {
    return display_name;
  }

  public void setDisplay_name(String display_name) {
    this.display_name = display_name;
  }

  public String getFriend_code() {
    return friend_code;
  }

  public void setFriend_code(String friend_code) {
    this.friend_code = friend_code;
  }

  public String getExt_url() {
    return ext_url;
  }

  public void setExt_url(String ext_url) {
    this.ext_url = ext_url;
  }

  public String getImage_url() {
    return image_url;
  }

  public void setImage_url(String image_url) {
    this.image_url = image_url;
  }
}
