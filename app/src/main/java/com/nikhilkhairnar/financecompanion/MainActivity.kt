package com.nikhilkhairnar.financecompanion

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.nikhilkhairnar.financecompanion.databinding.ActivityMainBinding
import com.nikhilkhairnar.financecompanion.utils.hide
import com.nikhilkhairnar.financecompanion.utils.show

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupNavigation()
    }

    private fun setupNavigation(){
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        binding.bottomNavigation.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.addEditTransactionFragment -> {
                    binding.bottomNavigation.hide()
                    window.statusBarColor =
                        getColor(R.color.background)
                }
                R.id.homeFragment -> {
                    binding.bottomNavigation.show()
                    window.statusBarColor =
                        getColor(R.color.primary)
                }
                else -> {
                    binding.bottomNavigation.show()
                    window.statusBarColor =
                        getColor(R.color.surface_white)
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}