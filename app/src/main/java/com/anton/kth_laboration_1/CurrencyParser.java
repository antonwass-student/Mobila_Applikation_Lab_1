package com.anton.kth_laboration_1;

import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Time;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by Anton on 2016-11-14.
 */

public class CurrencyParser {

    private XmlPullParser parser;
    private ArrayList<Currency> currencies;
    private Calendar loadDate;

    public CurrencyParser() throws XmlPullParserException{
        this.parser = XmlPullParserFactory.newInstance().newPullParser();
    }

    public Calendar parse(InputStream xmlStream, ArrayList<Currency> currencies) throws Exception{
        parser.setInput(xmlStream, null);
        this.currencies = currencies;

        int parseEvt = parser.getEventType();

        while(parseEvt != XmlPullParser.END_DOCUMENT){
            switch(parseEvt){
                case XmlPullParser.END_DOCUMENT:
                    break;
                case XmlPullParser.START_DOCUMENT:
                    break;
                case XmlPullParser.START_TAG:
                    String tag = parser.getName();
                    if(tag.equalsIgnoreCase("cube")){
                        parseCurrency();
                    }
                    break;
                case XmlPullParser.END_TAG:
                    break;
                case XmlPullParser.TEXT:
                    break;
                default:
                    break;
            }

            parseEvt = parser.next();
        }

        xmlStream.close();
        Log.d("","Parsing complete");
        return loadDate;
    }

    private void parseCurrency() throws IOException, XmlPullParserException{
        try{
            int parseEvent;
            String name;
            do{
                parseEvent = parser.next();
                name=parser.getName();
                if(parseEvent == XmlPullParser.START_TAG){
                    if(name.equalsIgnoreCase("cube")){
                        if(parser.getAttributeCount() > 0){
                            if(parser.getAttributeName(0).equalsIgnoreCase("time")){
                                //This is the cube containing currency cubes.
                                DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

                                try {
                                    loadDate = Calendar.getInstance();
                                    loadDate.setTime(formatter.parse(parser.getAttributeValue(0)));
                                    Log.d("", "TIME = " + loadDate.getTime());
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }else{
                                //This is a cube containing a currency
                                double rate = 0;
                                String currency = "";
                                if(parser.getAttributeName(0).equalsIgnoreCase("currency")){
                                    currency = parser.getAttributeValue(0);
                                }

                                if(parser.getAttributeName(1).equalsIgnoreCase("rate")){
                                    rate = Double.parseDouble(parser.getAttributeValue(1));
                                }

                                if(rate>0 && !currency.equalsIgnoreCase("")){
                                    currencies.add(new Currency(currency, rate));
                                    Log.d("", "Size = " + currencies.size());
                                }

                            }
                        }
                    }
                }
            }while(parseEvent != XmlPullParser.END_TAG || name.equalsIgnoreCase("cube"));

            Log.d("", "Done parsing cube");
        }catch(Exception e){
            e.printStackTrace();
            Log.d("", e.getMessage());
        }
    }
}
