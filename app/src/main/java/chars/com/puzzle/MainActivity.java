package chars.com.puzzle;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import chars.com.puzzle.view.PuzzleLayout;

public class MainActivity extends AppCompatActivity {

    private PuzzleLayout mPuzzleLayout;
    private TextView mLevel;
    private TextView mTime;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTime = (TextView)findViewById(R.id.id_time);
        mLevel = (TextView)findViewById(R.id.id_level);

        mPuzzleLayout = (PuzzleLayout)findViewById(R.id.id_puzzle);
        mPuzzleLayout.setTimeEnabled(true);
        mPuzzleLayout.setmListener(new PuzzleLayout.PuzzleListener() {
            @Override
            public void nextLevel(final int nextLevel) {
                new AlertDialog.Builder(MainActivity.this).setTitle("Game Info").setMessage("Level Up").setPositiveButton("Next Level", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mPuzzleLayout.nextLevel();
                        mLevel.setText(""+nextLevel);
                    }
                }).show();
            }

            @Override
            public void timeChanged(int currentTime) {
                mTime.setText(""+currentTime);
            }

            @Override
            public void gameOver() {
                new AlertDialog.Builder(MainActivity.this).setTitle("Game Info").setMessage("Game Over").setPositiveButton("Again", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mPuzzleLayout.restart();
                    }
                }).setNegativeButton("Quit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }).show();
            }
        });
    }
}
