package com.gaoql;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.gaoql.customview.SearchView;


public class SearchActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SearchView s = new SearchView(this);
        setContentView(s);
    }
}
