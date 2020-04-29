package com.arumugam.hospitalfinder;

import android.location.Location;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


public class ApiQuery {
    private String my_api_key="AIzaSyBUAk2Y6BDJFAeExCNNgFDDJfq6TXYnFVw";
    public StringBuffer query(Location location)
    {
        String q = "https://maps.googleapis.com/maps/api/place/details/json?rankby=distance&keyword=hosiptal&location";
        q=q+location.getLatitude()+","+location.getLongitude();
        q=q+"&key="+my_api_key;

        try {
            URL url = new URL(q);
            HttpURLConnection httpconn = (HttpURLConnection)url.openConnection();
            InputStream inputStream = httpconn.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            StringBuffer sbuff = new StringBuffer();
            String s="";

            while((s=br.readLine())!=null)
            {
                sbuff.append(s);
            }

            return sbuff;
        }
        catch(Exception e) {
            Log.d("url exception", e.toString());
        }

        return null;
    }

    public ArrayList<HospitalDetails> stringToObjects(StringBuffer sbuff) {
        ArrayList<HospitalDetails> hospdetails = new ArrayList();

        if (sbuff == null)
            return null;

        try {
            JSONObject jsonobj = new JSONObject(sbuff.toString());
            JSONArray jsonarray = jsonobj.getJSONArray("result");

            for(int i=0; i<jsonarray.length(); i++){

                    HospitalDetails hospitalDetails = new HospitalDetails();

                    JSONObject jsonObject = jsonarray.getJSONObject(i);

                    if(jsonObject.getString("name")!=null)  hospitalDetails.setHospitalName(jsonObject.getString("name"));
                    else  hospitalDetails.setHospitalName("Not Available");

                    try {
                        hospitalDetails.setRating(String.valueOf(jsonObject.getDouble("rating")));
                    }catch (Exception e){
                        hospitalDetails.setRating("Not Available");
                    }

                    try {
                        if (jsonObject.getJSONObject("opening_hours").getBoolean("open_now"))  hospitalDetails.setOpeningHours("Opened");
                        else hospitalDetails.setOpeningHours("closed");
                    } catch (Exception e) {
                        hospitalDetails.setOpeningHours("Not Available");
                    }

                    hospitalDetails.setAddress(jsonObject.getString("vicinity"));
                    hospitalDetails.setLocationlatlng(new double[]{jsonObject.getJSONObject("geometry").getJSONObject("location").getDouble("lat"),
                            jsonObject.getJSONObject("geometry").getJSONObject("location").getDouble("lng")});

                    hospdetails.add(hospitalDetails);
            }

            return hospdetails;
        }
        catch(Exception e) {
            Log.d("jsonobject", e.toString());
            return null;
        }

    }
}
