package com.kludge.wakemeup;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.ArrayList;
import java.util.Calendar;

public class MainAlarm extends AppCompatActivity {

    //keys
    static final int ID_ADD_ALARM = 100;
    static final int ID_RESULT_OK = 666;

    static final int ID_CONTEXT_EDIT = 200;
    static final int ID_CONTEXT_DELETE= 201;

    static ArrayList<AlarmDetails> alarms; //array containing DESCRIPTION OF ALARMS? !!!! MUST IT BE STATIC???
    static AlarmAdapter alarmAdapter; //arrayAdapter for the ListView
    static AlarmManager alarmManager; //alarmManager

    static PendingIntent pendingIntent; //pendingIntent for adding to alarmManager

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_alarm);

        //initialise alarmManager
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        // initialise alarms ArrayList if empty, else load from FILE
        alarms = AlarmLab.get(getApplicationContext()).getAlarms();

        //initiates AlarmAdapter for ListView, (context, layout, strArray)
        alarmAdapter = new AlarmAdapter(this, alarms);

        //sets up listView and attach the adapter to this ListView
        ListView listView = (ListView) findViewById(R.id.list_alarms);
        assert listView != null;
        listView.setAdapter(alarmAdapter);


        //sets up addAlarm button and listener
        FloatingActionButton buttAddAlarm = (FloatingActionButton) findViewById(R.id.float_add_alarm);
        assert buttAddAlarm != null;
        buttAddAlarm.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view){
                //opens add alarm activity
                requestAddAlarm();
            }
        });

        //sets up listView for contextMenu
        registerForContextMenu(listView);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo){
        super.onCreateContextMenu(menu, view, menuInfo);

        switch(view.getId()){
            case R.id.list_alarms:
                menu.add(0, ID_CONTEXT_EDIT, 0 , "Edit");
                menu.add(0, ID_CONTEXT_DELETE, 0, "Delete"); //(?, key, order, text)
                break;
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item){

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        switch(item.getItemId()){
            case ID_CONTEXT_EDIT:
                //todo: create edit alarm functionality
                return super.onContextItemSelected(item);
            case ID_CONTEXT_DELETE:
                alarmAdapter.remove(alarms.get(info.position));
                return super.onContextItemSelected(item);

        }
        return false;
    }

    //opens up activity to addAlarm
    private void requestAddAlarm() {
        //sets up intent to inputAlarm
        Intent addAlarm = new Intent(getApplicationContext(), InputAlarm.class);
        startActivityForResult(addAlarm, ID_ADD_ALARM);
    }

    //adds alarm to the alarms ArrayList, calls addAlarmIntent to setup pendingIntent
    private void addAlarm(Intent data){
        AlarmDetails newAlarm = new AlarmDetails(data.getIntExtra("hour", 0),
                data.getIntExtra("minute", 0),
                data.getStringExtra("alarm_name"));

        addAlarmIntent(newAlarm);

        alarms.add(newAlarm);

        AlarmLab.get(getApplicationContext()).saveAlarms();

        alarmAdapter.notifyDataSetChanged();
    }

    //sends pending intent to alarm service
    //pre-cond: alarmDetail object
    //post-cond: sends pendingIntent to alarmManager
    private void addAlarmIntent(AlarmDetails alarm){

        //setup alarmRinger intent to call alarmReceiver
        Intent alarmIntent = new Intent(getApplicationContext(), AlarmReceiver.class);

        //todo: change to AlarmWake activity w/ Service
        pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), (int)alarm.getId(), alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        //set pendingIntent at alarmTime
        alarmManager.set(AlarmManager.RTC_WAKEUP, alarm.getTimeInMillis(), pendingIntent);
    }

    //show the timePicker dialog inside a DialogFragment
    private void showTimePickerDialog(){
        DialogFragment newFrag = new TimePickerFragment();
        newFrag.show(getSupportFragmentManager(), "timePicker"); //requires instance of a FragmentManager, + unique tag for this fragment
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(resultCode == ID_RESULT_OK){
            switch(requestCode){
                case ID_ADD_ALARM:
                    addAlarm(data);
                    break;
            }
        }
    }

    // saves arraylist of alarms to database
    @Override
    protected void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);

        //save info by doing eg. outState.putString("key", varName), outState.putFloatArray("key",..
        AlarmLab.get(getApplicationContext()).saveAlarms();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState){
        super.onRestoreInstanceState(savedInstanceState);

        //restore info by taking it out eg. var = savedInstanceState.getString("key");
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

}

//AlarmAdapter for the ListView
class AlarmAdapter extends ArrayAdapter<AlarmDetails> {
    public AlarmAdapter(Context context, ArrayList<AlarmDetails> alarmList){
        super(context, 0, alarmList);
    }

    //returns actual View to be displayed as row within the alarm ListView
    @Override
    public View getView(int pos, View convertView, ViewGroup parent){
        //retrieves alarms array for this position
        final AlarmDetails alarm = getItem(pos);

        //check if existing view is being reused, else just inflate the view with custom alarm_list_item xml
        if(convertView == null)
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.alarm_list_item, parent, false);

        //lookup the Views to be populated, ie. alarm name and alarm time
        TextView alarmName = (TextView) convertView.findViewById(R.id.alarm_name);
        TextView alarmTime = (TextView) convertView.findViewById(R.id.alarm_time);

        //gets the switch widget for the View
        Switch aSwitch = (Switch) convertView.findViewById(R.id.alarm_on_state);
        aSwitch.setChecked(MainAlarm.alarms.get(pos).isOnState());         //if the alarm state was on, set aSwitch accordingly

        aSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alarm.toggleOnState();

                //todo: change to AlarmWake activity w/ Service 2
                Intent alarmIntent = new Intent(getContext(), AlarmReceiver.class);

                //todo: whose pendingIntent?
                MainAlarm.pendingIntent = PendingIntent.getBroadcast(getContext(), (int)alarm.getId(), alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                //checks the pendingIntent for the alarm
                if(alarm.isOnState())
                    MainAlarm.alarmManager.set(AlarmManager.RTC_WAKEUP, alarm.getTimeInMillis(), MainAlarm.pendingIntent); //reset just in case? todo: check if needed
                else //cancel the alarm
                    MainAlarm.alarmManager.cancel(MainAlarm.pendingIntent);

                 notifyDataSetChanged();
            }
        });

        //updates the Views with the data
        alarmName.setText(alarm.strName);
        alarmTime.setText(alarm.nHour+":"+alarm.nMin+(alarm.bOnState?" ON":" OFF")); //CHANGE THIS, ON OFF JUST TO TEST ONLY

        //return completed view to render on screen
        return convertView;
    }


}


