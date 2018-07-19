package org.dinglh.modern;

import java.text.NumberFormat;

public class Base {

    public static void StringBase() {
        String str = "C:\\Users\\dingk\\Videos\\Radeon ReLive\\2018.07.06-12.11_660mb_t_t.mp4";
        String filename = str.substring(str.lastIndexOf("\\") + 1); //2018.07.06-12.11_660mb_t_t.mp4
        
        int numerator = 111, denominator = 500;
        NumberFormat numberFormat = NumberFormat.getInstance();
        numberFormat.setMaximumFractionDigits(0);
        String result = numberFormat.format((float)numerator/(float)denominator * 100);
        Integer.valueOf(result).intValue();   
    }
    
    public static void main(String[] args)
    {
        StringBase();
    }
}
