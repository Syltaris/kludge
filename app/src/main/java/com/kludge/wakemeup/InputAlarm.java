package com.kludge.wakemeup;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.Set;

public class InputAlarm extends AppCompatActivity {

    long alarmId;
    SharedPreferences sharedPrefs;
    SharedPreferences.OnSharedPreferenceChangeListener prefListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_alarm);


        //sets up Preferences fragment
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        SettingsFragment settingsFragment = new SettingsFragment();
        fragmentTransaction.add(android.R.id.content, settingsFragment, "SETTINGS_FRAGMENT");
        fragmentTransaction.commit();

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        alarmId = getIntent().getLongExtra("alarmId", -1);

        // initialise preferences if editing existing alarm
        if (alarmId != -1){

            SharedPreferences.Editor editor = sharedPrefs.edit();
            AlarmDetails alarm = AlarmLab.get(getBaseContext()).getAlarmDetails(alarmId);

            editor.putString("preference_alarm_name", alarm.getName());
            editor.putInt("preference_alarm_hour", alarm.getHour());
            editor.putInt("preference_alarm_minute", alarm.getMin());
            editor.putBoolean("preference_alarm_repeat", alarm.isRepeat());
            editor.putString("preference_snooze_duration", ""+alarm.getnSnooze());
            editor.putString("preference_alarm_ringtone", alarm.getRingtone());

            editor.apply();
        }

        /* todo:fix updating summary
        //update time text on alarm preference
        prefListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                Log.i("changed"," called");
                switch(key){
                    case "preference_alarm_name":
                        EditTextPreference prefName = (EditTextPreference) findPreference(key);
                        prefName.setSummary("Name: "+sharedPreferences.getString("preference_alarm_name", "New Alarm"));
                        break;
                    case "preference_alarm_time":
                        com.kludge.wakemeup.TimePreference prefTime = (com.kludge.wakemeup.TimePreference) findPreference(key);
                        prefTime.setSummary("Time set: "+sharedPreferences.getInt("preference_alarm_hour",0)+":"+sharedPreferences.getInt("preference_alarm_minute", 0));
                        break;
                    case "preference_alarm_snooze":
                        ListPreference prefSnooze = (ListPreference) findPreference(key) ;
                        prefSnooze.setSummary("Snooze duration: "+sharedPreferences.getInt("preference_snooze_duration", 0)+" minutes");
                        break;
                }
            }
        };

        sharedPrefs.registerOnSharedPreferenceChangeListener(prefListener);

        /*
        TimePicker viewTimePicker = ((TimePicker) findViewById(R.id.time_picker));
        assert viewTimePicker != null;
        viewTimePicker.setIs24HourView(true);

        TextView buttonSave = (TextView) findViewById(R.id.butt_add_alarm);
        buttonSave.setText("Add");

        */
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_input_alarm, menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch(item.getItemId()){
            case R.id.menu_done:
                sendAlarm();
            default:
                return true;
        }
    }

    public static class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener{

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.alarm_preferences);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View v = super.onCreateView(inflater, container, savedInstanceState);

            return v;
        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            super.onPause();
            getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            Log.i("changed"," called");
            switch(key){
                case "preference_alarm_name":
                    EditTextPreference prefName = (EditTextPreference) findPreference(key);
                    prefName.setSummary("Name: "+sharedPreferences.getString("preference_alarm_name", "New Alarm"));
                    break;
                case "preference_alarm_time":
                    com.kludge.wakemeup.TimePreference prefTime = (com.kludge.wakemeup.TimePreference) findPreference(key);
                    prefTime.setSummary("Time set: "+sharedPreferences.getInt("preference_alarm_hour",0)+":"+sharedPreferences.getInt("preference_alarm_minute", 0));
                    break;
                case "preference_alarm_snooze":
                    ListPreference prefSnooze = (ListPreference) findPreference(key) ;
                    prefSnooze.setSummary("Snooze duration: "+sharedPreferences.getInt("preference_snooze_duration", 0)+" minutes");
                    break;

            }
        }
    }



    private void sendAlarm(){

        //update sharedPrefs here before putting in
        //prefEditor.putString("preference_alarm_name", "Alarm Name");
        //prefEditor.putBoolean("preference_alarm_repeat", false);

        //prefEditor.apply();
        /*
        EditText viewAlarmName = ((EditText) findViewById(R.id.alarm_name));
        assert viewAlarmName != null;
        String alarmName = viewAlarmName.getText().toString();

        TimePicker viewTimePicker = ((TimePicker) findViewById(R.id.time_picker));
        assert viewTimePicker != null;

        */

        SharedPreferences.Editor prefEditor = sharedPrefs.edit();

        alarmId = getIntent().getLongExtra("alarmId", -1);

        Intent data = new Intent();
        data.putExtra("alarm_name", sharedPrefs.getString("preference_alarm_name", "Alamo Name"));
        data.putExtra("hour", sharedPrefs.getInt("preference_alarm_hour", 0));
        data.putExtra("minute", sharedPrefs.getInt("preference_alarm_minute", 0));
        data.putExtra("repeat", sharedPrefs.getBoolean("preference_alarm_repeat", false));
        data.putExtra("snooze", Integer.parseInt(sharedPrefs.getString("preference_snooze_duration", "1")));
        data.putExtra("ringtone", sharedPrefs.getString("preference_alarm_ringtone", "Empty"));


        data.putExtra("alarmId", alarmId); //alarmId can be -1, which means new alarm

        setResult(MainAlarm.RESULT_OK, data);

        prefEditor.clear();
        prefEditor.apply();

        finish();
    }
}
