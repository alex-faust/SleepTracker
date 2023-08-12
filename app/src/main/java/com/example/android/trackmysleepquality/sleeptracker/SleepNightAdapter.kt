package com.example.android.trackmysleepquality.sleeptracker

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.android.trackmysleepquality.database.SleepNight
import com.example.android.trackmysleepquality.databinding.ListItemSleepNightBinding

//changed from RecyclerView.Adapter to this one to apply the difference between 2 items
//helps you build a rv that's backed by a list
//Without DiffUtil, submitting new list would reset the scroll position to
// beginning which will not be a good user experience. ListAdapter overcomes
// this by enforcing the implementation in a safe and simple way.
class SleepNightAdapter(private val clickListener: SleepNightListener):
                        ListAdapter<SleepNight,
                                SleepNightAdapter.ViewHolder>(SleepNightDiffCallback()) {

    /* since we are subclassing ListAdapter, we dont need to define this, ListAdapter will do this for us
    var data = listOf<SleepNight>()
        set(value) { //knowing when data has changed
            field = value
            notifyDataSetChanged() //tells rv the entire list may be invalid.
            //to fix, we need to tell rv what exactly has changed
        }*/

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        /*val item = getItem(position)
        holder.bind(item)*/
        holder.bind(getItem(position)!!, clickListener)
    }

    //since we are subclassing ListAdapter, we dont need to define this, ListAdapter will do this for us
    //override fun getItemCount() = data.size

    //private constructor so it can only be called inside the class
    class ViewHolder private constructor(
        private val binding: ListItemSleepNightBinding) : RecyclerView.ViewHolder(binding.root) {

        /*
        I did an inline refactor on these because they were not that necessary but were still needed

        val sleepLength: TextView = binding.sleepLength
        val quality: TextView = binding.qualityString
        val qualityImage: ImageView = binding.qualityImage*/

        fun bind(item: SleepNight, clickListener: SleepNightListener) {
            binding.sleep = item
            binding.clickListener = clickListener
            binding.executePendingBindings() //have to use this when binding rvs
        }
        /*

        dont need this anymore because of our binding adapter class.{
            val res = itemView.context.resources
            binding.sleepLength.text = convertDurationToFormatted(
                item.startTimeMilli, item.endTimeMilli, res
            )
            binding.qualityString.text = convertNumericQualityToString(item.sleepQuality, res)
            binding.qualityImage.setImageResource(
                when (item.sleepQuality) {
                    0 -> R.drawable.ic_sleep_0
                    1 -> R.drawable.ic_sleep_1
                    2 -> R.drawable.ic_sleep_2
                    3 -> R.drawable.ic_sleep_3
                    4 -> R.drawable.ic_sleep_4
                    5 -> R.drawable.ic_sleep_5
                    else -> R.drawable.ic_sleep_active
                }
            )
        }*/
        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                //adding binding to viweholder
                val binding = ListItemSleepNightBinding.inflate(layoutInflater, parent, false)
                //val view = layoutInflater.inflate(R.layout.list_item_sleep_night, parent, false)
                //return ViewHolder(view)
                return ViewHolder(binding)
            }
        }
    }

    //first thing is DiffUtil will need to do is decide if items are the same
    class SleepNightDiffCallback: DiffUtil.ItemCallback<SleepNight>() {

        //this one ex: is if you change the position of an item, this one will check the id
        //only check the ids in this call back so it'll know the difference if an item being
        //edited, removed, or moved, or an item being changed
        override fun areItemsTheSame(oldItem: SleepNight, newItem: SleepNight): Boolean {
            return oldItem.nightId == newItem.nightId
        }

        //used to detect if the contents of the item have changed. It checks if 2 items are equal.
        //if it has changed any of its values. will give you a call back in case you want to do a
        // custon check for equality
        override fun areContentsTheSame(oldItem: SleepNight, newItem: SleepNight): Boolean {
            return oldItem == newItem
        }
    }
}

class SleepNightListener(val clickListener: (sleeId: Long) -> Unit){
    fun onClick(night: SleepNight) = clickListener(night.nightId)

}