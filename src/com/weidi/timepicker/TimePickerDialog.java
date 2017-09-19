package com.weidi.timepicker;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.app.DialogFragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.weidi.library.R;
import com.weidi.timepicker.config.PickerConfig;
import com.weidi.timepicker.data.Type;
import com.weidi.timepicker.data.WheelCalendar;
import com.weidi.timepicker.listener.OnDateSetListener;

import java.util.Calendar;


/**
 * Created by jzxiang on 16/4/19.
 */
public class TimePickerDialog extends DialogFragment implements View.OnClickListener {

    private static final String TAG = "TimePickerDialog";
    private static final int COLOR = 0XFFE60012;
    private static final String TITLE = "TimePicker";
    private static final String SURE = "确定";
    private static final String CANCEL = "取消";
    private static final String YEAR = "年";
    private static final String MONTH = "月";
    private static final String DAY = "日";
    private static final String HOUR = "时";
    private static final String MINUTE = "分";

    private Activity mActivity;
    private PickerConfig mPickerConfig;
    private TimeWheel mTimeWheel;
    private long mCurrentMillSeconds;
    // private OnDateSetListener mOnDateSetListener;

    /*public interface OnDateSetListener {

        void onDateSet(TimePickerDialog timePickerView, long millseconds);

    }*/

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Activity activity = getActivity();
        mActivity.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = new Dialog(getActivity(), R.style.Dialog_NoTitle);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(initView());
        return dialog;
    }

    @Override
    public void onResume() {
        super.onResume();
        Window window = getDialog().getWindow();
        //        int height = getResources().getDimensionPixelSize(R.dimen.picker_height);
        //        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, height);//Here!
        window.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);//Here!
        window.setGravity(Gravity.BOTTOM);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.tv_cancel) {
            dismiss();
        } else if (i == R.id.tv_sure) {
            sureClicked();
        }
    }

    /*public void setOnDateSetListener(OnDateSetListener listener){
        mOnDateSetListener = listener;
    }*/

    /*
    * @desc This method returns the current milliseconds. If current milliseconds is not set,
    *       this will return the system milliseconds.
    * @param none
    * @return long - the current milliseconds.
    */
    public long getCurrentMillSeconds() {
        if (mCurrentMillSeconds == 0)
            return System.currentTimeMillis();

        return mCurrentMillSeconds;
    }

    private View initView() {
        LayoutInflater inflater = LayoutInflater.from(mActivity);
        View view = inflater.inflate(R.layout.timepicker_layout, null);
        View toolbar = view.findViewById(R.id.toolbar);
        TextView title = (TextView) view.findViewById(R.id.tv_title);
        TextView sure = (TextView) view.findViewById(R.id.tv_sure);
        TextView cancel = (TextView) view.findViewById(R.id.tv_cancel);
        sure.setOnClickListener(this);
        cancel.setOnClickListener(this);

        toolbar.setBackgroundColor(COLOR);
        title.setText(TITLE);
        sure.setText(SURE);
        cancel.setText(CANCEL);

        mTimeWheel = new TimeWheel(view, mPickerConfig);
        return view;
    }

    private void initialize(PickerConfig pickerConfig) {
        mPickerConfig = pickerConfig;
    }

    private static TimePickerDialog newIntance(PickerConfig pickerConfig) {
        TimePickerDialog timePickerDialog = new TimePickerDialog();
        timePickerDialog.initialize(pickerConfig);
        return timePickerDialog;
    }

    /*
    * @desc This method is called when onClick method is invoked by sure button. A Calendar
    * instance is created and
    *       initialized. 
    * @param none
    * @return none
    */
    private void sureClicked() {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();

        calendar.set(Calendar.YEAR, mTimeWheel.getCurrentYear());
        calendar.set(Calendar.MONTH, mTimeWheel.getCurrentMonth() - 1);
        calendar.set(Calendar.DAY_OF_MONTH, mTimeWheel.getCurrentDay());
        calendar.set(Calendar.HOUR_OF_DAY, mTimeWheel.getCurrentHour());
        calendar.set(Calendar.MINUTE, mTimeWheel.getCurrentMinute());

        mCurrentMillSeconds = calendar.getTimeInMillis();
        if (mPickerConfig.mCallBack != null) {
            mPickerConfig.mCallBack.onDateSet(this, mCurrentMillSeconds);
        }
        dismiss();
    }

    public static class Builder {
        PickerConfig mPickerConfig;

        public Builder() {
            mPickerConfig = new PickerConfig();
        }

        public Builder setType(Type type) {
            mPickerConfig.mType = type;
            return this;
        }

        public Builder setThemeColor(int color) {
            mPickerConfig.mThemeColor = color;
            return this;
        }

        public Builder setCancelStringId(String left) {
            mPickerConfig.mCancelString = left;
            return this;
        }

        public Builder setSureStringId(String right) {
            mPickerConfig.mSureString = right;
            return this;
        }

        public Builder setTitleStringId(String title) {
            mPickerConfig.mTitleString = title;
            return this;
        }

        public Builder setToolBarTextColor(int color) {
            mPickerConfig.mToolBarTVColor = color;
            return this;
        }

        public Builder setWheelItemTextNormalColor(int color) {
            mPickerConfig.mWheelTVNormalColor = color;
            return this;
        }

        public Builder setWheelItemTextSelectorColor(int color) {
            mPickerConfig.mWheelTVSelectorColor = color;
            return this;
        }

        public Builder setWheelItemTextSize(int size) {
            mPickerConfig.mWheelTVSize = size;
            return this;
        }

        public Builder setCyclic(boolean cyclic) {
            mPickerConfig.cyclic = cyclic;
            return this;
        }

        public Builder setMinMillseconds(long millseconds) {
            mPickerConfig.mMinCalendar = new WheelCalendar(millseconds);
            return this;
        }

        public Builder setMaxMillseconds(long millseconds) {
            mPickerConfig.mMaxCalendar = new WheelCalendar(millseconds);
            return this;
        }

        public Builder setCurrentMillseconds(long millseconds) {
            mPickerConfig.mCurrentCalendar = new WheelCalendar(millseconds);
            return this;
        }

        public Builder setYearText(String year) {
            mPickerConfig.mYear = year;
            return this;
        }

        public Builder setMonthText(String month) {
            mPickerConfig.mMonth = month;
            return this;
        }

        public Builder setDayText(String day) {
            mPickerConfig.mDay = day;
            return this;
        }

        public Builder setHourText(String hour) {
            mPickerConfig.mHour = hour;
            return this;
        }

        public Builder setMinuteText(String minute) {
            mPickerConfig.mMinute = minute;
            return this;
        }

        public Builder setCallBack(OnDateSetListener listener) {
            mPickerConfig.mCallBack = (com.weidi.timepicker.listener.OnDateSetListener) listener;
            return this;
        }

        public TimePickerDialog build() {
            return newIntance(mPickerConfig);
        }

    }

}
