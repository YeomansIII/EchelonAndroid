package io.yeomans.echelon;

/**
 * Created by jason on 10/5/15.
 */
public class Participant {
    private boolean active;
    private String displayName, extUrl, imageUrl;

    public Participant() {

    }

    public Participant(String displayName, String extUrl, String imageUrl) {
        this.displayName = displayName;
        this.extUrl = extUrl;
        this.imageUrl = imageUrl;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getExtUrl() {
        return extUrl;
    }

    public void setExtUrl(String extUrl) {
        this.extUrl = extUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
