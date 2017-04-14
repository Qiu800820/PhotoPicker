package me.iwf.photopicker.event;

import me.iwf.photopicker.entity.Media;

/**
 * Created by donglua on 15/6/30.
 */
public interface Selectable {


    /**
     * Indicates if the item at position position is selected
     *
     * @param media Media of the item to check
     * @return true if the item is selected, false otherwise
     */
    boolean isSelected(Media media);

    /**
     * Toggle the selection status of the item at a given position
     *
     * @param media Media of the item to toggle the selection status for
     */
    void toggleSelection(Media media);

    /**
     * Clear the selection status for all items
     */
    void clearSelection();

    /**
     * Count the selected items
     *
     * @return Selected items count
     */
    int getSelectedItemCount();

}
