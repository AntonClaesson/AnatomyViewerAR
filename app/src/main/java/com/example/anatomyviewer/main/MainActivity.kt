package com.example.anatomyviewer.main

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI

import com.example.anatomyviewer.R

import com.example.anatomyviewer.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val TAG: String = MainActivity::class.java.toString()

    private lateinit var binding: ActivityMainBinding
    private lateinit var mainActivityViewModel: MainActivityViewModel

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupDataBinding()
        setupViewModel()

        drawerLayout = binding.drawerLayout
        val navController = this.findNavController(R.id.nav_host_fragment)
        NavigationUI.setupActionBarWithNavController(this, navController, drawerLayout)
        appBarConfiguration = AppBarConfiguration(navController.graph, drawerLayout)
        NavigationUI.setupWithNavController(binding.navView, navController)
    }

    private fun setupDataBinding(){
        binding = DataBindingUtil.setContentView(this,
            R.layout.activity_main
        )
        binding.lifecycleOwner = this
    }

    private fun setupViewModel(){
        mainActivityViewModel = ViewModelProvider(this).get(MainActivityViewModel::class.java)
        binding.mainActivityViewModel = mainActivityViewModel
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = this.findNavController(R.id.nav_host_fragment)
        return NavigationUI.navigateUp(navController, appBarConfiguration)
    }
}
