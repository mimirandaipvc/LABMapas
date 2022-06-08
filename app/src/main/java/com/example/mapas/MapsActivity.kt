package com.example.mapas

import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.core.app.ActivityCompat
import com.example.mapas.api.ServiceBuilder

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.mapas.databinding.ActivityMapsBinding
import com.example.mapas.models.User
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.example.mapas.api.Endpoints
import com.google.android.gms.location.*


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    //calculate distance
    private var continenteLat: Double = 0.0
    private var continenteLong: Double = 0.0

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    private lateinit var lastLocation: Location
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        private const val REQUEST_CHECK_SETTINGS = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //added to implement distance between two locations
        continenteLat = 41.7043
        continenteLong = -8.8148


        //intitialize fusedLocationClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        //maps
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)


        val request = ServiceBuilder.buildService(Endpoints::class.java)
        val call = request.getUsers()

        var position: LatLng

        call.enqueue(object : Callback<List<User>> {
            override fun onResponse(call: Call<List<User>>, response: Response<List<User>>) {
                if (response.isSuccessful){
                    val users = response.body()!!
                    for (user in users) {
                        position = LatLng(user.address.geo.lat.toString().toDouble(), user.address.geo.lng.toString().toDouble())
                        mMap.addMarker(MarkerOptions().position(position).title(user.address.suite + " - " + user.address.city))
                    }
                }

            }
            override fun onFailure(call: Call<List<User>>, t: Throwable) {
                TODO("Not yet implemented")
            }})


        // request creation
        createLocationRequest()

        //callback para estar sempre a fazer os pedidos
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)
                lastLocation = p0.lastLocation
                var loc = LatLng(lastLocation.latitude, lastLocation.longitude)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 15.0f))
                findViewById<TextView>(R.id.txtcoordenadas).setText("Lat: " + loc.latitude + " - Long: " + loc.longitude)
                Log.d("**** Ruben", "new location received - " + loc.latitude + " -" + loc.longitude)
                //add address with new function
                val address = getAddress(lastLocation.latitude, lastLocation.longitude)
                findViewById<TextView>(R.id.txtmorada).setText("Morada: " + address)
                //calculate Distance
                findViewById<TextView>(R.id.txtdistancia).setText("Distância(metros) : " + calculateDistance(
                    lastLocation.latitude, lastLocation.longitude,
                    continenteLat, continenteLong).toString())
            }
        }




    }

    override fun onResume() {
        super.onResume()
        startLocationUpdates()
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            return
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null /* Looper */)
    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
    }


    private fun getAddress(lat: Double, lng: Double): String {
        val geocoder = Geocoder(this)
        val list = geocoder.getFromLocation(lat, lng, 1)
        return list[0].getAddressLine(0)
    }

    private fun createLocationRequest() {
        locationRequest = LocationRequest()
// interval specifies the rate at which your app will like to receive updates.
        locationRequest.interval = 10000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    //calcula distância
    fun calculateDistance(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Float {
        val results = FloatArray(1)
        Location.distanceBetween(lat1, lng1, lat2, lng2, results)
        // distance in meter
        return results[0]
    }

}