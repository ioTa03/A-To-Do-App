package com.example.todo1.Fragments

import ToDoAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.todo1.R
import com.example.todo1.databinding.FragmentHomeBinding
import com.example.todo1.utils.ToDoData
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore

class HomeFragment : Fragment(), NewFragment.NextBtnClickListener,
    ToDoAdapter.ToDoAdapterClicksInterface {

    private lateinit var auth: FirebaseAuth
    private lateinit var databaseRef: DatabaseReference
    private lateinit var navController: NavController
    private lateinit var binding: FragmentHomeBinding
    private var popUp: NewFragment? = null
    private lateinit var adapter: ToDoAdapter
    private lateinit var mList: MutableList<ToDoData>
    private lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init(view)
        callEvents()
        getDataFromFirebase()
        getDataFromFirestore()
        binding.signOutButton.setOnClickListener {
            signOutUser()
        }
    }

    private fun signOutUser() {
        auth.signOut()
        if (auth.currentUser == null) {
            Toast.makeText(context, "Signed out successfully", Toast.LENGTH_SHORT).show()
        }
        navController.navigate(R.id.action_homeFragment_to_inFragment)
    }

    private fun callEvents() {
        binding.btnHome.setOnClickListener {
            // Prevent multiple instances of popup
            if (popUp != null)
                childFragmentManager.beginTransaction().remove(popUp!!).commit()
            popUp = NewFragment()
            popUp!!.setListener(this) // Pass an instance of the implemented interface
            popUp!!.show(
                childFragmentManager,
                NewFragment.TAG
            )
        }
    }

    private fun init(view: View) {
        navController = Navigation.findNavController(view)
        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            firestore = FirebaseFirestore.getInstance()
            databaseRef = FirebaseDatabase.getInstance().reference.child("Tasks").child(currentUser.uid)
        } else {
            Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show()
            // You might want to navigate to the login screen here
        }

        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        mList = mutableListOf()
        adapter = ToDoAdapter(mList)
        adapter.setListener(this) // Set the listener for the adapter
        binding.recyclerView.adapter = adapter
    }

    override fun onSaveTask(todo: String, todoEt: TextInputEditText) {
        val currentUser = auth.currentUser
        currentUser?.let {
            // Save to Firebase Realtime Database
            val newTaskRef = databaseRef.push()
            newTaskRef.setValue(todo).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(context, "Saved Successfully to Firebase Realtime Database", Toast.LENGTH_SHORT).show()
                    todoEt.text = null
                } else {
                    Toast.makeText(context, task.exception?.message, Toast.LENGTH_SHORT).show()
                }
            }

            // Save to Firestore
            val task = hashMapOf(
                "task" to todo
            )
            firestore.collection("Tasks").document(currentUser.uid).collection("UserTasks")
                .add(task)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(context, "Saved Successfully to Firestore", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, task.exception?.message, Toast.LENGTH_SHORT).show()
                    }
                }
        } ?: run {
            Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onUpdateTask(toDoData: ToDoData, todoEt: TextInputEditText) {
        val currentUser = auth.currentUser
        currentUser?.let {
            // Update in Firebase Realtime Database
            val map = HashMap<String, Any>()
            map[toDoData.taskId] = toDoData.task
            databaseRef.updateChildren(map).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(context, "Updated Successfully in Firebase Realtime Database", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, task.exception?.message, Toast.LENGTH_SHORT).show()
                }
            }

            // Update in Firestore
            val task = hashMapOf(
                "task" to toDoData.task
            )
            firestore.collection("Tasks").document(currentUser.uid).collection("UserTasks")
                .document(toDoData.taskId)
                .set(task)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(context, "Updated Successfully in Firestore", Toast.LENGTH_SHORT).show()
                        todoEt.text = null
                        popUp!!.dismiss()
                    } else {
                        Toast.makeText(context, task.exception?.message, Toast.LENGTH_SHORT).show()
                    }
                }
        } ?: run {
            Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getDataFromFirebase() {
        if (databaseRef != null) {
            databaseRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    mList.clear()
                    for (taskSnapshot in snapshot.children) {
                        val taskValue = taskSnapshot.getValue(String::class.java)
                        if (taskValue != null) {
                            val todoTask = ToDoData(taskSnapshot.key ?: "", taskValue)
                            mList.add(todoTask)
                        }
                    }
                    adapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun getDataFromFirestore() {
        val currentUser = auth.currentUser
        currentUser?.let {
            firestore.collection("Tasks").document(currentUser.uid).collection("UserTasks")
                .addSnapshotListener { snapshots, e ->
                    if (e != null) {
                        Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                        return@addSnapshotListener
                    }

                    mList.clear()
                    for (document in snapshots!!) {
                        val taskValue = document.getString("task")
                        if (taskValue != null) {
                            val todoTask = ToDoData(document.id, taskValue)
                            mList.add(todoTask)
                        }
                    }
                    adapter.notifyDataSetChanged()
                }
        } ?: run {
            Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDeleteTaskBtnClicked(toDoData: ToDoData) {
        val currentUser = auth.currentUser
        currentUser?.let {
            // Delete from Firebase Realtime Database
            val databaseRef = FirebaseDatabase.getInstance().reference.child("Tasks").child(currentUser.uid).child(toDoData.taskId)
            databaseRef.removeValue().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(context, "Deleted Successfully from Firebase Realtime Database", Toast.LENGTH_SHORT).show()
                    // Remove the item from the local list
                    mList.remove(toDoData)
                    adapter.notifyDataSetChanged()
                } else {
                    Toast.makeText(context, task.exception?.message, Toast.LENGTH_SHORT).show()
                }
            }

            // Delete from Firestore
            firestore.collection("Tasks").document(currentUser.uid).collection("UserTasks")
                .document(toDoData.taskId)
                .delete()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(context, "Deleted Successfully from Firestore", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, task.exception?.message, Toast.LENGTH_SHORT).show()
                    }
                }
        } ?: run {
            Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onEditTaskBtnClicked(toDoData: ToDoData) {
        if (popUp != null)
            childFragmentManager.beginTransaction().remove(popUp!!).commit()
        popUp = NewFragment.newInstance(toDoData.taskId, toDoData.task)

        popUp!!.setListener(this)
        popUp!!.show(childFragmentManager, NewFragment.TAG)
    }
}
