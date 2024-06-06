package com.example.todo1.Fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.todo1.R
import com.google.firebase.auth.FirebaseAuth

class SplashFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_splash, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try {
            auth = FirebaseAuth.getInstance()
            navController = Navigation.findNavController(view)
            Handler(Looper.getMainLooper()).postDelayed({
                if (auth.currentUser != null) {
                    navController.navigate(R.id.action_splashFragment_to_homeFragment)
                } else {
                    navController.navigate(R.id.action_splashFragment_to_inFragment)
                }
            }, 3000) // 2000 milliseconds delay
        } catch (e: Exception) {
            // If any exception occurs, navigate to the sign-in fragment
            navController.navigate(R.id.action_splashFragment_to_inFragment)
        }
    }
}
