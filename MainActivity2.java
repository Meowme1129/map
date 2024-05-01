package com.example.mappp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity2 extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    TextView textView3;
    TextView textView4; // 修改为textView4
    Spinner spinner;
    Button button;
    Button button3;
    private LatLng clickPoint;

    FusedLocationProviderClient fusedLocationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        if(getIntent()!=null){
           Bundle bundle= getIntent().getBundleExtra("clickBundle");
           if(bundle!=null) {
               clickPoint = bundle.getParcelable("clickPoint");
           }

        }
        // 连接 UI 元件
        textView3 = findViewById(R.id.textView3);
        textView4 = findViewById(R.id.textView4); // 修改为textView4
        spinner = findViewById(R.id.spinner);
        button = findViewById(R.id.button);
        button3 = findViewById(R.id.button3);

        // 设置 Spinner 选项
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.spinner,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(0);

        // 初始化 FusedLocationProviderClient
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // 设置按钮点击事件
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 获取 Spinner 的当前选定位置
                int selectedPosition = spinner.getSelectedItemPosition();
                // 获取选定位置对应的选项值
                String selectedOption = spinner.getSelectedItem().toString();
                // 将选定的选项设置到 "通报结果" TextView 中
                textView4.setText("通報结果：" + selectedOption); // 修改为textView4
                // 显示 "通报结果" TextView
                textView4.setVisibility(View.VISIBLE); // 修改为textView4

                // 启动地图活动，等待结果
                Intent intent = new Intent(MainActivity2.this, MapsActivity.class);
                intent.putExtra("selectedOption", selectedOption);
                setResult(1234,intent);
                finish();
            }
        });

        // 设置 button3 按钮的点击事件
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 启动地图活动，等待结果
                setResult(12345);
                finish();
            }
        });

        // 请求位置权限并获取当前位置
        requestLocationPermission();
    }

    // 请求位置权限
    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            getCurrentLocation();
        }
    }

    // 获取当前位置
    private void getCurrentLocation() {
        // 检查是否已经授予了位置权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // 如果没有权限，则请求权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            // 如果已经有权限，则获取当前位置
            fusedLocationProviderClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                // 获取当前位置的经纬度
                                double latitude = location.getLatitude();
                                double longitude = location.getLongitude();
                                // 将经纬度转换为地址信息并显示在textView3上
                                String address = getAddressFromLocation(latitude, longitude);
                                if(clickPoint!=null)
                                {
                                    String clickAddress = getAddressFromLocation(clickPoint.latitude, clickPoint.longitude);
                                    textView3.setText(clickAddress);
                                }
                                else if (address != null) {
                                    textView3.setText(address); // 修改为textView3
                                } else {
                                    textView3.setText("未找到位置信息"); // 修改为textView3
                                }
                            } else {
                                textView3.setText("无法获取当前位置"); // 修改为textView3
                            }
                        }
                    });
        }
    }

    // 将经纬度转换为地址信息
    private String getAddressFromLocation(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && addresses.size() > 0) {
                Address address = addresses.get(0);
                return address.getAddressLine(0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    // 处理位置权限请求结果
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 用户授予了位置权限，获取当前位置
                getCurrentLocation();
            } else {
                Toast.makeText(this, "位置权限被拒绝，无法获取当前位置", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // 获取来自 MapsActivity 的结果并设置到相应的 TextView 中
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                String address = data.getStringExtra("address");
                if (address != null) {
                    // 设置位置信息到textView3中
                    textView3.setText(address); // 修改为textView3
                } else {
                    textView3.setText("未找到位置信息"); // 修改为textView3
                }
            }
        }
    }
}
