package cn.gzzgtech.bottomandshapedemo.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import cn.gzzgtech.bottomandshapedemo.R;
import cn.gzzgtech.bottomandshapedemo.fragment.Fragment1;
import cn.gzzgtech.bottomandshapedemo.fragment.Fragment2;
import cn.gzzgtech.bottomandshapedemo.fragment.Fragment3;

import java.util.ArrayList;
import java.util.List;

public class LinearLayoutActivity extends AppCompatActivity {

    @BindView(R.id.fl_fragment)
    FrameLayout flFragment;
    @BindView(R.id.iv1)
    ImageView iv1;
    @BindView(R.id.tv1)
    TextView tv1;
    @BindView(R.id.ll_tab1)
    LinearLayout llTab1;
    @BindView(R.id.iv2)
    ImageView iv2;
    @BindView(R.id.tv2)
    TextView tv2;
    @BindView(R.id.ll_tab2)
    LinearLayout llTab2;
    @BindView(R.id.iv3)
    ImageView iv3;
    @BindView(R.id.tv3)
    TextView tv3;
    @BindView(R.id.ll_tab3)
    LinearLayout llTab3;

    Unbinder unbinder;
    Fragment1 fragment1;
    Fragment2 fragment2;
    Fragment3 fragment3;
    private Fragment fragment_now = null;
//    private List<View> textViews;
    private List<ImageView> iv_list;
    private List<TextView> tv_list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bottom_linearlayout);
        unbinder = ButterKnife.bind(this);
        inint();

    }


    private void inint() {
        iv_list = new ArrayList<>();
        tv_list = new ArrayList<>();

        iv_list.add(iv1);
        iv_list.add(iv2);
        iv_list.add(iv3);

        tv_list.add(tv1);
        tv_list.add(tv2);
        tv_list.add(tv3);

//        textViews[0] = tv1;
//        textViews[1] = tv2;
//        textViews[2] = tv3;

        changePageSelect(0);
        changePageFragment(R.id.ll_tab1);

    }

    @OnClick({R.id.iv1, R.id.ll_tab1, R.id.iv2, R.id.ll_tab2, R.id.iv3, R.id.ll_tab3})
    public void onViewClicked(View view) {
        changePageFragment(view.getId());

    }

    /**
     * 选中的tab 和 没有选中的tab 的图标和字体颜色
     *
     * @param index
     */
    public void changePageSelect(int index) {
        for (int i = 0; i < iv_list.size(); i++) {
            if (index == i) {
                iv_list.get(i).setEnabled(false);
                tv_list.get(i).setTextColor(getResources().getColor(R.color.colorLightRed));
            } else {
                iv_list.get(i).setEnabled(true);
                tv_list.get(i).setTextColor(getResources().getColor(R.color.colorTextGrey));
            }
        }
    }

    /**
     * 当点击导航栏时改变 fragment
     *
     * @param id
     */
    public void changePageFragment(int id) {
        switch (id) {
            case R.id.ll_tab1:
            case R.id.iv1:
                if (fragment1 == null) {//减少new fragmnet,避免不必要的内存消耗
                    fragment1 = Fragment1.newInstance();
                }
                changePageSelect(0);
                switchFragment(fragment_now, fragment1);
                break;
            case R.id.ll_tab2:
            case R.id.iv2:
                if (fragment2 == null) {
                    fragment2 = Fragment2.newInstance();
                }
                changePageSelect(1);
                switchFragment(fragment_now, fragment2);
                break;
            case R.id.ll_tab3:
            case R.id.iv3:
                if (fragment3 == null) {
                    fragment3 = Fragment3.newInstance();
                }
                changePageSelect(2);
                switchFragment(fragment_now, fragment3);
                break;
        }
    }

    /**
     * 隐藏显示fragment
     *
     * @param from 需要隐藏的fragment
     * @param to   需要显示的fragment
     */
    public void switchFragment(Fragment from, Fragment to) {
        if (to == null)
            return;
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (!to.isAdded()) {
            if (from == null) {
                transaction.add(R.id.fl_fragment, to).show(to).commit();
            } else {
                // 隐藏当前的fragment，add下一个fragment到Activity中并显示
                transaction.hide(from).add(R.id.fl_fragment, to).show(to).commitAllowingStateLoss();
            }
        } else {
            // 隐藏当前的fragment，显示下一个
            transaction.hide(from).show(to).commit();
        }
        fragment_now = to;

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
