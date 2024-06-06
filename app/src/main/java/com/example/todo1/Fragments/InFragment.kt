package com.example.todo1.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.todo1.R
import com.example.todo1.databinding.FragmentInBinding
import com.google.firebase.auth.FirebaseAuth


class InFragment : Fragment() {


    private lateinit var auth: FirebaseAuth
    private lateinit var navController: NavController
    private lateinit var binding: FragmentInBinding
//    private lateinit var firestoreRepository: FirestoreRepository

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentInBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init(view)
        registerEvents()
    }

    private fun init(view: View) {
        navController = Navigation.findNavController(view)
        auth = FirebaseAuth.getInstance()
//        firestoreRepository = FirestoreRepository()
    }

    private fun registerEvents() {
        binding.authTextView.setOnClickListener {
            navController.navigate(R.id.action_inFragment_to_upFragment)
        }

        binding.nextBTN.setOnClickListener {
            val email = binding.editTextEmail.text.toString()
            val pass = binding.editTextPassword.text.toString()

            if (email.isNotEmpty() && pass.isNotEmpty()) {

                auth.signInWithEmailAndPassword(email, pass).addOnCompleteListener {
                    if (it.isSuccessful) {
                        Toast.makeText(context, "Logged In SUCCESSFULLY", Toast.LENGTH_SHORT).show()
                        navController.navigate(R.id.action_inFragment_to_homeFragment)
                    } else {
                        Toast.makeText(context, it.exception?.message, Toast.LENGTH_SHORT).show()
                    }
                }

            } else {
                Toast.makeText(context, "Please fill out all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }
}