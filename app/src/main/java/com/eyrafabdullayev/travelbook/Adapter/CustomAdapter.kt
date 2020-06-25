package com.eyrafabdullayev.travelbook.Adapter

import android.app.Activity
import android.graphics.BitmapFactory
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.eyrafabdullayev.travelbook.Model.Place
import com.eyrafabdullayev.travelbook.R

class CustomAdapter : ArrayAdapter<Place> {

    private lateinit var places: ArrayList<Place>
    private lateinit var context: Activity

    constructor(places: ArrayList<Place>, context: Activity): super(context,
        R.layout.layout_places,places) {
        this.places = places
        this.context = context
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val layoutInflater = context.layoutInflater
        val placeView : View = layoutInflater.inflate(R.layout.layout_places,null,true)

        val addressText : TextView = placeView.findViewById(R.id.addressText)
        addressText.setText(places.get(position).getAddress())
        val countryText : TextView = placeView.findViewById(R.id.countryText)
        countryText.setText(places.get(position).getCountryName())

        val marker : ImageView = placeView.findViewById(R.id.list_marker)
        val bitmap = BitmapFactory.decodeResource(context.resources,
            R.drawable.marker
        )
        marker.setImageBitmap(bitmap)

        return placeView
    }
}