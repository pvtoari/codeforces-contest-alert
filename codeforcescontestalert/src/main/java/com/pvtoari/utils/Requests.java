package com.pvtoari.utils;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.pvtoari.bot.Config;

import java.io.IOException;
import java.util.*;

import org.apache.http.HttpResponse;

public class Requests {

    public static String getRawCodeforcesContests() {
        String res = "";

        CloseableHttpClient client = HttpClients.createDefault();
        Tracer.log(Tracer.HIGH_RISK, "Attempting to send a GET request to " + Config.CODEFORCES_API+ "... Big volume of requests in a short period of time may cause IP ban.");  
        HttpGet request = new HttpGet(Config.CODEFORCES_API);
        HttpResponse response = null;
        Scanner sc = null;

        try {
            response = client.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();
            sc = new Scanner(response.getEntity().getContent());
            Tracer.log(Tracer.INFO, "GET request returned code " + (statusCode==200 ? "200, performing further operations..." : statusCode + ", aborting GET request."));
            
            if(statusCode != 200) return "fail";

            while(sc.hasNext()) {
                res += sc.nextLine() + "\n";
            }
        } catch (Exception e) {
            e.printStackTrace();
            Tracer.log(Tracer.HIGH_RISK, "Failure while performing GET request to Codeforces API.");
        } finally {
            try {
                client.close();
                sc.close();

                Tracer.log(Tracer.INFO, "Closeable resources closed due to end of GET request.");
            } catch (IOException e) {
                e.printStackTrace();
                Tracer.log(Tracer.MEDIUM_RISK, "Failure while closing closeable resources.");
            }
        }

        return res;
    }

    private static void splitBy4096(int length, String raw, ArrayList<String> res) {
        Tracer.log(Tracer.LOW_RISK, "Raw content splitting is running...");
        int nSplits = length/4096;
        int remaining = length%4096;

        
        for(int i = 0; i < nSplits; i++) {
            res.add(raw.substring(i*4096, (i+1)*4096));
        }
        
        // añadir el resto de la cadena
        if(remaining > 0) {
            res.add(raw.substring(nSplits*4096, length));
        }
        
        Tracer.log(Tracer.MEDIUM_RISK, "Raw content length: " + length + "splitted into " + nSplits + " parts with " + remaining + " remaining characters.");
    }

    public static ArrayList<String> getSplittedRawCodeforcesContests() {
        ArrayList<String> res = new ArrayList<>();
        String raw = getRawCodeforcesContests();

        if(raw.equals("fail")) {
            Tracer.log(Tracer.HIGH_RISK, "Raw content obtention failed. Aborting raw content splitting.");
            return null;
        }

        if(raw.length() > 4096) {
            splitBy4096(raw.length(), raw, res);
        } else {
            res.add(raw);
        }
        return res;
    }

    public static String getRawFilteredContent() {

        Tracer.log(Tracer.HIGH_RISK, "Invoking raw content obtention...");
        String messageContent = getRawCodeforcesContests();

        if(messageContent.equals("fail")) {
            Tracer.log(Tracer.HIGH_RISK, "Raw content obtention failed. Aborting filtered content obtention.");
            return "fail";
        }

        Tracer.log(Tracer.LOW_RISK, "Filtering raw content...");
        char[] contentChars = messageContent.toCharArray();
        int stoppingIndex = messageContent.indexOf("\"phase\":\"FINISHED\"");
        int deletingCurlyBraceIndex = stoppingIndex;
            
        while(contentChars[deletingCurlyBraceIndex] != '{') {
            contentChars[deletingCurlyBraceIndex] = 0;
            deletingCurlyBraceIndex--;
        }
            
        contentChars[deletingCurlyBraceIndex] = 0;
        contentChars[deletingCurlyBraceIndex-1] = 0;
            
        int beautifyIndex = messageContent.indexOf("[");
        for(int i = 0; i <= beautifyIndex; i++) {
            contentChars[i] = 0;
        }
            
        String newContent = new String(contentChars).substring(0, stoppingIndex);
        Tracer.log(Tracer.INFO, "Finished filtering raw content");

        return newContent;
    }

    public static String getFormatedUpcomingContests() {
        String res = "";
        Tracer.log(Tracer.LOW_RISK, "Parsing filtered data...");
        //Contest[] contests = Contest.parseRawFilteredData(getRawFilteredContent()); this line is replaced by the new handler
        Contest[] contests = Contest.parseRawFilteredData(ContestHandler.getRawFilteredContentv2());
        Arrays.sort(contests, new DescendingContestComparator());

        if(contests == null) {
            Tracer.log(Tracer.HIGH_RISK, "Filtered data parsing failed. Aborting formatted content generation.");
            return "An error ocurred while processing your request. Please try again later.";
        }

        Tracer.log(Tracer.LOW_RISK, "Generating formatted content...");
        for(Contest contest : contests) {
            res += contest.getFormattedMessageContent() + "\n";
        }

        return res;
    }

    public static class DescendingContestComparator implements Comparator<Contest> {
        @Override
        public int compare(Contest c1, Contest c2) {
            return c2.compareTo(c1);
        }
    }
}