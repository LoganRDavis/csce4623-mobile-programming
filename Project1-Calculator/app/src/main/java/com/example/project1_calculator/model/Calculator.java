package com.example.project1_calculator.model;

import android.util.Log;

public class Calculator {

    private static String TAG = Calculator.class.getName();

    private String textResult = "";
    private String validInputList = "+-*/.0123456789";
    private String operatorList = "+-*/";

    public Calculator() {
        restart();
    }

    public void restart() {
        this.textResult = "";
    }

    public String getTextResult() {
        return this.textResult;
    }

    public void addInput(String input){
        char previousInput = (this.textResult.length() > 0) ? this.textResult.charAt(this.textResult.length() - 1) : ' ';
        if (operatorList.contains(String.valueOf(input)) || input.charAt(0) == '.') {
            if (operatorList.contains(String.valueOf(previousInput)) || previousInput == ' ') {
                return;
            }
        }
        if (input.charAt(0) == '.'){
            int stringLength = this.textResult.length();
            for (int i = stringLength - 1; i >= 0; i--) {
                if (operatorList.contains(String.valueOf(this.textResult.charAt(i)))) {
                    break;
                }
                if (this.textResult.charAt(i) == '.') {
                    return;
                }
            }
        }
        this.textResult += input;
    }

    public void removeInput(){
        if (this.textResult != null && this.textResult.length() != 0) {

            //Checks to make sure math isn't done on invalid characters
            int stringLength = this.textResult.length();
            for (int i = 0; i < stringLength; i++) {
                char input = this.textResult.charAt(i);
                if (!validInputList.contains(String.valueOf(input))) {
                    this.textResult = "";
                    return;
                }
            }

            this.textResult = this.textResult.substring(0, this.textResult.length() - 1);
        }
    }

    public void changeSign() {
        int stringLength = this.textResult.length();
        for (int i = stringLength - 1; i >= 0; i--) {
            char input = this.textResult.charAt(i);
            char previousInput = (i - 1 > 0) ? this.textResult.charAt(i - 1) : ' ';
            if (operatorList.contains(String.valueOf(input))) {
                if (input == '-') {
                    if (operatorList.contains(String.valueOf(previousInput)) || previousInput == ' ') {
                        this.textResult = this.textResult.substring(0, i) + this.textResult.substring(i +1, stringLength);
                        return;
                    }
                    this.textResult = this.textResult.substring(0, i) + "+" + this.textResult.substring(i +1, stringLength);
                    return;
                }
                if (input == '+') {
                    this.textResult = this.textResult.substring(0, i) + "-" + this.textResult.substring(i +1, stringLength);
                    return;
                }
                this.textResult = this.textResult.substring(0, i + 1) + "-" + this.textResult.substring(i + 1, stringLength);
                return;
            }
        }
        this.textResult = "-" + this.textResult;
    }

    public String calculateResult() {
       int stringLength = this.textResult.length();
       if (stringLength == 0 ||
               operatorList.contains(String.valueOf(this.textResult.charAt(stringLength - 1))) ||
               this.textResult.charAt(stringLength - 1) == 'E') {
           return this.textResult;
       }

       double result = 0.0;
       char previousInput = ' ';
       char previousOperator = ' ';
       String currentNumber = "";
       for (int i = 0; i < stringLength; i++) {
           char input = this.textResult.charAt(i);

           if ((operatorList.contains(String.valueOf(previousInput)) || previousInput == ' ') && input == '-') {
               currentNumber += '-';
           } else if (operatorList.contains(String.valueOf(input)) || i == stringLength - 1) {
               if (i == stringLength - 1) {
                   currentNumber += input;
               }
               Log.e("Me", previousInput + "");
               Log.e("Me", previousOperator + "");
               Log.e("Me", currentNumber);

               double number = Double.parseDouble(currentNumber);
               switch (previousOperator) {
                   case '+':
                       result = result + number;
                       break;
                   case '-':
                       result = result - number;
                       break;
                   case '*':
                       result = result * number;
                       break;
                   case '/':
                       result = result / number;
                       break;
                   default:
                       result = number;
                       break;
               }
               currentNumber = "";
               previousOperator = input;
           } else {
               currentNumber += input;
           }
           previousInput = input;
       }

       return Double.toString(result);
    }
}