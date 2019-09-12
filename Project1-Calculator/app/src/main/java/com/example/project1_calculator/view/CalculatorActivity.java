package com.example.project1_calculator.view;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Button;

import com.example.project1_calculator.R;
import com.example.project1_calculator.presenter.CalculatorPresenter;
import android.content.pm.ActivityInfo;

public class CalculatorActivity extends AppCompatActivity {

    private static String TAG = CalculatorActivity.class.getName();

    private ViewGroup buttonGrid;
    private TextView textResult;

    CalculatorPresenter presenter = new CalculatorPresenter(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);
        buttonGrid = (ViewGroup) findViewById(R.id.buttonGrid);
        textResult = (TextView) findViewById(R.id.textResult);
        presenter.onCreate();
    }

    public void onButtonClick(View v) {
        Button button = (Button) v;
        String tag = button.getTag().toString();
        presenter.onButtonSelected(tag);
    }

    public void setTextResult(String textResult) {
        this.textResult.setText(textResult);
    }
}
