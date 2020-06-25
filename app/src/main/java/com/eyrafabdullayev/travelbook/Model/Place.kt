package com.eyrafabdullayev.travelbook.Model

import com.google.android.gms.maps.model.LatLng

class Place {

    private lateinit var latLng: LatLng
    private lateinit var address: String
    private lateinit var countryName: String

    constructor(latLng: LatLng, address: String,countryName: String) {
        this.latLng = latLng
        this.address = address
        this.countryName = countryName
    }

    fun getLatLng(): LatLng {
        return latLng
    }

    fun getAddress(): String {
        return address
    }

    fun getCountryName(): String {
        return countryName
    }
}