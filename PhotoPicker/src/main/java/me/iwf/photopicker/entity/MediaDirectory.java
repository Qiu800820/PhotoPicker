package me.iwf.photopicker.entity;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

import me.iwf.photopicker.utils.FileUtils;

/**
 * Created by donglua on 15/6/28.
 */
public class MediaDirectory {

    private String id;
    private String coverPath;
    private String name;
    private long dateAdded;
    private ArrayList<Media> medias = new ArrayList<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MediaDirectory)) return false;

        MediaDirectory directory = (MediaDirectory) o;

        boolean hasId = !TextUtils.isEmpty(id);
        boolean otherHasId = !TextUtils.isEmpty(directory.id);

        if (hasId && otherHasId) {
            if (!TextUtils.equals(id, directory.id)) {
                return false;
            }

            return TextUtils.equals(name, directory.name);
        }

        return false;
    }

    @Override
    public int hashCode() {
        if (TextUtils.isEmpty(id)) {
            if (TextUtils.isEmpty(name)) {
                return 0;
            }

            return name.hashCode();
        }

        int result = id.hashCode();

        if (TextUtils.isEmpty(name)) {
            return result;
        }

        result = 31 * result + name.hashCode();
        return result;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCoverPath() {
        return coverPath;
    }

    public void setCoverPath(String coverPath) {
        this.coverPath = coverPath;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(long dateAdded) {
        this.dateAdded = dateAdded;
    }

    public ArrayList<Media> getMedias() {
        return medias;
    }

    public void setMedias(ArrayList<Media> medias) {
        if (medias == null) return;
        for (int i = 0, j = 0, num = medias.size(); i < num; i++) {
            Media p = medias.get(j);
            if (p == null || !FileUtils.fileIsExists(p.getPath())) {
                medias.remove(j);
            } else {
                j++;
            }
        }
        this.medias = medias;
    }

    public List<String> getPhotoPaths() {
        List<String> paths = new ArrayList<>(medias.size());
        for (Media media : medias) {
            paths.add(media.getPath());
        }
        return paths;
    }

    public void addPhoto(int id, String path, int type) {
        if (FileUtils.fileIsExists(path)) {
            medias.add(new Media(id, path, type));
        }
    }

}
