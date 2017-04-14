package me.iwf.photopicker.adapter;

import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import me.iwf.photopicker.entity.Media;
import me.iwf.photopicker.entity.MediaDirectory;
import me.iwf.photopicker.event.Selectable;

public abstract class SelectableAdapter<VH extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<VH> implements Selectable {

    private static final String TAG = SelectableAdapter.class.getSimpleName();

    protected List<MediaDirectory> photoDirectories;
    protected ArrayList<Media> selectedPhotos;

    public int currentDirectoryIndex = 0;


    public SelectableAdapter() {
        photoDirectories = new ArrayList<>();
        selectedPhotos = new ArrayList<>();
    }


    /**
     * Indicates if the item at position where is selected
     *
     * @param media Media of the item to check
     * @return true if the item is selected, false otherwise
     */
    @Override
    public boolean isSelected(Media media) {
        return getSelectedPhotos().contains(media);
    }

    /**
     * Toggle the selection status of the item at a given position
     *
     * @param media Media of the item to toggle the selection status for
     */
    @Override
    public void toggleSelection(Media media) {
        if (selectedPhotos.contains(media)) {
            selectedPhotos.remove(media);
        } else {
            selectedPhotos.add(media);
        }
    }


    /**
     * Clear the selection status for all items
     */
    @Override
    public void clearSelection() {
        selectedPhotos.clear();
    }


    /**
     * Count the selected items
     *
     * @return Selected items count
     */
    @Override
    public int getSelectedItemCount() {
        return selectedPhotos.size();
    }


    public void setCurrentDirectoryIndex(int currentDirectoryIndex) {
        this.currentDirectoryIndex = currentDirectoryIndex;
    }


    public ArrayList<Media> getCurrentPhotos() {
        return photoDirectories.get(currentDirectoryIndex).getMedias();
    }

    public List<Media> getSelectedPhotos() {
        return selectedPhotos;
    }

}