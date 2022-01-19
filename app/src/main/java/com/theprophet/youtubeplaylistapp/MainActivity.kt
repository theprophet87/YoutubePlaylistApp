    package com.theprophet.youtubeplaylistapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.Window
import android.view.WindowInsets.Type.systemBars
import android.view.WindowInsetsController
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.*
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.navigation.NavigationView
import com.theprophet.youtubeplaylistapp.databinding.ActivityMainBinding
import com.theprophet.youtubeplaylistapp.databinding.FragmentHomeBinding
import kotlinx.coroutines.launch

    class MainActivity : FragmentActivity() {

    //view binding variable
    private var binding: ActivityMainBinding? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        


        //find views from home activity xml
        val toolbar = findViewById<MaterialToolbar>(R.id.topAppbar)
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
        val navigationView = findViewById<NavigationView>(R.id.navigation_view)
        
        //set up navHost and navController
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        val navController = navHostFragment.navController


        //Side menu behavior
        toolbar.setNavigationOnClickListener(View.OnClickListener {
            drawerLayout.openDrawer(
                GravityCompat.START
            )
        })

        /* set up variables for nav destinations */

        // from home to edit
         val toHomeAction = HomeFragmentDirections.actionHomeFragmentToEditFragment()

        // from edit to home
        val toEditAction = EditFragmentDirections.actionEditFragmentToHomeFragment2()


        /* variable to check if you are at home screen
        * this will help to avoid crashes from nullpointer exceptions */
        var atHome: Boolean = true


        /* logic for navigation from menu */
        navigationView.setNavigationItemSelectedListener(NavigationView.OnNavigationItemSelectedListener { item ->
            val id = item.itemId
            item.isChecked = true
            drawerLayout.closeDrawer(GravityCompat.START)
            when (id) {
                R.id.nav_home -> {

                    //if you're not already at the Home screen, go to Edit screen
                    if(!atHome) {
                        navHostFragment.findNavController().navigate(toEditAction)
                    }
                    //if you're at the home screen already, do nothing
                    else{
                        return@OnNavigationItemSelectedListener true
                    }

                    //make sure the check is set to Home screen
                    atHome = true
                }
                R.id.nav_edit -> {

                    //if you are at the Home screen, go to Edit screen
                    if(atHome) {
                        navHostFragment.findNavController().navigate(toHomeAction)

                        //if you are in the Edit screen, do nothing
                    }else{
                        return@OnNavigationItemSelectedListener true
                    }

                    //set check to not being at Home
                    atHome = false
                }

                else -> return@OnNavigationItemSelectedListener true
            }
            true
        })






        }



}