package hershedtilak.sliderpuzzle;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.widget.ImageView;

/**
 * Created by Hershed on 8/27/2015.
 * Class for individual tiles in Slider Puzzle
 */
public class TileView extends ImageView {

    private boolean isLastTile;
    private Point final_position;

    public TileView(Context context) {
        super(context);
        initUI();
    }

    public TileView(Context context, Bitmap b, Point p) {
        super(context);
        super.setImageBitmap(b);
        final_position = p;
        initUI();
    }

    private void initUI(){
        isLastTile = false;
    }

    public void setLastTile() {
        isLastTile = true;
    }
    public void setFinalPosition(Point p) {
        final_position = p;
    }

    public boolean isLastTile() {
        return isLastTile;
    }

    public Point getFinalPosition(){
        return final_position;
    }
}
