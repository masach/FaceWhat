package com.chat.ui.adapter.album;


import java.io.Serializable;

/**
 * 
 * @Description Õº∆¨∂‘œÛ
 *
 */
@SuppressWarnings("serial")
public class ImageItem implements Serializable {
    private String imageId;
    private String thumbnailPath;
    private String imagePath;
    private boolean isSelected = false;

    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    public String getThumbnailPath() {
        return thumbnailPath;
    }

    public void setThumbnailPath(String thumbnailPath) {
        this.thumbnailPath = thumbnailPath;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }
}
