package com.car.carsquad.carapp;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Objects;

public class SearchActivity extends AppCompatActivity implements View.OnClickListener {

    private final double SEARCHRADIUS = 10; //SEARCH WITHIN 10 miles

    private Button mCancelSearch;
    private Button mSearch;
    private String startPt;
    private String endPt;
    private LatLng startLatLng;
    private LatLng endLatLng;
    private Double findStartLat;
    private Double findStartLng;
    private Double findEndLat;
    private Double findEndLng;
    private TextView mDisplayDate;
    private TextView mDisplayTime;
    private DatePickerDialog.OnDateSetListener mDateSetListener;
    private TimePickerDialog.OnTimeSetListener mTimeSetListener;

    private DatabaseReference mPostDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        mPostDatabase = FirebaseDatabase.getInstance().getReference().child("post");
        mPostDatabase.keepSynced(true);

        //GOOGLE PLACES AUTOCOMPLETE
        PlaceAutocompleteFragment autocompleteFragment1 = (PlaceAutocompleteFragment) getFragmentManager()
                .findFragmentById(R.id.post_start_point1);
        autocompleteFragment1.setHint("ENTER START POINT");
        ImageView startIcon = (ImageView)((LinearLayout)autocompleteFragment1.getView()).getChildAt(0);
        startIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_start));
        autocompleteFragment1.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                startPt = place.getName().toString();
                startLatLng = place.getLatLng();
                findStartLat = startLatLng.latitude;
                findStartLng = startLatLng.longitude;
            }
            @Override
            public void onError(Status status) {
            }
        });
        PlaceAutocompleteFragment autocompleteFragment2 = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.post_destination_point1);
        autocompleteFragment2.setHint("ENTER DESTINATION");
        ImageView endIcon = (ImageView)((LinearLayout)autocompleteFragment1.getView()).getChildAt(0);
        endIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_end));
        autocompleteFragment2.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                endPt = place.getName().toString();
                endLatLng = place.getLatLng();
                findEndLat = endLatLng.latitude;
                findEndLng = endLatLng.longitude;
            }
            @Override
            public void onError(Status status) {
            }
        });

        mCancelSearch = (Button) findViewById(R.id.cancel_search);
        mCancelSearch.setOnClickListener(this);
        mSearch = (Button) findViewById(R.id.submit_search_button);
        mSearch.setOnClickListener(this);
        mDisplayDate = (TextView) findViewById(R.id.tvDate);
        mDisplayTime = (TextView) findViewById(R.id.tvTime);

        mDisplayDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog dialog = new DatePickerDialog(
                        SearchActivity.this,
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth, mDateSetListener,
                        year, month, day);
                Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.getDatePicker().setMinDate(System.currentTimeMillis()-1000);
                dialog.show();
            }
        });

        mDisplayTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                Calendar cal = Calendar.getInstance();
                int hour = cal.get(Calendar.HOUR_OF_DAY);
                int minute = cal.get(Calendar.MINUTE);
                TimePickerDialog dialog = new TimePickerDialog(
                        SearchActivity.this,
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth, mTimeSetListener,
                        hour, minute, true);
                Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                dialog.show();
            }
        });
        mDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                month++;
                Log.d("DriverPostActivity", "onDateSet: mm/dd/yyyy: " + month + "/" + day + "/" + year);
                String date =  month + "/" + day + "/" + year;
                mDisplayDate.setText(date);
            }
        };
        mTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                Log.d("DriverPostActivity", "onTimeSet: hh:mm: " + hour + "/" + minute);
                String time =  checkDigit(hour) + ":" + checkDigit(minute);
                mDisplayTime.setText(time);
            }
        };
    }

    //search for rides that match user preferences
    public void searchRides(){
        double result = CalculationByDistance(startLatLng, endLatLng);
        //Toast.makeText(this, "Distance is: " + Double.toString(result), Toast.LENGTH_LONG).show();

        AlertDialog.Builder builder = new AlertDialog.Builder(SearchActivity.this);
        builder.setCancelable(true);
        builder.setTitle("DISTANCE CALCULATED");
        builder.setMessage("Distance is approx: " + (int) result + " miles");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        builder.show();

    }
    //CALCULATE DISTANCE GIVEN COORDINATES
    public double CalculationByDistance(LatLng StartP, LatLng EndP) {
        int Radius = 6371;// radius of earth in Km
        double lat1 = StartP.latitude;
        double lat2 = EndP.latitude;
        double lon1 = StartP.longitude;
        double lon2 = EndP.longitude;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2)
                * Math.sin(dLon / 2);
        double c = 2 * Math.asin(Math.sqrt(a));
        double valueResult = Radius * c;
        double km = valueResult / 1;
        DecimalFormat newFormat = new DecimalFormat("####");
        int kmInDec = Integer.valueOf(newFormat.format(km));
        double meter = valueResult % 1000;
        int meterInDec = Integer.valueOf(newFormat.format(meter));
        Log.i("Radius Value", "" + valueResult + "   KM  " + kmInDec
                + " Meter   " + meterInDec);
        //return distance in miles
        return Radius * c * 0.621371;
    }

    public void onClick(View view) {
        if(view == mCancelSearch){
            finish();
            startActivity(new Intent(this, RiderActivity.class));
        }
        else if(view == mSearch){
            searchRides();
        }
    }

    public String checkDigit(int number) {
        return number <= 9 ? "0" + number : String.valueOf(number);
    }
}
