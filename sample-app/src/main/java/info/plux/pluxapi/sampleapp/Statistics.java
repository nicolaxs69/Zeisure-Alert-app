package info.plux.pluxapi.sampleapp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Created by Nicolas on 17/11/2017.
 */

public class Statistics {

    List <Integer> arrayList;
    int size;

    public Statistics( List<Integer>arrayList) {
        this.arrayList = arrayList;
        size = arrayList.size();
    }


    public  int getMin(List<Integer>arrayList){
        Object Min = Collections.min(arrayList);
        return (int) Min;
    }


    public  int sum(List<Integer> arrayList) {
        int sum = 0;
        for (int i = 0; i < size; i++){
            sum = sum + arrayList.get(i);
        }
        return sum;
    }


    public  double average(List<Integer> arrayList) {
        double average = sum(arrayList) / size;
        return average;
    }

//    public  double variance(List<Integer> arrayList) {
//        double sumMinusAverage = sum(arrayList) - average(arrayList);
//        double result = 1 / (size - 1) * Math.pow(sumMinusAverage, 2);
//        return result;
//    }

    public  double variance(List<Integer> arrayList) {
        double sumDiffsSquared = 0.0;
        double avg = average(arrayList);
        for (int value : arrayList)
        {
            double diff = value - avg;
            diff *= diff;
            sumDiffsSquared += diff;
        }
        return sumDiffsSquared  / (arrayList.size()-1);
    }


    public int getMax(List<Integer> arrayList) {
        Object Max = Collections.max(arrayList);
        return (int) Max;
    }


}
