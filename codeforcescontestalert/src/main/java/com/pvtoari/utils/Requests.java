package com.pvtoari.utils;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;

import java.io.IOException;
import java.util.*;

import org.apache.http.HttpResponse;

public class Requests {

    private static String getRawCodeforcesContests() {
        String res = "";

        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet request = new HttpGet(Config.CODEFORCES_API);
        HttpResponse response = null;
        Scanner sc = null;

        try {
            response = client.execute(request);
            sc = new Scanner(response.getEntity().getContent());
    
            res += response.getStatusLine() + "\n";
            while(sc.hasNext()) {
                res += sc.nextLine() + "\n";
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                client.close();
                sc.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return res;
    }

    private static void splitBy4096(int length, String raw, ArrayList<String> res) {
        int nSplits = length/4096;
        int remaining = length%4096;

        for(int i = 0; i < nSplits; i++) {
            res.add(raw.substring(i*4096, (i+1)*4096));
        }

        // añadir el resto de la cadena
        if(remaining > 0) {
            res.add(raw.substring(nSplits*4096, length));
        }
    }

    public static ArrayList<String> getSplittedRawCodeforcesContests() {
        ArrayList<String> res = new ArrayList<>();
        String raw = getRawCodeforcesContests();

        if(raw.length() > 4096) {
            splitBy4096(raw.length(), raw, res);
        } else {
            res.add(raw);
        }
        return res;
    }

    public static void getJsonedCodeforcesContests() {
        // TODO: use org.json.JSONArray and stuff
    }
}