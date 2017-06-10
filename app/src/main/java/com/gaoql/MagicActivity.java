package com.gaoql;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gaoql.customview.DividerGridItemDecoration;
import com.gaoql.customview.RadianViewPager;

import java.util.ArrayList;
import java.util.List;

public class MagicActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    RadianViewPager radianViewPager;
    private List<String> datas = new ArrayList<>();
    RvAdapter rvAdapter =  new RvAdapter();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_magic);
        initData();
        recyclerView = (RecyclerView)findViewById(R.id.rv);
        radianViewPager =(RadianViewPager)findViewById(R.id.vp);
        recyclerView.setLayoutManager(new GridLayoutManager(this,2));
        recyclerView.setAdapter(rvAdapter);
        radianViewPager.setOnPagerChangeListener(new RadianViewPager.OnPagerChangeListener() {
            @Override
            public void pageChanged(int from, int to) {
                changeData(from,to);
                rvAdapter.notifyItemRangeChanged(0,4);
            }
        });
    }

    private void initData(){
        datas.add("春");
        datas.add("夏");
        datas.add("秋");
        datas.add("冬");
    }

    private void changeData(int from,int to){
        datas.clear();
        datas.add("春"+from+to);
        datas.add("夏"+from+to);
        datas.add("秋"+from+to);
        datas.add("冬"+from+to);

    }
    class RvAdapter extends RecyclerView.Adapter<MagicActivity.MyViewHolder>{

        @Override
        public MagicActivity.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            MagicActivity.MyViewHolder myViewHolder = new MagicActivity.MyViewHolder(LayoutInflater.from(MagicActivity.this).inflate(R.layout.layout_content_item,parent,false));
            return myViewHolder;
        }

        @Override
        public void onBindViewHolder(MagicActivity.MyViewHolder holder, int position) {

            holder.textView.setText(datas.get(position));
        }

        @Override
        public int getItemCount() {
            return datas.size();
        }
    }

    class MyViewHolder extends RecyclerView.ViewHolder{
        TextView textView;
        public MyViewHolder(View itemView) {
            super(itemView);
            textView= (TextView) itemView.findViewById(R.id.content_text);
        }
    }
}
