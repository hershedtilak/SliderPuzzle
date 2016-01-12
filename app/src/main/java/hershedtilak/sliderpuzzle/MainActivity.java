package hershedtilak.sliderpuzzle;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.ParcelFileDescriptor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.FileDescriptor;
import java.io.IOException;
import java.lang.ref.WeakReference;

public class MainActivity extends AppCompatActivity {

    private PuzzleView puzzle;
    private TextView text;
    private Button reset_button;
    private Button image_button;

    private static final int SELECT_PICTURE = 1;
    private Uri selectedImageURI = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        puzzle = (PuzzleView) findViewById(R.id.gameBoard);
        text = (TextView) findViewById(R.id.textView);
        reset_button = (Button) findViewById(R.id.reset_button);
        image_button = (Button) findViewById(R.id.image_button);

        // set reset button listener
        reset_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(selectedImageURI != null)
                    createPuzzle(selectedImageURI);
                else
                    text.setText("Select an image");
            }
        });

        // set choose image button listener
        image_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent,
                        "Select Picture"), SELECT_PICTURE);
            }
        });

        puzzle.linkText(text);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // create puzzle after user selects image from library
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                selectedImageURI = data.getData(); // GET REAL URI
                createPuzzle(selectedImageURI);
            }
        }
    }

    public void createPuzzle(Uri imageURI) {
        puzzle.removeAllViews();
        puzzle.resetPuzzle();
        text.setText("");
        PuzzleCreatorTask task = new PuzzleCreatorTask(puzzle);
        task.execute(selectedImageURI);
    }

    // helper function to get a bitmap from the user's photo library
    private Bitmap getBitmapFromUri(Uri uri) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor =
                getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        return image;
    }

    // rotates a bitmap by the specified angle
    private static Bitmap rotateBitmap(Bitmap source, float angle)
    {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    // create puzzle asynchronously
    class PuzzleCreatorTask extends AsyncTask<Uri, Void, Bitmap> {
        private final WeakReference<PuzzleView> puzzleViewReference;
        private Uri imageURI = null;

        public PuzzleCreatorTask(PuzzleView puzzleView) {
            puzzleViewReference = new WeakReference<PuzzleView>(puzzleView);
        }

        // Decode image in background.
        @Override
        protected Bitmap doInBackground(Uri... params) {
            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);

            imageURI = params[0];
            Bitmap puzzleImage = null;
            Bitmap rotatedPuzzleImage = null;
            Bitmap scaledPuzzleImage = null;
            try {
                puzzleImage = getBitmapFromUri(imageURI);
                rotatedPuzzleImage = rotateBitmap(puzzleImage, 90);
                scaledPuzzleImage = Bitmap.createScaledBitmap(rotatedPuzzleImage, metrics.widthPixels - 150, metrics.heightPixels - 800, false);
            } catch (IOException e) {};

            return scaledPuzzleImage;
        }

        // Once complete, see if ImageView is still around and set bitmap.
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (puzzleViewReference != null && bitmap != null) {
                final PuzzleView puzzle = puzzleViewReference.get();
                if (puzzle != null) {
                    puzzle.setPuzzleImage(bitmap);
                    puzzle.createPuzzle();
                }
            }
            if (bitmap == null)
            {
                text.setText("Error loading image");
            }
        }
    }

}
