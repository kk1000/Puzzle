package chars.com.puzzle;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import chars.com.puzzle.view.PuzzleLayout;

public class MainActivity extends AppCompatActivity {

    private PuzzleLayout mPuzzleLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPuzzleLayout = (PuzzleLayout)findViewById(R.id.id_puzzle);
        mPuzzleLayout.setmListener(new PuzzleLayout.PuzzleListener() {
            @Override
            public void nextLevel(int nextLevel) {
                new AlertDialog.Builder(MainActivity.this).setTitle("Game Info").setMessage("Level Up").setPositiveButton("Next Level", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mPuzzleLayout.nextLevel();
                    }
                }).show();
            }

            @Override
            public void timeChanged(int currentTime) {

            }

            @Override
            public void gameOver() {

            }
        });
    }
}
