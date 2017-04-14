package me.iwf.photopicker.entity;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

/**
 * Created by donglua on 15/6/30.
 */
public class Media implements Parcelable {

    public static final int FILE_TYPE_IMAGE = 0;
    public static final int FILE_TYPE_VIDEO = 3;

    public static final int STATUS_NONE = 0;
    public static final int STATUS_ZIP = 1;
    public static final int STATUS_UPLOAD = 2;

    private int id;
    private String path;
    private String thumbnail;
    private int type;
    private int status;
    private String url;


    public Media(int id, String path, int type) {
        this.id = id;
        this.path = path;
        this.thumbnail = path;
        this.type = type;
    }

    public Media() {
    }

    protected Media(Parcel in) {
        id = in.readInt();
        path = in.readString();
        thumbnail = in.readString();
        type = in.readInt();
        url = in.readString();
        status = in.readInt();
    }

    public static final Creator<Media> CREATOR = new Creator<Media>() {
        @Override
        public Media createFromParcel(Parcel in) {
            return new Media(in);
        }

        @Override
        public Media[] newArray(int size) {
            return new Media[size];
        }
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Media)) return false;

        Media media = (Media) o;

        return !TextUtils.isEmpty(path) && path.equals(media.path);
    }

    @Override
    public int hashCode() {
        return id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public boolean isUpload() {
        return this.status == STATUS_UPLOAD;
    }

    public boolean isZip() {
        return this.status == STATUS_ZIP;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(path);
        dest.writeString(thumbnail);
        dest.writeInt(type);
        dest.writeString(url);
        dest.writeInt(status);
    }
}
