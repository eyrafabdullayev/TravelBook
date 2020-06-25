package com.eyrafabdullayev.travelbook.Activity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.eyrafabdullayev.travelbook.R

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.util.*
import kotlin.collections.HashMap

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var locationManager: LocationManager
    private lateinit var locationListener: LocationListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.clear()

        val intent = intent
        if (intent != null) {
            val show = intent.getBooleanExtra("show", false)
            val add = intent.getBooleanExtra("add", false)

            if (show) {

                val address = intent.getStringExtra("address")
                val latitude = intent.getDoubleExtra("latitude", 0.0)
                val longitude = intent.getDoubleExtra("longitude", 0.0)

                if (address != null && latitude != null && longitude != null) {
                    val latLng = LatLng(latitude, longitude)
                    mMap.addMarker(MarkerOptions().position(latLng).title(address))
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18f))
                } else {
                    val intent = Intent(this, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(intent)
                }

            } else if (add) {

                mMap.setOnMapLongClickListener(myListener)

                locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
                locationListener = object : LocationListener {
                    override fun onLocationChanged(location: Location?) {
                        if (location != null) {
                            val sharedPreferences = this@MapsActivity.getSharedPreferences(
                                "com.eyrafabdullayev.travelbook",
                                Context.MODE_PRIVATE
                            )
                            val firstTimeCheck = sharedPreferences.getBoolean("notFirstTime", false)
                            if (!firstTimeCheck) {
                                mMap.clear()

                                val latLng = LatLng(location.latitude, location.longitude)
                                val address = getAddress(latLng)
                                mMap.addMarker(
                                    MarkerOptions().position(latLng)
                                        .title(address.get("thoroughfare") + " " + address.get("subThoroughfare"))
                                )
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))

                                sharedPreferences.edit().putBoolean("notFirstTime", true).apply()
                            }
                        }

                    }

                    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
                        TODO("Not yet implemented")
                    }

                    override fun onProviderEnabled(provider: String?) {
                        TODO("Not yet implemented")
                    }

                    override fun onProviderDisabled(provider: String?) {
                        TODO("Not yet implemented")
                    }

                }

                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        1
                    )
                } else {
                    locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        1,
                        1f,
                        locationListener
                    )
                    val lastKnownLocation =
                        locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    if (lastKnownLocation != null) {
                        val latLng = LatLng(lastKnownLocation.latitude, lastKnownLocation.longitude)
                        val address = getAddress(latLng)
                        mMap.addMarker(
                            MarkerOptions().position(latLng)
                                .title(address.get("thoroughfare") + " " + address.get("subThoroughfare"))
                        )
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                    }
                }
            }
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 1) {
            if (grantResults != null && grantResults.size > 0) {
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        1,
                        1f,
                        locationListener
                    )
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    val myListener = object : GoogleMap.OnMapLongClickListener {
        override fun onMapLongClick(p0: LatLng?) {
            if (p0 != null) {
                mMap.clear()

                val address = getAddress(p0)

                val builder = AlertDialog.Builder(this@MapsActivity)
                builder.setTitle("Are you sure?")
                builder.setMessage(
                    "add to list - " + address.get("thoroughfare") + " " + if (address.get(
                            "subThoroughfare"
                        ) == null
                    ) "" else address.get("subThoroughfare")
                )
                builder.setPositiveButton(android.R.string.yes) { dialog, which ->
                    if (!address.isEmpty()) {

                        //insert
                        insertToDatabase(p0,address)

                        Toast.makeText(
                            applicationContext,
                            "Success, place added ..",
                            Toast.LENGTH_LONG
                        )
                            .show()

                        val intent = Intent(this@MapsActivity, MainActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        startActivity(intent)
                    } else {
                        Toast.makeText(
                            applicationContext,
                            "Please try another place ..",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
                builder.setNegativeButton(android.R.string.no) { dialog, which ->
                    Toast.makeText(
                        applicationContext,
                        "You can try again ..",
                        Toast.LENGTH_LONG
                    ).show()
                }
                builder.show()
            }
        }

    }

    fun getAddress(latLng: LatLng): HashMap<String, String> {
        var address = hashMapOf<String, String>()
        if (latLng != null) {
            val geocoder = Geocoder(this, Locale.getDefault())

            if (geocoder != null) {
                val addressList = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)

                if (addressList != null && addressList.size > 0) {
                    if (addressList[0].thoroughfare != null) {
                        address.put("thoroughfare", addressList[0].thoroughfare)
                        if (addressList[0].subThoroughfare != null) {
                            address.put("subThoroughfare", addressList[0].subThoroughfare)
                        }
                    }

                    if (addressList[0].countryName != null) {
                        address.put("countryName", addressList[0].countryName)
                    }
                }
            }
        }
        return address;
    }

    fun insertToDatabase(p0: LatLng,address: HashMap<String,String>) {
        try {
            val database =
                openOrCreateDatabase("Places", Context.MODE_PRIVATE, null)
            database.execSQL("CREATE TABLE IF NOT EXISTS places (id INTEGER PRIMARY KEY,latitude DOUBLE,longitude DOUBLE,address,country VARCHAR)")

            val sqlString =
                "INSERT INTO places (latitude,longitude,address,country) VALUES (?,?,?,?)"
            val statement = database.compileStatement(sqlString)

            statement.bindDouble(1, p0.latitude)
            statement.bindDouble(2, p0.longitude)
            statement.bindString(
                3,
                address.get("thoroughfare") + " " + if (address.get("subThoroughfare") == null) "" else address.get(
                    "subThoroughfare"
                )
            )
            statement.bindString(4, address.get("countryName"))
            statement.execute()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


}
