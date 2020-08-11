package com.roshanaryal.guessthecelibrity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    ImageView profileImage;
    Button button1;
    Button button2;
    Button button3;
    Button button4;

    ArrayList<String> clebUrls=new ArrayList<String>();
    ArrayList<String> clebNamw=new ArrayList<String>();
    String[] answer=new String[4];
    int locationOfCorrectAnswer=0;

    int choosenCleb=0;

    private boolean haveNetworkConnection() {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }
        return haveConnectedWifi || haveConnectedMobile;
    }

    public void clebChoosen(View view){


        if (view.getTag().toString().equals(Integer.toString(locationOfCorrectAnswer))){
            Toast.makeText(getApplicationContext(),"Right Answer",Toast.LENGTH_SHORT).show();



        }
        else {
            Toast.makeText(getApplicationContext(),"Wrong it is\n"+clebNamw.get(choosenCleb),Toast.LENGTH_SHORT).show();
        }
        if (haveNetworkConnection()) {
            newQuestion();
        }

    }


    public class DownloadImage extends AsyncTask<String,Void, Bitmap>{

        @Override
        protected Bitmap doInBackground(String... urls) {

            try {
            URL url=new URL(urls[0]);
            HttpURLConnection urlConnection=(HttpURLConnection)url.openConnection();
            urlConnection.connect();
            InputStream inputStream=urlConnection.getInputStream();
            Bitmap bitmap= BitmapFactory.decodeStream(inputStream);

            return bitmap;



            }catch (Exception e){
                e.printStackTrace();
                return null;

            }


        }
    }

    public class downloadTask extends AsyncTask<String,Void,String>{

        @Override
        protected String doInBackground(String... urls) {
            String result="";
            URL url;
            HttpURLConnection urlConnection=null;

            try {
                url=  new URL( urls[0]);

                urlConnection=(HttpURLConnection)url.openConnection();
                InputStream in=urlConnection.getInputStream();
                InputStreamReader reader=new InputStreamReader(in);

                int data=reader.read();
                while (data!=-1){
                    char current=(char) data;
                    result+=current;
                    data=reader.read();

                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return result;
        }
    }

    public void newQuestion(){
        try {


            Random random = new Random();
            choosenCleb = random.nextInt(clebUrls.size());

            DownloadImage downloadImage = new DownloadImage();

            Bitmap clebImage = null;

            clebImage = downloadImage.execute(clebUrls.get(choosenCleb)).get();


            profileImage.setImageBitmap(clebImage);

            locationOfCorrectAnswer = random.nextInt(4);

            int IncorrectAnswerLocation;

            for (int i = 0; i < 4; i++) {
                if (i == locationOfCorrectAnswer) {
                    answer[i] = clebNamw.get(choosenCleb);
                } else {
                    IncorrectAnswerLocation = random.nextInt(clebUrls.size());
                    while (IncorrectAnswerLocation == choosenCleb) {
                        IncorrectAnswerLocation = random.nextInt(clebUrls.size());
                    }
                    answer[i] = clebNamw.get(IncorrectAnswerLocation);
                }

            }

            button1.setText(answer[0]);
            button2.setText(answer[1]);
            button3.setText(answer[2]);
            button4.setText(answer[3]);
        }
        catch (Exception e){
            e.printStackTrace();
        }


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        profileImage=findViewById(R.id.profile_image);

        button1=findViewById(R.id.button4);
        button2=findViewById(R.id.button3);
        button3=findViewById(R.id.button2);
        button4=findViewById(R.id.button1);


        if (haveNetworkConnection()) {


            downloadTask task = new downloadTask();

            String result = null;

            try {
                result = task.execute("http://www.posh24.se/kandisar").get();

                String[] splitresult = result.split("<div class=\"listedArticle\">");

                Pattern p = Pattern.compile("<img src=\"(.*?)\" alt=");
                Matcher m = p.matcher(splitresult[0]);

                while (m.find()) {
                    clebUrls.add(m.group(1));
                }


                p = Pattern.compile("alt=\"(.*?)\"/>");
                m = p.matcher(splitresult[0]);
                while (m.find()) {
                    clebNamw.add(m.group(1));
                }

                newQuestion();

            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        else {
            Toast.makeText(getApplicationContext(),"No internet available\nconnect to internet",Toast.LENGTH_SHORT).show();
        }

    }
}
