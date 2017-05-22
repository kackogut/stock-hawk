package com.udacity.stockhawk.ui;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.LineScatterCandleRadarDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

import net.danlew.android.joda.JodaTimeAndroid;

import org.joda.time.DateTime;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Timer;

import au.com.bytecode.opencsv.CSVReader;
import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

/**
 * Created by kacper on 03/05/2017.
 */

public class DetailStockActivity extends AppCompatActivity{

    @BindView(R.id.date_value_tv)
    TextView mDateValueTV;
    @BindView(R.id.stock_value_tv)
    TextView mStockValueTV;
    @BindView(R.id.chart)
    LineChart mStockHistoryChart;

    public static final String EXTRA_SYMBOL_NAME = "symbol_name";
    public static final String SAVED_STATE_DATA = "data";
    public static final String SAVED_STATE_VALUE= "value";

    private String mSymbolName;
    private List<String[]> mStockValues;
    private List<Entry> mChartEntries;

    private int mSeclectedValue;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_stock);
        ButterKnife.bind(this);
        JodaTimeAndroid.init(this);

        if(getIntent()!=null){
            if(getIntent().hasExtra(EXTRA_SYMBOL_NAME)){
                mSymbolName = getIntent().getStringExtra(EXTRA_SYMBOL_NAME);
                getSupportActionBar().setTitle(mSymbolName);
                initChart();
            }
        }
    }
    private void initChart(){
        setStockValues();
        setAxis();
        setListener();
    }

    private void setListener() {
        mStockHistoryChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                showTextViews(true);
                mSeclectedValue = (int)e.getX();
                mStockValueTV.setText(Float.toString(e.getY()));
                String date = mStockValues.get(mSeclectedValue)[0];
                mDateValueTV.setText(formatDate(date));
            }

            @Override
            public void onNothingSelected() {
                showTextViews(false);
            }
        });
    }

    private void setStockValues(){
        mStockValues = Collections.emptyList();
        Cursor cursor = getContentResolver().query(Contract.Quote.makeUriForStock(mSymbolName), null, null, null, null);
        if(cursor.moveToFirst()){
            String value = cursor.getString(cursor.getColumnIndex(Contract.Quote.COLUMN_HISTORY));
            cursor.close();
            CSVReader csvReader = new CSVReader(new StringReader(value));
            try {
                mStockValues = csvReader.readAll();
                csvReader.close();
            }catch (Exception e){
                Timber.e(e.getMessage());
            }
        }
    }



    private void setAxis(){
        int dateLabelIterator = 0;
        mChartEntries = new ArrayList<>();
        for(String[] strings:mStockValues){
            mChartEntries.add(new Entry(dateLabelIterator,Float.parseFloat(strings[1])));
            dateLabelIterator++;
        }
        LineDataSet dataSet = new LineDataSet(mChartEntries,"Lable");
        dataSet.setColor(Color.GREEN);
        dataSet.setValueTextColor(Color.GREEN);
        LineData lineData = new LineData(dataSet);
        mStockHistoryChart.setData(lineData);
        mStockHistoryChart.invalidate();

        mStockHistoryChart.setKeepPositionOnRotation(true);
        mStockHistoryChart.setDrawGridBackground(false);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if(savedInstanceState.containsKey(SAVED_STATE_VALUE)){

            mSeclectedValue = savedInstanceState.getInt(SAVED_STATE_DATA);
            mDateValueTV.setText(formatDate(mStockValues.get(mSeclectedValue)[0]));
            mStockValueTV.setText(savedInstanceState.getString(SAVED_STATE_VALUE));
            mStockHistoryChart.highlightValue(mSeclectedValue,mSeclectedValue,true);
            showTextViews(true);
            //mStockHistoryChart.highlightValue(mSeclectedValue,mSeclectedValue);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(mDateValueTV.getText().length()>0){
            outState.putInt(SAVED_STATE_DATA, mSeclectedValue);
            outState.putString(SAVED_STATE_VALUE, mStockValueTV.getText().toString());
        }
    }

    private void showTextViews(boolean show){
        if(show){
            mDateValueTV.setVisibility(View.VISIBLE);
            mStockValueTV.setVisibility(View.VISIBLE);
        }else{
            mDateValueTV.setVisibility(View.GONE);
            mStockValueTV.setVisibility(View.GONE);
        }
    }
    private String formatDate(String dateToFormat){
        Date formattedDate = new Date(Long.parseLong(dateToFormat));
        DateTime dateTime = new DateTime(formattedDate);
        return dateTime.getDayOfMonth()+" "+dateTime.monthOfYear().getAsText()+" "+dateTime.getYear();
    }

}
