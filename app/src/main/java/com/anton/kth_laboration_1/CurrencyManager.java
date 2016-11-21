package com.anton.kth_laboration_1;

import android.content.Context;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;

import javax.xml.parsers.DocumentBuilder;

/**
 * Created by Anton on 2016-11-14.
 */

public class CurrencyManager {

    private MainActivity activity;
    private String fileName = "currencies.dat";
    private ArrayList<Currency> currencies;
    private boolean loadedFromFile = false;
    private Calendar loadDate = null;

    public CurrencyManager(MainActivity activity){
        this.currencies = new ArrayList<Currency>();
        this.activity = activity;
    }

    public ArrayList<Currency> getCurrencies(){
        return currencies;
    }

    /***
     * Loads currencies from an XML file.
     * @param url To the XML file
     * @return If loading currencies was successful.
     */
    public boolean loadCurrencies(URL url){

        HttpURLConnection urlConnection = null;
        try{

            urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.connect();

            CurrencyParser parser = new CurrencyParser();

            currencies.clear();

            currencies.add(new Currency("EUR", 1));

            loadDate = parser.parse(urlConnection.getInputStream(), currencies);

            Log.d("", String.valueOf(currencies.size()));

            saveCurrencies();

            loadedFromFile = false;

            return true;

        }catch(Exception e){
            e.printStackTrace();
            return false;
        }finally {
            if(urlConnection!=null)
                urlConnection.disconnect();
        }

    }

    /***
     * Saves list of currencies to a file.
     * @return true if data was saved successfully.
     */
    private boolean saveCurrencies(){

        FileOutputStream outputStream = null;
        PrintWriter pw = null;

        try{
            outputStream = activity.getApplicationContext().openFileOutput(fileName, Context.MODE_PRIVATE);

            pw = new PrintWriter(outputStream);
            pw.println(loadDate.getTime().toString());
            for(Currency c : currencies){
                pw.println(c.getName()+":"+c.getRate());
            }

            pw.flush();

        }catch(Exception e){
            e.printStackTrace();
            return false;
        }finally {
            try {
                pw.close();
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        return true;
    }

    /**
     * Loads currencies from a file instead. The file to load is specified in the class.
     * @return the arraylist containing the currencies.
     */
    public ArrayList<Currency> loadCurrenciesFromFile(){

        FileInputStream inputStream;
        ArrayList<Currency> loadedCurrencies = new ArrayList();

        try{
            inputStream = activity.openFileInput(fileName);
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line;
            String date = reader.readLine();
            String d = "Fri Nov 06 00:00:00 GMT+00:00 2015";
            DateFormat formatter = new SimpleDateFormat("EEE MMM dd hh:mm:ss z yyyy");
            this.loadDate = Calendar.getInstance();
            loadDate.setTime(formatter.parse(date));

            while((line=reader.readLine()) != null){
                String[] split = line.split(":");
                String name = split[0];
                double rate = Double.parseDouble(split[1]);
                loadedCurrencies.add(new Currency(name, rate));
            }
        }catch(Exception e){
            e.printStackTrace();
        }

        if(loadedCurrencies.size() > 1){
            loadedFromFile = true;
        }else{
            loadedFromFile = false;
        }
        currencies = loadedCurrencies;
        return loadedCurrencies;
    }

    public boolean isLoadedFromFile() {
        return loadedFromFile;
    }

    public Calendar getLoadDate() {
        return loadDate;
    }
}
