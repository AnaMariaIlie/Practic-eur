package ro.pub.cs.systems.eim.practicaltest02.network;

import android.util.Log;


import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.ResponseHandler;
import cz.msebera.android.httpclient.client.entity.UrlEncodedFormEntity;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.impl.client.BasicResponseHandler;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.message.BasicNameValuePair;
import cz.msebera.android.httpclient.protocol.HTTP;
import ro.pub.cs.systems.eim.practicaltest02.general.Constants;
import ro.pub.cs.systems.eim.practicaltest02.general.Utilities;
import ro.pub.cs.systems.eim.practicaltest02.model.WeatherForecastInformation;

public class CommunicationThread extends Thread {

    private ServerThread serverThread;
    private Socket socket;

    public CommunicationThread(ServerThread serverThread, Socket socket) {
        this.serverThread = serverThread;
        this.socket = socket;
    }

    @Override
    public void run() {
        if (socket == null) {
            Log.e(Constants.TAG, "[COMMUNICATION THREAD] Socket is null!");
            return;
        }
        try {
            BufferedReader bufferedReader = Utilities.getReader(socket);
            PrintWriter printWriter = Utilities.getWriter(socket);
            if (bufferedReader == null || printWriter == null) {
                Log.e(Constants.TAG, "[COMMUNICATION THREAD] Buffered Reader / Print Writer are null!");
                return;
            }
            Log.i(Constants.TAG, "[COMMUNICATION THREAD] Waiting for parameters from client (oper1 / information type!");
            String oper1 = bufferedReader.readLine();
            String oper2 = bufferedReader.readLine();
            String informationType = bufferedReader.readLine();

            if (oper1 == null || oper1.isEmpty() ||  oper2 == null || oper2.isEmpty() ||informationType == null || informationType.isEmpty()) {
                Log.e(Constants.TAG, "[COMMUNICATION THREAD] Error receiving parameters from client (oper1 / information type!");
                return;
            }

            List<String> data = serverThread.getData();
            String  res = null;

                Log.i(Constants.TAG, "[COMMUNICATION THREAD] Getting the information from the webservice...");


                HttpClient httpClient = new DefaultHttpClient();
                HttpGet httpPost = new HttpGet(Constants.WEB_SERVICE_ADDRESS + "EUR" + ".json");
                /*List<NameValuePair> params = new ArrayList<>();
                params.add(new BasicNameValuePair(Constants.QUERY_ATTRIBUTE, "EUR"));
                UrlEncodedFormEntity urlEncodedFormEntity = new UrlEncodedFormEntity(params, HTTP.UTF_8);
                httpPost.setEntity(urlEncodedFormEntity);*/

                ResponseHandler<String> responseHandler = new BasicResponseHandler();
                String pageSourceCode = httpClient.execute(httpPost, responseHandler);
                if (pageSourceCode == null) {
                    Log.e(Constants.TAG, "[COMMUNICATION THREAD] Error getting the information from the webservice!");
                    return;
                }

               JsonElement jelement = new JsonParser().parse(pageSourceCode);
               JsonObject jobject = jelement.getAsJsonObject();
               jobject = jobject.getAsJsonObject("bpi");

            String currentObservation= jobject.getAsJsonObject("USD").get("rate").getAsString();


/*
                Document document = Jsoup.parse(pageSourceCode);
                Element nush = document.child(0).child(1);
                String nush2 = document.child(0).child(1).toString().substring(document.child(0).child(1).toString().indexOf("{"));
                nush2 = nush2.substring(0, nush2.length() - 8);//cred ca 8
                try {
                    JSONObject content = new JSONObject(nush2);
                    String a = "b";
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            Element element = document.child(0);
                Elements elements = element.getElementsByTag(Constants.SCRIPT_TAG);
                Elements elementsok = elements.tagName("bpi");
                Elements elementsusd = elements.tagName("USD");
               Elements elementsrate= elements.tagName("rate");
            String currentObservation= elementsrate.get(0).text();*/

              /* for (Element script: elements) {

                    String scriptData = script.data();

                     JSONObject content = new JSONObject("");
                      currentObservation = content.getString("rate");


                     serverThread.setData(oper1, oper2, "");
                     break;

                }*/

            switch(informationType) {
                case "+":
                    res = String.valueOf(Integer.parseInt(oper1) + Integer.parseInt(oper2));
                    break;
                case "-":
                    res = String.valueOf(Integer.parseInt(oper1) - Integer.parseInt(oper2));
                    break;
                case "*":
                    res = String.valueOf(Integer.parseInt(oper1) * Integer.parseInt(oper2));
                    break;
                case "/":
                    res = String.valueOf(Integer.parseInt(oper1) / Integer.parseInt(oper2));
                    break;
                case "^":
                    res = String.valueOf(Integer.parseInt(oper1) * Integer.parseInt(oper2));
                    break;
                case "%":
                    res = String.valueOf(Integer.parseInt(oper1) % Integer.parseInt(oper2));
                    break;
                default:
                    res = "[COMMUNICATION THREAD] Wrong information type (all / temperature / wind_speed / condition / humidity / pressure)!";
            }

            printWriter.println(currentObservation);
            printWriter.flush();
        } catch (IOException ioException) {
            Log.e(Constants.TAG, "[COMMUNICATION THREAD] An exception has occurred: " + ioException.getMessage());
            if (Constants.DEBUG) {
                ioException.printStackTrace();
            }
        /*}catch (JSONException jsonException) {
            Log.e(Constants.TAG, "[COMMUNICATION THREAD] An exception has occurred: " + jsonException.getMessage());
            if (Constants.DEBUG) {
                jsonException.printStackTrace();
            }*/
        }/* catch (JSONException e) {
            e.printStackTrace();
        }*/ finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException ioException) {
                    Log.e(Constants.TAG, "[COMMUNICATION THREAD] An exception has occurred: " + ioException.getMessage());
                    if (Constants.DEBUG) {
                        ioException.printStackTrace();
                    }
                }
            }
        }
    }

}
