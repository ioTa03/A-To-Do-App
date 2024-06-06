package com.example.todo1.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.todo1.databinding.FragmentNewBinding
import com.example.todo1.utils.ToDoData
import com.google.android.material.textfield.TextInputEditText

class NewFragment : DialogFragment() {

    private lateinit var binding: FragmentNewBinding
    private lateinit var listener: NextBtnClickListener
    private var toDoData: ToDoData?=null
    fun setListener(listener: NextBtnClickListener) {
        this.listener = listener
    }

    companion object{
        const val TAG="NewFragment"

        @JvmStatic
        fun newInstance(taskId: String, task:String)=NewFragment().apply {
            arguments=Bundle().apply {
                putString("taskId",taskId)
                putString("task",task)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if(arguments!=null){
            toDoData=ToDoData(arguments?.getString("taskId").toString(),
                arguments?.getString("task").toString()
            )

            binding.toDoo.setText(toDoData?.task)
        }

        newEvents()
    }

    private fun newEvents() {
        binding.toDooNext.setOnClickListener {
            val textt = binding.toDoo.text.toString()
            if (textt.isNotEmpty()) {

                if(toDoData==null){
                    listener.onSaveTask(textt, binding.toDoo)
                }
                else{
                    toDoData?.task=textt
                    listener.onUpdateTask(toDoData!!,binding.toDoo)
                }

            } else {
                Toast.makeText(context, "Add Some Task, Invalid", Toast.LENGTH_SHORT).show()
            }
        }

        binding.toDooClose.setOnClickListener {
            dismiss()
        }
    }

    interface NextBtnClickListener {
        fun onSaveTask(todo: String, todoEt: TextInputEditText)
        fun onUpdateTask(toDoData: ToDoData, todoEt: TextInputEditText)
//       pass tododata for update
    }
}
