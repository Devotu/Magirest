package test;

import java.net.*;
import java.io.*;
import java.util.*;
import com.fasterxml.jackson.databind.node.ObjectNode;

import play.Logger;

public class Requests {

    public static String makeRequest(String myUrl, String httpMethod, ObjectNode params) {

        try {
            URL url = new URL(myUrl);
            Logger.debug("Requesting " + myUrl + " / " + httpMethod + " / " + params.toString());
            HttpURLConnection conn = null;
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoInput(true);
            conn.setRequestProperty("Content-Type", "application/json");
            DataOutputStream dos = null;
            conn.setRequestMethod(httpMethod);

            if (Arrays.asList("POST", "PUT", "DELETE").contains(httpMethod)) {
                String parameters = params.toString();
                conn.setDoOutput(true);
                dos = new DataOutputStream(conn.getOutputStream());
                dos.writeBytes(parameters);
                dos.flush();
                dos.close();
            }

            int respCode = conn.getResponseCode();
            if (respCode != 200 && respCode != 201 && respCode != 101) {
                String error = inputStreamToString(conn.getErrorStream());
                return error;
            }
            
            return inputStreamToString(conn.getInputStream());

        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }

    public static String makeRequest(String myUrl, String httpMethod) {
        
                try {
                    URL url = new URL(myUrl);
                    Logger.debug("Requesting " + myUrl + " / " + httpMethod);
                    HttpURLConnection conn = null;
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setDoInput(true);
                    DataOutputStream dos = null;
                    conn.setRequestMethod(httpMethod);
        
                    int respCode = conn.getResponseCode();
                    if (respCode != 200 && respCode != 201 && respCode != 101) {
                        String error = inputStreamToString(conn.getErrorStream());
                        return error;
                    }
                    
                    return inputStreamToString(conn.getInputStream());
        
                } catch (Exception e) {
                    e.printStackTrace();
                }
        
                return "";
            }

    public static String inputStreamToString(InputStream is) {

        try {
            BufferedReader br = null;
            StringBuilder sb = new StringBuilder();
    
            String line;
            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            br.close();
            return sb.toString();
            
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }
}