import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.outkeeper.Person
import com.example.outkeeper.PersonActivity
import com.example.outkeeper.R


class PersonAdapter(private val dataSet: List<Person>,private val activity: Activity) :

    RecyclerView.Adapter<PersonAdapter.ViewHolder>() {

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // Initializes views used by person highlight
        val textView: TextView
        val imageView: ImageView
        val row_item: LinearLayout
        init {
            // Define click listener for the ViewHolder's View
            textView = view.findViewById(R.id.itemTextViev_persons_name)
            row_item = view.findViewById(R.id.person_row_item)
            imageView = view.findViewById(R.id.itemImageView)
            //textView.setOnClickListener(PersonClickListener(p("22", listOf("SS"),Date(),true):Person,9))
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.person_row_item, viewGroup, false)
        return ViewHolder(view)
    }



    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.textView.text = dataSet[position].name
        var person_profile_picture = dataSet[position].photos[0]
        println(person_profile_picture)
        Glide.with(viewHolder.imageView)
            .load(person_profile_picture)
            .apply(RequestOptions().override(600, 200))
            .into(viewHolder.imageView)


        viewHolder.row_item.setOnClickListener { v ->
            val intent = Intent(activity, PersonActivity::class.java)
            intent.putExtra("person",dataSet[position])
            activity.startActivityForResult(intent,2)
        }

    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size




}


