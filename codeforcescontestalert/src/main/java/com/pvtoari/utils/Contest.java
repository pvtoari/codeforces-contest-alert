package com.pvtoari.utils;

import java.util.ArrayList;
import java.util.Date;

import com.vdurmont.emoji.EmojiParser;

public class Contest implements Comparable<Contest> {
    private int id;
    private String name, type, phase;
    private boolean frozen;
    private long durationSeconds, startTimeSeconds, relativeTimeSeconds;

    private static final String TROPHY = EmojiParser.parseToUnicode(":trophy:");
    private static final String NAME_BADGE = EmojiParser.parseToUnicode(":name_badge:");
    private static final String LABEL = EmojiParser.parseToUnicode(":label:");
    private static final String HOURGLASS = EmojiParser.parseToUnicode(":hourglass:");
    private static final String SNOWFLAKE = EmojiParser.parseToUnicode(":snowflake:");
    private static final String CLOCK1 = EmojiParser.parseToUnicode(":clock1:");
    private static final String CALENDAR = EmojiParser.parseToUnicode(":calendar:");
    private static final String CHECK_MARK_BUTTON = EmojiParser.parseToUnicode(":check_mark_button:");
    private static final String CROSS_MARK = EmojiParser.parseToUnicode(":x:");
    
    public Contest() {
        this.id = 0;
        this.name = "";
        this.type = "";
        this.phase = "";
        this.frozen = false;
        this.durationSeconds = 0;
        this.startTimeSeconds = 0;
        this.relativeTimeSeconds = 0;
    }

    public Contest(int id, String name, String type, String phase, boolean frozen, long durationSeconds, long startTimeSeconds, long relativeTimeSeconds) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.phase = phase;
        this.frozen = frozen;
        this.durationSeconds = durationSeconds;
        this.startTimeSeconds = startTimeSeconds;
        this.relativeTimeSeconds = relativeTimeSeconds;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPhase() {
        return phase;
    }

    public void setPhase(String phase) {
        this.phase = phase;
    }

    public boolean isFrozen() {
        return frozen;
    }

    public void setFrozen(boolean frozen) {
        this.frozen = frozen;
    }

    public long getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(long durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public long getStartTimeSeconds() {
        return startTimeSeconds;
    }

    public void setStartTimeSeconds(long startTimeSeconds) {
        this.startTimeSeconds = startTimeSeconds;
    }

    public long getRelativeTimeSeconds() {
        return relativeTimeSeconds;
    }

    public void setRelativeTimeSeconds(long relativeTimeSeconds) {
        this.relativeTimeSeconds = relativeTimeSeconds;
    }

    @Override
    public String toString() {
        return "Contest [id=" + id + ", name=" + name + ", type=" + type + ", phase=" + phase + ", frozen=" + frozen
                + ", durationSeconds=" + durationSeconds + ", startTimeSeconds=" + startTimeSeconds
                + ", relativeTimeSeconds=" + relativeTimeSeconds + "]";
    }

    @Override
    public int compareTo(Contest o) {
        return (int) (this.relativeTimeSeconds - o.relativeTimeSeconds);
    }

    public boolean equals(Contest o) {
        return this.id == o.id;
    }

    public Date getDateTime() {
        return new Date(startTimeSeconds * 1000);
    }

    public static boolean arraysEqual(Contest[] a, Contest[] b) {
        if(a.length != b.length) return false;

        for(int i = 0; i < a.length; i++) {
            if(!a[i].equals(b[i])) return false;
        }

        return true;
    }

    public static ArrayList<Contest> arraysDiff(Contest[] a, Contest[] b) {
        ArrayList<Contest> diff = new ArrayList<>();

        for (Contest contestB : b) {
            boolean found = false;
            for (Contest contestA : a) {
                if (contestB.equals(contestA)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                diff.add(contestB);
            }
        }

        return diff;
    }

    public static Contest[] parseRawFilteredData(String rawData) {
        /*
         * rawData is a string that contains the data of the contests in json format
         * in the form:
         * {"id":2003,
         * "name":"Codeforces Round (Div. 2)",
         * "type":"CF",
         * "phase":"BEFORE",
         * "frozen":false,
         * "durationSeconds":7200,
         * "startTimeSeconds":1724596500,
         * "relativeTimeSeconds":-1549316},
         * ...
         * and it might return wrong data if the format is not the same as the one above
         * in general the major part of the code in this project works with specific format from codeforces api so its not cool to use these methods with other data
         */


        // if u are reading this and also took a look at all my project, then u might wonder why i am commenting this code with such precision
        // well, the main reason is because this might be the method/class/stuff that might be the most complex and not understandable of all my project
        // so i am trying to make it as clear as possible for me and for you, the reader, i love u

        // cux this is ur fault

        if(rawData.equals("fail")) {
            Tracer.log(Tracer.HIGH_RISK, "Filtered data parsing failed. Aborting formatted content generation.");
            return null;
        }

        Contest[] res = null;

        String[] firstSplitting = rawData.split("\\{");
        for(int i = 0; i < firstSplitting.length; i++) {
            if(i==firstSplitting.length-1){ // if it's the last element, we don't want the '}' character
                firstSplitting[i] = firstSplitting[i].replace("}", "");
                continue;
            }
            firstSplitting[i] = firstSplitting[i].replace("},", ""); // else, we want to remove the '},' char sequence
        }

        // now that we have each contest in a different element of the array, we split each contest by the ',' character
        String[][] secondSplitting = new String[firstSplitting.length][7]; // seven cuz we have seven attributes, if codeforces changes their api it im fucked up
        for(int i = 0; i < firstSplitting.length; i++) {
            secondSplitting[i] = firstSplitting[i].split(","); // the array2d's elements are arrays whose elements will be the elements of the first splitting splitted by commas
        }

        // when we are done, we will clean up the residues from json fields and stuff to perform a better parsing
        for(int i = 1; i < secondSplitting.length; i++) { // index one cuz idk why my algorithm generates an index 0 with empty data 
            String[] contentToBeParsed = secondSplitting[i]; // that is, we get every array contained in secondSplitting and for its elements we clear up things
            for(int j = 0; j < contentToBeParsed.length; j++) {
                contentToBeParsed[j] = contentToBeParsed[j].substring(contentToBeParsed[j].indexOf(":")+1); // we obtain the index of ':' so we can get a substring that only contains the value of that json field
                contentToBeParsed[j] = contentToBeParsed[j].replace("\"", ""); // if that cleared value substring was e.g. a string field, then we'll have to remove quotes and stuff, otherwise do nothing
                contentToBeParsed[j] = contentToBeParsed[j].trim(); // using this since for some elements an large amount of spaces are generated and parsing goes crazy
            }
        }

        res = new Contest[secondSplitting.length-1]; // -1 cuz we have an empty element at the beginning of the array
        for(int i = 1; i < secondSplitting.length; i++) {
            Contest element = new Contest();
            element.setId(Integer.parseInt(secondSplitting[i][0]));
            element.setName(secondSplitting[i][1]);
            element.setType(secondSplitting[i][2]);
            element.setPhase(secondSplitting[i][3]);
            element.setFrozen(Boolean.parseBoolean(secondSplitting[i][4]));
            element.setDurationSeconds(Long.parseLong(secondSplitting[i][5]));
            element.setStartTimeSeconds(Long.parseLong(secondSplitting[i][6]));
            element.setRelativeTimeSeconds(Long.parseLong(secondSplitting[i][7].replace("\"", ""))); // added replace method for a bug fix when parsing

            res[i-1] = element;
        }

        return res;
    }

    public String getFormattedMessageContent() {
        return TROPHY + " Contest " + id + " " + TROPHY + "\n\n"
            + NAME_BADGE + " Name: " + name + "\n"
            + LABEL + " Type: " + type + "\n"
            + HOURGLASS + " Phase: " + phase + "\n"
            + SNOWFLAKE + " Frozen: " + (frozen ? CHECK_MARK_BUTTON : CROSS_MARK) + "\n"
            + CLOCK1 + " Duration: " + formatSeconds(durationSeconds) + "\n"
            + CALENDAR + " Start Time: " + getDateTime().toString() + "\n";
    }

    private String formatSeconds(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;

        return hours + "h " + minutes + "m " + secs + "s";
    }
}