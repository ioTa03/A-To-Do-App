import android.text.Editable
import com.example.todo1.utils.ToDoData
import android.view.ViewGroup
import android.view.LayoutInflater

import androidx.recyclerview.widget.RecyclerView
import com.example.todo1.Fragments.HomeFragment
import com.example.todo1.databinding.EachTodoItemBinding

class ToDoAdapter(private val mList: MutableList<ToDoData>):
    RecyclerView.Adapter<ToDoAdapter.ToDoViewHolder>() {

    private var listener: ToDoAdapterClicksInterface? = null

    fun setListener(listener: HomeFragment) {
        this.listener = listener
    }

    inner class ToDoViewHolder(binding: EachTodoItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val binding = binding
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ToDoViewHolder {
        val binding = EachTodoItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ToDoViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    override fun onBindViewHolder(holder: ToDoViewHolder, position: Int) {
        with(holder) {
            with(mList[position]) {
                binding.todoTask.text = Editable.Factory.getInstance().newEditable(this.task)
                binding.deleteTask.setOnClickListener {
                    listener?.onDeleteTaskBtnClicked(this)
                }
                binding.editTask.setOnClickListener {
                    listener?.onEditTaskBtnClicked(this)
                }
//                binding.root.setOnClickListener {
//                    listener?.onItemClicked(this) // Call the onItemClicked method from the listener
//                }
            }
        }
    }
    interface ToDoAdapterClicksInterface {
        fun onDeleteTaskBtnClicked(toDoData: ToDoData)
        fun onEditTaskBtnClicked(toDoData: ToDoData)
    }
}

