package hershedtilak.sliderpuzzle;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.GridLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Hershed on 8/27/2015.
 * Class for Slider Puzzle
 */
public class PuzzleView extends GridLayout implements OnTouchListener {

    private Bitmap puzzleImage;
    private TextView text;

    ArrayList<Point> solution = new ArrayList();
    private ArrayList<TileView> currentBoard = new ArrayList();
    private TileView lastTile;

    private static int columns = 4;
    private static int rows = 4;

    public PuzzleView(Context context) {
        super(context);
    }

    public PuzzleView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void linkText(TextView t) {
        text = t;
    }

    public void createPuzzle(){

        // Initialize GridView
        setColumnCount(columns);
        setRowCount(rows);

        // get tile width and height
        int tileWidth = puzzleImage.getWidth()/columns;
        int tileHeight = puzzleImage.getHeight()/rows;

        // Initialize Board
        for(int i = 0; i < rows; i++) {
            for(int j = 0; j < columns; j++) {
                // create solution Array List
                solution.add(new Point(i, j));

                // initialize image tiles
                if(i == rows-1 && j == columns-1) {
                    lastTile = new TileView(this.getContext());
                    lastTile.setFinalPosition(new Point(i,j));
                    android.view.ViewGroup.LayoutParams layoutParams = new android.view.ViewGroup.LayoutParams(tileWidth, tileHeight);
                    lastTile.setLayoutParams(layoutParams);
                    lastTile.setLastTile();
                } else {
                    Bitmap b = Bitmap.createBitmap(puzzleImage, j * tileWidth, i * tileHeight, tileWidth, tileHeight);
                    currentBoard.add(new TileView(this.getContext(), b, new Point(i, j)));
                }
            }
        }

        // shuffle tiles
        Collections.shuffle(currentBoard);
        currentBoard.add(lastTile);

        // add tiles to screen
        for(int k = 0; k < rows*columns; k++) {
            currentBoard.get(k).setOnTouchListener(this);
            this.addView(currentBoard.get(k));
        }

    }

    public void setPuzzleImage(Bitmap b) { puzzleImage = b; }

    // Touch Event Listener
    public boolean onTouch(View v, MotionEvent event) {

        if(event.getActionMasked() != MotionEvent.ACTION_DOWN)
            return false;

        TileView touchedTile = (TileView) v;

        // get index of touched tile
        int tidx = currentBoard.indexOf(v);

        // get index of last tile (last tile is the tile with no image with which other tiles may be switched
        int lidx = 0;
        for(TileView t : currentBoard) {
            if (t.isLastTile()) {
                lidx = currentBoard.indexOf(t);
            }
        }

        // check if touched tile is a valid tile to move
        if(((tidx-1 == lidx) && (tidx%columns!=0)) || ((tidx+1 == lidx) && (tidx%columns!=(columns-1)))
                || (tidx-columns == lidx) || (tidx+columns == lidx)) {
            Collections.swap(currentBoard, tidx, lidx);
            updateBoard();
            if(checkWin()) {
                text.setText("Puzzle Complete!");
            }

            return true;
        }
        return false;
    }

    // redraws the current puzzle board onto the screen
    private void updateBoard() {
        for(int k = 0; k < rows*columns; k++) {
            bringChildToFront(currentBoard.get(k));
            requestLayout();
            invalidate();
        }
    }

    // checks win condition
    private boolean checkWin() {
        text.setText("");
        for(int k = 0; k < rows*columns; k++) {
            if(!solution.get(k).equals(currentBoard.get(k).getFinalPosition())) {
                return false;
            }
        }
        return true;
    }

    // resets puzzle
    public void resetPuzzle() {
        solution.clear();
        currentBoard.clear();
    }

}