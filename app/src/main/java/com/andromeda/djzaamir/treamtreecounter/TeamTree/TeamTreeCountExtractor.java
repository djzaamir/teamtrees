package com.andromeda.djzaamir.treamtreecounter.TeamTree;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TeamTreeCountExtractor implements Runnable {

    private boolean teamtree_connect_error;
    private onTreeCountUpdate onTreeCountUpdateCallback;


    private boolean keep_running;


    public TeamTreeCountExtractor(onTreeCountUpdate onTreeCountUpdateCallback){
        this.onTreeCountUpdateCallback =  onTreeCountUpdateCallback;
        keep_running = true;

    }

    //Core Bootloader Function
    private void goForLaunch() {
        while (keep_running){

                //Load TeamTree Website

                String teamTrees_site = "https://teamtrees.org/";
                StringBuilder http_response = new StringBuilder();

                try {
                    URL url =  new URL(teamTrees_site);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.connect();

                    //Read response byte by byte IF HTTP_OK
                    if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        teamtree_connect_error = false;
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        String line;
                        while ((line = bufferedReader.readLine()) != null) {
                            http_response.append(line);
                        }
                    }else{
                        teamtree_connect_error = true;
                    }
                } catch (MalformedURLException e) {
                    teamtree_connect_error = true;
                } catch (IOException e) {
                    teamtree_connect_error = true;
                }

            //Parse and get tree-count
            String tree_count  =  customTreecountParserFromHtml(http_response.toString());
            if (!teamtree_connect_error){

                int int_representation = Integer.valueOf(tree_count);
                final int animation_count = 20;

                for (int i = animation_count; i >= 2; i--) {
                    String animation_value = prettyFormatTreeCount(String.valueOf(int_representation / i));
                    this.onTreeCountUpdateCallback.TreeCountUpdate(animation_value);

                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                    }
                }
                
                this.onTreeCountUpdateCallback.TreeCountUpdate(prettyFormatTreeCount(tree_count));
            }
            else{
                this.onTreeCountUpdateCallback.TreeCountUpdate("404 :(");
            }

            //Sleep for 5 seconds
            try {
                Thread.sleep(4000);
            } catch (InterruptedException e) {
            }
        }

    }


    private String customTreecountParserFromHtml(String html){
        if (!teamtree_connect_error){

            //Try to roughly pinpoint area in html containing tree-count, by matching following lines
            String str_to_match_for = "<div id=\"totalTrees\" class=\"counter\" data-count=";

            //Rough-Area Starting Position
            int match_pos = html.indexOf(str_to_match_for);
            String extracted_treecount = "409 :(";
            if (match_pos > -1){

            //Further Narrow down tree-count search, We are getting closer to Numeric Part here
            int treecount_start_pos = match_pos + str_to_match_for.length();

            //Extract Rough tree count
            String potential_treecount_str = html.substring(treecount_start_pos, treecount_start_pos + 11);

            //Extract the Numeric Part Using Java Regex
            Pattern numeric_pattern =  Pattern.compile("\\d+");
            Matcher extracted_treecount_match = numeric_pattern.matcher(potential_treecount_str);
            extracted_treecount_match.find();
            extracted_treecount = potential_treecount_str.substring(extracted_treecount_match.start(),                                                                                                   extracted_treecount_match.end());
            }
            return extracted_treecount;
        }
        return null;
    }

    private String prettyFormatTreeCount(String extracted_treecount) {
        StringBuilder prettyFormattedTreeCount = new StringBuilder();
        int num_count = 0;
        for (int i = extracted_treecount.length() - 1; i >= 0 ; i--) {
            num_count++;
            prettyFormattedTreeCount.append(extracted_treecount.charAt(i));

            if (num_count == 3 && (i-1) >= 0){
                prettyFormattedTreeCount.append(",");
                num_count = 0;
            }
        }
        return prettyFormattedTreeCount.reverse().toString();
    }

    public void setKeep_running(boolean keep_running) {
        this.keep_running = keep_running;
    }

    @Override
    public void run() {
        goForLaunch();
    }
}
