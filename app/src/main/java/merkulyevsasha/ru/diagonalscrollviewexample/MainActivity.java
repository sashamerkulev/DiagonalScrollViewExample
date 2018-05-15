package merkulyevsasha.ru.diagonalscrollviewexample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.Locale;

import merkulyevsasha.ru.diagonalscrollviewexample.controls.DiagonalScrollView;

public class MainActivity extends AppCompatActivity implements DiagonalScrollView.ScrollChangedListener{

    private static final int MAX_ROWS = 100;
    private static final int MAX_COLUMNS = 50;

    private DiagonalScrollView diagonal;
    private LinearLayout table;
    private LinearLayout rows;
    private LinearLayout columns;

    private int dp30;
    private int dp50;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        diagonal = findViewById(R.id.diagonal);
        table = findViewById(R.id.table);
        rows = findViewById(R.id.rows);
        columns = findViewById(R.id.columns);

        dp30 = (int)(30 * getResources().getDisplayMetrics().density);
        dp50 = (int)(60 * getResources().getDisplayMetrics().density);

        for(int j = 0; j < MAX_ROWS; j ++){

            TextView tv = getTextView();
            tv.setText(String.format(Locale.getDefault(), "%d", j));

            columns.addView(tv);
        }

        for(int i=0; i < MAX_ROWS; i++){

            LinearLayout tr = new LinearLayout(this);
            tr.setOrientation(LinearLayout.HORIZONTAL);

            TextView row = getTextView();
            row.setText(String.format(Locale.getDefault(), "%d", i));
            rows.addView(row);

            for(int j = 0; j < MAX_COLUMNS; j ++){

                TextView col = getTextView();
                col.setText(String.format(Locale.getDefault(), "%d - %d", i, j));

                tr.addView(col);
            }

            table.addView(tr);
        }

        diagonal.setScrollViewListener(this);
    }

    private TextView getTextView(){
        TextView tv = new TextView(this);

        tv.setBackgroundResource(R.drawable.rectangle);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp50, dp30);
        tv.setLayoutParams(params);
        tv.setGravity(Gravity.CENTER);
        return tv;
    }

    @Override
    public void onScrollChanged(FrameLayout scrollView, int x, int y, int oldx, int oldy) {
        if (scrollView instanceof DiagonalScrollView){
            rows.scrollTo(0, y);
            columns.scrollTo(x, 0);
        }
    }
}
