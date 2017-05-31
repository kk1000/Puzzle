package chars.com.puzzle.util;

import android.graphics.Bitmap;

/**
 * Created by dengw on 2017/5/31.
 */

public class ImagePiece {
    private int index;
    private Bitmap bitmap;

    public ImagePiece() {
    }

    public ImagePiece(int index, Bitmap bitmap) {
        this.index = index;
        this.bitmap = bitmap;
    }

    public int getIndex() {
        return index;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    @Override
    public String toString() {
        return "ImagePiece{" +
                "index=" + index +
                ", bitmap=" + bitmap +
                '}';
    }
}
