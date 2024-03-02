package com.learn.weatherapp.presentation.view

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.EditText
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.learn.weatherapp.R
import com.learn.weatherapp.data.model.WeatherData
import com.learn.weatherapp.databinding.ActivityMainBinding
import com.learn.weatherapp.presentation.viewModel.MainViewModel
import com.learn.weatherapp.utils.Resource
import com.learn.weatherapp.utils.Utils
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private lateinit var mainBinding: ActivityMainBinding
    private val permissionId = 2
    private lateinit var mainViewModel: MainViewModel
    var userLocation = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainBinding = ActivityMainBinding.inflate(layoutInflater)


        setContentView(mainBinding.root)

        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }

        mainViewModel = ViewModelProvider(this)[MainViewModel::class.java]
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mainBinding.mainLayout.visibility = View.GONE
        initObserver()
        initUi()
    }

    private fun initUi() {
        val searchEditText: EditText =
            mainBinding.searchView.findViewById(androidx.appcompat.R.id.search_src_text)
        searchEditText.setTextColor(ContextCompat.getColor(this, R.color.black))
        searchEditText.setHintTextColor(ContextCompat.getColor(this, R.color.black))
        mainBinding.searchView.queryHint = "Search for a city"
        mainBinding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    userLocation = query
                    mainViewModel.getData(query)
                    mainBinding.searchView.setQuery("", false);
                    mainBinding.searchView.clearFocus();
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }

        })

        mainBinding.refresh.setOnClickListener {
            mainViewModel.getData(userLocation)
        }
        val searchEditTextOnError: EditText =
            mainBinding.searchViewOnError.findViewById(androidx.appcompat.R.id.search_src_text)
        searchEditTextOnError.setTextColor(ContextCompat.getColor(this, R.color.black))
        searchEditTextOnError.setHintTextColor(ContextCompat.getColor(this, R.color.black))
        mainBinding.searchViewOnError.queryHint = "Search for a city"
        mainBinding.searchViewOnError.setOnQueryTextListener(object :
            SearchView.OnQueryTextListener,
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    userLocation = query
                    mainViewModel.getData(query)
                    mainBinding.searchViewOnError.setQuery("", false);
                    mainBinding.searchViewOnError.clearFocus();
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }

        })

    }

    //get time from epoch
    private fun getTime(epochSec: Long): String? {
        val date = Date(epochSec * 1000)
        val format = SimpleDateFormat(
            "HH:mm",
            Locale.getDefault()
        )
        return format.format(date)
    }

    private fun initObserver() {
        mainViewModel.liveData.observe(this) {
            when (it) {
                is Resource.Error -> {
                    mainBinding.apply {
                        mainLayout.visibility = View.GONE
                        progress.visibility = View.GONE
                        errorText.visibility = View.VISIBLE
                        errorText.text = it.message
                        searchViewOnError.visibility = View.VISIBLE
                    }


                }

                Resource.Loading -> {
                    mainBinding.apply {
                        mainLayout.visibility = View.GONE
                        progress.visibility = View.VISIBLE
                        errorText.visibility = View.GONE
                        searchViewOnError.visibility = View.GONE
                    }


                }

                is Resource.Success -> {
                    mainBinding.apply {
                        mainLayout.visibility = View.VISIBLE
                        progress.visibility = View.GONE
                        errorText.visibility = View.GONE
                        searchView.visibility = View.VISIBLE
                        searchViewOnError.visibility = View.GONE
                        showData(it.data)
                    }

                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showData(data: WeatherData) {
        val temperature = data.main.temp.toString()
        val humidity = data.main.humidity
        val windSpeed = data.wind.speed
        val sunRise = data.sys.sunrise
        val sunSet = data.sys.sunset
        val seaLevel = data.main.pressure
        val condition = data.weather.firstOrNull()?.main ?: "unknown"
        val maxTemp = data.main.tempMax
        val minTemp = data.main.tempMin
        val city = data.name

        mainBinding.apply {
            cityName.text = city
            temp.text = "$temperature\u00B0C"
            humidityValue.text = "$humidity %"
            dayValue.text = dayName(System.currentTimeMillis())
            dayDate.text = currentDate()
            windSpeedValue.text = "$windSpeed m/s"
            conditions.text = condition
            type.text = condition
            sunRiseValue.text = getTime(sunRise)
            sunSetValue.text = getTime(sunSet)
            seaLevelValue.text = "$seaLevel hPa"
            minTempVal.text = "Min:$minTemp\u00B0C"
            maxTempVal.text = "Max:$maxTemp\u00B0C"
            setBackground(condition)
        }
    }

    private fun setBackground(condition: String) {

        when (condition) {

            "Clear Sky", "Sunny", "Clear" -> {
                mainBinding.mainLayout.setBackgroundResource(R.drawable.sunny)
                mainBinding.climateType.setBackgroundResource(R.drawable.sun)
            }

            "Party Clouds", "Clouds", "Overcast", "Mist", "Foggy" -> {
                mainBinding.mainLayout.setBackgroundResource(R.drawable.cloudy)
                mainBinding.climateType.setBackgroundResource(R.drawable.cloud)
            }

            "Light Rain", "Drizzle", "Moderate Rain", "Showers", "Heavy Rain", "Rain" -> {
                mainBinding.mainLayout.setBackgroundResource(R.drawable.rainy)
                mainBinding.climateType.setBackgroundResource(R.drawable.rain)
            }

            "Light Snow", "Moderate Snow", "Heavy Snow", "Blizzard" -> {
                mainBinding.mainLayout.setBackgroundResource(R.drawable.snowy)
                mainBinding.climateType.setBackgroundResource(R.drawable.snow)
            }

            else -> {
                mainBinding.mainLayout.setBackgroundResource(R.drawable.sunny)
                mainBinding.climateType.setBackgroundResource(R.drawable.sun)
            }
        }
    }

    private fun currentDate(): String {

        val sdf = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun dayName(timeStamp: Long): String {
        val sdf = SimpleDateFormat("EEEE", Locale.getDefault())
        return sdf.format(Date())
    }

    //get location
    private fun getLocation() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return
                }
                mFusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->
                    var location: Location? = task.result
                    if (location != null) {
                        getDatFromServer(location)
                    } else {
                        val locationManager: LocationManager =
                            getSystemService(Context.LOCATION_SERVICE) as LocationManager
                        location =
                            locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                        if (location != null) {
                            getDatFromServer(location)
                        } else {
                            Toast.makeText(this, "Unable to fetch location", Toast.LENGTH_SHORT)
                                .show()
                            mainBinding.apply {
                                progress.visibility = View.GONE
                                mainLayout.visibility = View.GONE
                            }
                        }

                    }
                }
            } else {
                Toast.makeText(this, "Please turn on location", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            requestPermissions()
        }
    }

    @SuppressLint("SetTextI18n")
    @Suppress("DEPRECATION")
    private fun getDatFromServer(location: Location) {
        if (Utils.isNetworkAvailable(this@MainActivity)) {
            val geocoder = Geocoder(this, Locale.getDefault())
            val list: MutableList<Address>? =
                geocoder.getFromLocation(location.latitude, location.longitude, 1)
            mainBinding.apply {
                list?.let {
                    userLocation = it[0].locality
                    mainViewModel.getData(userLocation)
                }
            }

        } else {
            mainBinding.errorText.visibility = View.VISIBLE
            mainBinding.errorText.text = "No Internet"

        }
    }

    override fun onResume() {
        super.onResume()
        getLocation()
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private fun checkPermissions(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ),
            permissionId
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == permissionId) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                getLocation()
            } else if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_DENIED)) {
                finish()
            }

        }
    }
}
