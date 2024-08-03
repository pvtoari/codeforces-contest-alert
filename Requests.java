package codeforces_api;

import java.io.*;
import java.net.*;

public class Requests {
    public static void getRawContent(){
        try {
            URL url = new URL("https://codeforces.com/api/contest.list?");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");

            BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));

            String content = br.readLine();
            char[] contentChars = content.toCharArray();
            int stoppingIndex = content.indexOf("\"phase\":\"FINISHED\"");
            int deletingCurlyBraceIndex = stoppingIndex;

            while(contentChars[deletingCurlyBraceIndex] != '{') {
                contentChars[deletingCurlyBraceIndex] = 0;
                deletingCurlyBraceIndex--;
            }

            contentChars[deletingCurlyBraceIndex] = 0;
            contentChars[deletingCurlyBraceIndex-1] = 0;
            
            

            //System.out.println("Stopping at index: " + stoppingIndex);
            boolean inQuotes = false;
            int indentLevel = 0;

            for (int i = 0; i < stoppingIndex; i++) {
                char c = contentChars[i];
                if (c == '\"') {
                    inQuotes = !inQuotes;
                    System.out.print(c);
                } else if (c == '{' && !inQuotes) {
                    System.out.println();
                    printIndent(indentLevel);
                    System.out.println(c);
                    indentLevel++;
                    printIndent(indentLevel);
                } else if (c == '}' && !inQuotes) {
                    System.out.println();
                    indentLevel--;
                    printIndent(indentLevel);
                    System.out.println(c);
                    printIndent(indentLevel);
                } else if (c == ',' && !inQuotes) {
                    System.out.println(c);
                    printIndent(indentLevel);
                } else {
                    System.out.print(c);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void printIndent(int level) {
        for (int i = 0; i < level; i++) {
            System.out.print("    ");
        }
    }
}