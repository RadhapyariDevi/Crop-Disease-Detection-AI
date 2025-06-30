package com.radhapyari.ai_for_crop_diseases_detection.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.firebase.auth.FirebaseAuth
import com.radhapyari.ai_for_crop_diseases_detection.CategoryActivity
import com.radhapyari.ai_for_crop_diseases_detection.R


class HomeFragment : Fragment() {

    private lateinit var navControl: NavController
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_home, container, false)

    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navControl = Navigation.findNavController(view)
        auth = FirebaseAuth.getInstance()

        val scanButton = view.findViewById<ImageView>(R.id.scanButton)
        val uploadButton = view.findViewById<ImageView>(R.id.uploadButton)
        val profile = view.findViewById<ImageView>(R.id.profile)

        scanButton.setOnClickListener {
            val intent = Intent(requireContext(), CategoryActivity::class.java)
            intent.putExtra("clickedImage", "scanButton")
            startActivity(intent)
        }

        uploadButton.setOnClickListener {
            val intent = Intent(requireContext(), CategoryActivity::class.java)
            intent.putExtra("clickedImage", "uploadButton")
            startActivity(intent)
        }

        profile.setOnClickListener {
            if(auth.currentUser != null){
                navControl.navigate(R.id.action_homeFragment_to_profileFragment)
            }
            else{
                AlertDialog.Builder(requireContext())
                    .setMessage("You haven't signed in yet. Please sign in first to access your profile.")
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton("Sign In") { _, _ ->
                        navControl.navigate(R.id.action_homeFragment_to_signInFragment)
                    }
                    .show()
            }

        }




    }


}