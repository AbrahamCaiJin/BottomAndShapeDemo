package cn.gzzgtech.bottomandshapedemo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import cn.gzzgtech.bottomandshapedemo.adapter.MainAdapter;
import cn.gzzgtech.bottomandshapedemo.R;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {


    @BindView(R.id.main_rv)
    RecyclerView mainRv;
    Unbinder unbinder;
    MainAdapter mainAdapter;
    String[] strings;
    List<String> lists;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        unbinder = ButterKnife.bind(this);
        inint();

    }

    private void inint() {
        lists= new ArrayList<>();
        strings = getResources().getStringArray(R.array.item_main_title);
        for(String string : strings){
            lists.add(string);
        }
        mainAdapter = new MainAdapter(lists);
        mainRv.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL,false));
        mainRv.setAdapter(mainAdapter);
        mainAdapter.setOnItemClickListener(new MainAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                switch (position){
                    case 0:
                        startActivity(new Intent(getApplicationContext(),LinearLayoutActivity.class));
                    break;
                    case 1:
                        startActivity(new Intent(getApplicationContext(),BottomNavigationActivity.class));
                        break;
                }
            }
        });

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        //解除绑定
        unbinder.unbind();
    }

}
