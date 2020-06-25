package com.eyrafabdullayev.travelbook.Activity

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import com.eyrafabdullayev.travelbook.Adapter.CustomAdapter
import com.eyrafabdullayev.travelbook.Model.Place
import com.eyrafabdullayev.travelbook.R
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.Exception

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val places = getPlaces()

        val customAdapter =
            CustomAdapter(places, this)
        listView.adapter = customAdapter

        listView.setOnItemClickListener { parent, view, position, id ->
            val intent = Intent(this,
                MapsActivity::class.java)

            val place = places.get(position)

            val latitude = place.getLatLng().latitude
            val longitude = place.getLatLng().longitude

            intent.putExtra("show",true)

            intent.putExtra("address",place.getAddress())
            intent.putExtra("latitude",latitude)
            intent.putExtra("longitude",longitude)

            startActivity(intent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val menuInflater = MenuInflater(this)
        menuInflater.inflate(R.menu.add_place, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == R.id.add_new_place) {
           val intent = Intent(this,
               MapsActivity::class.java)
           intent.putExtra("add",true)
           startActivity(intent)
        }
        return super.onOptionsItemSelected(item)
    }

    fun getPlaces(): ArrayList<Place> {

        val places = arrayListOf<Place>()

        try {
            val database = openOrCreateDatabase("Places", Context.MODE_PRIVATE,null)
            val cursor = database.rawQuery("SELECT * FROM places",null)

            val latitudeIx = cursor.getColumnIndex("latitude")
            val longitudeIx = cursor.getColumnIndex("longitude")
            val addressIx = cursor.getColumnIndex("address")
            val countryIx = cursor.getColumnIndex("country")

            while(cursor.moveToNext()) {
                val latitude = cursor.getDouble(latitudeIx)
                val longitude = cursor.getDouble(longitudeIx)
                val address = cursor.getString(addressIx)
                val country = cursor.getString(countryIx)

                val latLng = LatLng(latitude,longitude)
                val place = Place(
                    latLng,
                    address,
                    country
                )

                places.add(place)
            }

            cursor.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return places
    }
}
