package com.andromeda.djzaamir.treamtreecounter;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import com.andromeda.djzaamir.treamtreecounter.TeamTree.TeamTreeCountExtractor;
import com.andromeda.djzaamir.treamtreecounter.TeamTree.onTreeCountUpdate;

public class MainActivity extends AppCompatActivity {

    //Globals
    private TeamTreeCountExtractor teamTreeCountExtractor;
    private TextView tree_count_TextView,donation_textview;
    private Thread background_tree_count_thread;
    private Handler guiHandler;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        guiHandler = new Handler();
        tree_count_TextView = (TextView) findViewById(R.id.tree_count);
        donation_textview   = (TextView) findViewById(R.id.donation_textview);

        donation_textview.setMovementMethod(LinkMovementMethod.getInstance());

    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
         //Bootloader method
        goForLaunch();
    }

    //BootLoader Method
    private void goForLaunch() {
        tree_count_TextView.setText("0");

        //Main Tree count Module Init
        teamTreeCountExtractor =  new TeamTreeCountExtractor(new onTreeCountUpdate() {
            @Override
            public void TreeCountUpdate(String tree_count) {
                updateTreecountTextview(tree_count);
            }
        });


        //Branch it out to separate worker thread
        background_tree_count_thread = new Thread(teamTreeCountExtractor);
        background_tree_count_thread.start();
    }

    private void updateTreecountTextview(final String tree_count){
        guiHandler.post(new Runnable() {
            @Override
            public void run() {
                tree_count_TextView.setText(tree_count);
            }
        });
    }



    //When anot
    @Override
    protected void onPause() {
        super.onPause();
       kill_background_thread();
    }

    @Override
    protected void onStop() {
        super.onStop();
        kill_background_thread();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        kill_background_thread();
    }

    private void kill_background_thread(){
        try {
            teamTreeCountExtractor.setKeep_running(false);
            background_tree_count_thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
