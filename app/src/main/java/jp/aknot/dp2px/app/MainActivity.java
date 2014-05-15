package jp.aknot.dp2px.app;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;


public class MainActivity extends Activity implements ActionBar.TabListener {

    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        ViewPagerAdapter adapter = new ViewPagerAdapter(getFragmentManager());

        viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(adapter);

        for (int i = 0; i < adapter.getCount(); i++) {
            actionBar.addTab(actionBar.newTab()
                    .setText(adapter.getPageTitle(i))
                    .setTabListener(this));
        }

        viewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
        viewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
    }

    public class ViewPagerAdapter extends FragmentPagerAdapter {
        public ViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                return SimpleFragment.getInstance(ConvertType.DP2PX);
            } else if (position == 1) {
                return SimpleFragment.getInstance(ConvertType.PX2DP);
            }
            return null;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (position == 0) {
                return getString(R.string.tab_title_section1);
            } else if (position == 1) {
                return getString(R.string.tab_title_section2);
            }
            return null;
        }
    }

    private static enum ConvertType {
        DP2PX(0, "dp", "px"),
        PX2DP(1, "px", "dp")
        ;

        private final int position;
        private final String beforeUnit;
        private final String afterUnit;

        ConvertType(int position, String beforeUnit, String afterUnit) {
            this.position = position;
            this.beforeUnit = beforeUnit;
            this.afterUnit = afterUnit;
        }
    }

    private static enum Density {
        LDPI("ldpi", 120),
        MDPI("mdpi", 160),
        TVDPI("tvdpi", 213),
        HDPI("hdpi", 240),
        XHDPI("xhdpi", 320),
        XXHDPI("xxhdpi", 480),
        XXXHDPI("xxxhdpi", 640)
        ;

        private final String label;
        private final int value;

        private Density(String label, int value) {
            this.label = label;
            this.value = value;
        }

        public static int dp2px(Density density, int dp) {
            float scale = (float) density.value / (float) MDPI.value;
            return (int) ((float) dp  * scale + 0.5f);
        }

        public static int px2dp(Density density, int px) {
            float scale = (float) density.value / (float) MDPI.value;
            return (int) ((float) px / scale + 0.5f);
        }
    }

    private static final String ARGS_CONVERT_TYPE = "convert_type";

    public static class SimpleFragment extends Fragment {
        private TextView tvInputDisplay;

        public SimpleFragment() {
        }

        public static SimpleFragment getInstance(ConvertType convertType) {
            SimpleFragment fragment = new SimpleFragment();
            Bundle args = new Bundle();
            args.putSerializable(ARGS_CONVERT_TYPE, convertType);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            final View rootView = inflater.inflate(R.layout.fragment_main, container, false);

            final ConvertType convertType = (ConvertType) getArguments().getSerializable(ARGS_CONVERT_TYPE);

            tvInputDisplay = (TextView) rootView.findViewById(R.id.tv_display);
            GridView gridView = (GridView) rootView.findViewById(R.id.grid_view);

            ViewData[] list =  new ViewData[] {
                    new ViewData("7", 7.0f),
                    new ViewData("8", 8.0f),
                    new ViewData("9", 9.0f),
                    new ViewData("4", 4.0f),
                    new ViewData("5", 5.0f),
                    new ViewData("6", 6.0f),
                    new ViewData("1", 1.0f),
                    new ViewData("2", 2.0f),
                    new ViewData("3", 3.0f),
                    new ViewData("0", 0.0f),
                    new ViewData("00", 0.0f),
                    new ViewData("C", 0.0f),
            };
            gridView.setAdapter(new MyAdapter(getActivity(), R.layout.grid_item_number, R.layout.grid_item_action, list));

            ((TextView) rootView.findViewById(R.id.tv_display_unit)).setText(convertType.beforeUnit);

            Button btnConvert = (Button) rootView.findViewById(R.id.btn_convert);
            btnConvert.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String input = tvInputDisplay.getText().toString();
                    if (!TextUtils.isEmpty(input)) {
                        ((TextView) rootView.findViewById(R.id.calc_result_description))
                                .setText(String.format(Locale.getDefault(),
                                        getResources().getString(R.string.calc_result_description),
                                        input, convertType.beforeUnit, convertType.afterUnit));

                        Set<Density> keySet = layoutMap.keySet();
                        for (Density density : keySet) {
                            int result = 0;
                            switch (convertType) {
                                case DP2PX:
                                    result = Density.dp2px(density, Integer.valueOf(input));
                                    break;
                                case PX2DP:
                                    result = Density.px2dp(density, Integer.valueOf(input));
                                    break;
                            }
                            View view = rootView.findViewById(layoutMap.get(density));
                            ((TextView) view.findViewById(R.id.tv_label)).setText(density.label);
                            ((TextView) view.findViewById(R.id.tv_value)).setText(String.valueOf(result));
                        }


                        AlphaAnimation animation = new AlphaAnimation(0, 1);
                        animation.setDuration(2000);

                        rootView.findViewById(R.id.calc_result_description).setVisibility(View.VISIBLE);
                        rootView.findViewById(R.id.result_area).setVisibility(View.VISIBLE);

                        rootView.findViewById(R.id.calc_result_description).startAnimation(animation);
                        rootView.findViewById(R.id.result_area).startAnimation(animation);
                    }
                }
            });
            initCalcResultView(rootView, convertType);

            return rootView;
        }

        Map<Density, Integer> layoutMap = new HashMap<Density, Integer>();

        private void initCalcResultView(View rootView, ConvertType convertType) {
            layoutMap.put(Density.LDPI, R.id.ldpi);
            layoutMap.put(Density.MDPI, R.id.mdpi);
            layoutMap.put(Density.TVDPI, R.id.tvdpi);
            layoutMap.put(Density.HDPI, R.id.hdpi);
            layoutMap.put(Density.XHDPI, R.id.xhdpi);
            layoutMap.put(Density.XXHDPI, R.id.xxhdpi);
            layoutMap.put(Density.XXXHDPI, R.id.xxxhdpi);

            Set<Density> keySet = layoutMap.keySet();
            for (Density density : keySet) {
                View view = rootView.findViewById(layoutMap.get(density));
                ((TextView) view.findViewById(R.id.tv_label)).setText(density.label);
                ((TextView) view.findViewById(R.id.tv_value)).setText(null);
                ((TextView) view.findViewById(R.id.tv_unit)).setText(convertType.afterUnit);
            }

            rootView.findViewById(R.id.calc_result_description).setVisibility(View.GONE);
            rootView.findViewById(R.id.result_area).setVisibility(View.GONE);
        }

        private class ViewData {
            private final String label;
            private final float number;
            private ViewData(String label, float number) {
                this.label = label;
                this.number = number;
            }
        }

        static class ViewHolder {
            TextView textView;
        }

        private class MyAdapter extends ArrayAdapter<ViewData> {
            private LayoutInflater inflater;
            private int numberLayoutResId;
            private int actionLayoutResId;
            public MyAdapter(Context context, int numberLayoutResId, int actionLayoutResId, ViewData[] objects) {
                super(context, numberLayoutResId, objects);
                this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                this.numberLayoutResId = numberLayoutResId;
                this.actionLayoutResId = actionLayoutResId;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                ViewHolder holder;

                ViewData viewData = getItem(position);

                if (convertView == null) {
                    if (viewData.label.equals("C")) {
                        convertView = inflater.inflate(actionLayoutResId, parent, false);
                    } else {
                        convertView = inflater.inflate(numberLayoutResId, parent, false);
                    }

                    holder = new ViewHolder();
                    holder.textView = (TextView) convertView.findViewById(R.id.tv_number);
                    convertView.setTag(holder);
                    holder.textView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String selectedLabel = ((TextView) v).getText().toString();
                            String updatedLabel;
                            if (selectedLabel.equals("C")) {
                                updatedLabel = "";
                            } else {
                                updatedLabel = tvInputDisplay.getText().toString() + selectedLabel;
                            }
                            tvInputDisplay.setText(updatedLabel);
                        }
                    });
                } else {
                    holder = (ViewHolder) convertView.getTag();
                }


                holder.textView.setText(viewData.label);

                return convertView;
            }
        }
    }
}
