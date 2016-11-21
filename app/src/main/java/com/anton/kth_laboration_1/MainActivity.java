package com.anton.kth_laboration_1;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.xml.sax.Parser;

import java.io.File;
import java.lang.reflect.Array;
import java.net.URL;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private CurrencyManager currencyManager;
    private Currency selectedFrom;
    private Currency selectedTo;

    public static final String currency_url = "http://maceo.sth.kth.se/Home/eurofxref";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Spinner spinner_from = (Spinner) findViewById(R.id.spinner_rate_from);
        Spinner spinner_to = (Spinner) findViewById(R.id.spinner_cur_to);
        final EditText value_from = (EditText) findViewById(R.id.value_from);

        //Handler event for when an item is selected from the spinner.
        spinner_from.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedFrom = (Currency)parent.getSelectedItem();
                updateConversion();

                ((TextView)findViewById(R.id.text_rate_from)).setText("Rate: " + selectedFrom.getRate());
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        //Handler event for when an item is selected from the spinner.
        spinner_to.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedTo = (Currency)parent.getSelectedItem();
                updateConversion();

                ((TextView)findViewById(R.id.text_rate_to)).setText("Rate: " + selectedTo.getRate());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //Handler event for when a new FROM value has been set by the user.
        value_from.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_DONE){
                    updateConversion();
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    protected void onStart(){
        super.onStart();

        ParserTask tsk = new ParserTask(this);
        tsk.execute(currency_url);
    }

    /**
     * This method converts the input value to the target currency.
     */
    private void updateConversion(){
        if(selectedTo != null && selectedFrom != null){
            EditText value_from = (EditText)findViewById(R.id.value_from);
            TextView result = (TextView)findViewById(R.id.value_to);

            if(value_from.getText().length() < 1){
                value_from.setText("1");
                showToast("Input field must not be empty!");
            }

            //Convert to euro.
            Double inEuro = Double.parseDouble(value_from.getText().toString())/selectedFrom.getRate();

            //Convert to target currency.
            Double resultConversion = inEuro * selectedTo.getRate();

            //display result
            result.setText("" + resultConversion);
        }
    }

    /**
     *
     */
    private class ParserTask extends AsyncTask<String, Void, CurrencyManager>{

        private MainActivity activity;

        public ParserTask(MainActivity activity){
            this.activity = activity;
        }

        @Override
        protected CurrencyManager doInBackground(String... params){
            android.os.Debug.waitForDebugger();
            try{
                URL url = new URL(params[0]);
                CurrencyManager mngr = new CurrencyManager(activity);

                mngr.loadCurrenciesFromFile();

                Calendar today = Calendar.getInstance();

                mngr.getLoadDate().compareTo(today);
                long diff = today.getTime().getTime() - mngr.getLoadDate().getTime().getTime();

                long days = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);

                if(days > 1){
                    //we should refresh data

                    if(mngr.loadCurrencies(url) == false){
                        //download from web failed
                        //use the data from the file
                        mngr.loadCurrenciesFromFile();
                    }
                }
                return mngr;
            }catch(Exception e){
                this.cancel(true);
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(CurrencyManager currencyMngr){
            currencyManager = null;
            currencyManager = currencyMngr;

            if(currencyManager.isLoadedFromFile()){
                showToast("Could not download new data. Using currencies from " + currencyMngr.getLoadDate().getTime().toString() + " instead.");
            }
            showToast("Loaded " + currencyManager.getCurrencies().size() + " items.");

            Spinner spinner_from = (Spinner) findViewById(R.id.spinner_rate_from);
            Spinner spinner_to = (Spinner) findViewById(R.id.spinner_cur_to);
            ArrayAdapter<Currency> adapter = new ArrayAdapter(activity, R.layout.support_simple_spinner_dropdown_item, currencyMngr.getCurrencies());
            spinner_from.setAdapter(adapter);
            spinner_to.setAdapter(adapter);

        }

        @Override
        protected void onCancelled(CurrencyManager manager){showToast("Could not load list with currencies.");}
    }

    private void showToast(String msg){
        Toast toast = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
        toast.show();
    }

}


