package com.example.mappp;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    GoogleMap gMap;
    Location currentLocation;
    Marker marker;
    FusedLocationProviderClient fusedClient;
    private static final int REQUEST_CODE = 101;
    private static final int YOUR_REQUEST_CODE = 1001;
    SearchView searchView;
    private Button button;
    private List<Marker> markerList = new ArrayList<>();
    String selectedOption; // 儲存通報結果
    private Polyline polyline;
    private Marker mCurrLocationMarker;
    private LatLng startPoint;
    private LatLng endPoint;

    private Marker endMarker;
    private Context mContext;
    private boolean chooseStartPoint=false;
    private boolean chooseEndPoint=false;

    @Override
    protected void onResume() {
        super.onResume();
        // 確保 markerList 不為空且 Google 地圖已經初始化
        if (markerList != null && gMap != null) {
            // 將所有標記重新加載到地圖上
            for (Marker marker : markerList) {
                marker.setVisible(true);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        Intent intent = getIntent();
        mContext = this;

        if (intent != null && intent.hasExtra("selectedOption")) {
            selectedOption = intent.getStringExtra("selectedOption");
        }
        button = findViewById(R.id.button2);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openMainActivity2();
            }
        });
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        searchView = findViewById(R.id.search);
        searchView.clearFocus();

        fusedClient = LocationServices.getFusedLocationProviderClient(this);
        getLocation();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                String loc = searchView.getQuery().toString();
                if (loc == null) {
                    Toast.makeText(MapsActivity.this, "Location Not Found", Toast.LENGTH_SHORT).show();
                } else {
                    Geocoder geocoder = new Geocoder(MapsActivity.this, Locale.getDefault());
                    try {
                        List<Address> addressList = geocoder.getFromLocationName(loc, 1);
                        if (addressList.size() > 0) {
                            LatLng latLng = new LatLng(addressList.get(0).getLatitude(), addressList.get(0).getLongitude());
                            if (marker != null) {
                                marker.remove();
                            }
                            MarkerOptions markerOptions = new MarkerOptions().position(latLng).title(loc);
                            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                            marker = gMap.addMarker(markerOptions);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }

        });
    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
            return;
        }

        Task<Location> task = fusedClient.getLastLocation();

        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    currentLocation = location;
                    SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
                    assert supportMapFragment != null;
                    supportMapFragment.getMapAsync(MapsActivity.this);
                }
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.gMap = googleMap;
        Marker currLocationMarker;
        // 將所有標記重新加載到地圖上
        for (Marker marker : markerList) {
            marker.setVisible(true);
        }

        // 如果 currentLocation 為 null，則返回
        if (currentLocation == null) {
            return;
        }
        Log.e("peter","currentLocation"+currentLocation);
//        LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        startPoint=new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions().position(startPoint);
        if(startPoint!=null) {
            gMap.addMarker(new MarkerOptions().position(startPoint).title("Start Point"));

        }

        // 使用 Geocoder 取得目前位置的地址文字內容
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(currentLocation.getLatitude(), currentLocation.getLongitude(), 1);
            if (addresses != null && addresses.size() > 0) {
                String addressText = addresses.get(0).getAddressLine(0);
                markerOptions.title(addressText); // 將地址文字內容設置為標題
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 在標記上添加通報內容
        if (selectedOption != null) {
            markerOptions.snippet("通報結果：" + selectedOption);
        }

        // 將地圖示記設置為一開始的圖示
        marker = gMap.addMarker(markerOptions);
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(startPoint, 15));

        // 設置地圖點擊事件監聽器
        gMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                // 在點擊位置添加新的標記


                // 地理編碼取得點擊位置的地址資訊
                Geocoder geocoder = new Geocoder(MapsActivity.this, Locale.getDefault());
//                try {
//                    List<Address> addressList = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
//                    if (!addressList.isEmpty()) {
//                        Address address = addressList.get(0);
//                        String addressText = address.getAddressLine(0); // 取得地址文字內容
//                        // 設置返回結果並關閉目前地圖活動
//                        Intent resultIntent = new Intent();
//                        resultIntent.putExtra("address", addressText);
//                        setResult(RESULT_OK, resultIntent);
//                        finish();
//                    }
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
                if (polyline != null) {
                    polyline.remove();
                }
                if(chooseStartPoint){
                    startPoint=latLng;
                    gMap.clear();

                    MarkerOptions startMarkerOptions = new MarkerOptions().position(latLng).title("Current Marker");
                    // 將新標記添加到 markerList 中
                    Marker startMarker = gMap.addMarker(startMarkerOptions);
                    markerList.add(startMarker);
                    openMainActivity2EndPoint(startPoint);
                } else if(chooseEndPoint){

                    if(endMarker!=null) {
                        int index= markerList.indexOf(endMarker);
                        Log.e("peter","index"+index);
                        if(index!=-1) {
                            markerList.get(index).remove();
                        }
                    }
                    endPoint=latLng;

                    MarkerOptions endMarkerOptions = new MarkerOptions().position(latLng).title("Moved Marker");

                    // 在標記上添加通報內容
                    if (selectedOption != null) {
                        endMarkerOptions.snippet("通報結果：" + selectedOption); // 將通報內容設置為標記的 snippet
                    }
                    // 將新標記添加到 markerList 中
                    endMarker = gMap.addMarker(endMarkerOptions);
                    markerList.add(endMarker);
                }
                if(startPoint!=null&&endPoint!=null) {
                    // 添加折線
                    PolylineOptions polylineOptions = new PolylineOptions()
                            .clickable(true)
                            .add(
                                    startPoint,
                                    endPoint);

                    polyline = gMap.addPolyline(polylineOptions);
                }

                // 设置 endPoint 为点击的位置

//                startPoint = latLng;
                // 顯示提示訊息

            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocation();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == YOUR_REQUEST_CODE) { // 使用聲明的請求代碼變數
            // 確保 markerList 不為空且 Google 地圖已經初始化
            if (markerList != null && gMap != null) {
                // 將所有標記重新加載到地圖上
                for (Marker marker : markerList) {
                    marker.setVisible(true);
                }
            }
            if(resultCode==1234){
                if (data != null && data.hasExtra("selectedOption")) {
                    selectedOption = data.getStringExtra("selectedOption");
                }
                Log.e("peter","chooseEndPoint"+resultCode);
                chooseEndPoint=true;

            }  else if(resultCode==12345){
                Log.e("peter","chooseStartPoint"+resultCode);
                chooseStartPoint=true;

            }
        }
    }
    private void resetChooseFlag(){
        chooseStartPoint=false;
        chooseEndPoint=false;

    }
    public void openMainActivity2() {
        resetChooseFlag();
        Intent intent = new Intent(this, MainActivity2.class);
        startActivityForResult(intent,YOUR_REQUEST_CODE);
    }
    public void openMainActivity2EndPoint(LatLng endPoint) {
        resetChooseFlag();
        Intent intent = new Intent(this, MainActivity2.class);
        Bundle bundel= new Bundle();
        bundel.putParcelable("clickPoint",endPoint);
        intent.putExtra("clickBundle",bundel);
        startActivityForResult(intent,YOUR_REQUEST_CODE);
    }
}
