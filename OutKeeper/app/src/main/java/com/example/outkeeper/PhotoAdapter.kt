import android.content.Intent
import android.view.LayoutInflater
import android.view.View

import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.outkeeper.Person
import com.example.outkeeper.PersonActivity
import com.example.outkeeper.R


// Show persons all photos
class PhotoAdapter(private val photo_links: List<String>) :

    RecyclerView.Adapter<PhotoAdapter.ViewHolder>() {

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView : ImageView

        init {
            imageView = view.findViewById(R.id.imageView)
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.single_photo, viewGroup, false)


        return ViewHolder(view)
    }



    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        Glide.with(viewHolder.imageView)
            .load(photo_links[position])
            .diskCacheStrategy(DiskCacheStrategy.NONE) // Disable caching
            .skipMemoryCache(true) // Disable memory cache
            .apply(RequestOptions().override(1000, 500))
            .into(viewHolder.imageView)
    println(photo_links[position])



    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = photo_links.size




}


