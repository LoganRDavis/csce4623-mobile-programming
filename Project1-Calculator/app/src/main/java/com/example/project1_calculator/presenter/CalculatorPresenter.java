package com.example.project1_calculator.presenter;

import android.util.Log;

import com.example.project1_calculator.model.Calculator;
import com.example.project1_calculator.view.CalculatorActivity;

public class CalculatorPresenter implements Presenter {

    private static String TAG = CalculatorPresenter.class.getName();

    private CalculatorActivity view;
    private Calculator model;

    public CalculatorPresenter(CalculatorActivity view) {
       this.view = view;
       this.model = new Calculator();
    }

    @Override
    public void onCreate() {
        model = new Calculator();
    }

    @Override
    public void onPause() {}

    @Override
    public void onResume() {}

    @Override
    public void onDestroy() {}

    public void onButtonSelected(String tag) {
        Log.d(TAG, tag);
        switch (tag) {
            case "C":
                this.model.restart();
                break;
            case "B":
                this.model.removeInput();
                break;
            case "S":
                this.model.changeSign();
                break;
            case "=":
                String result = this.model.calculateResult();
                this.model.restart();
                this.model.addInput(result);
                break;
            default:
                this.model.addInput(tag);
                break;
        }
        this.view.setTextResult(this.model.getTextResult());
    }

    public void onResetSelected() {
        model.restart();
    }


}
