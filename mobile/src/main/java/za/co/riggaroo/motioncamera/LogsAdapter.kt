package za.co.riggaroo.motioncamera

import android.view.LayoutInflater
import android.view.ViewGroup
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions

/**
 * @author rebeccafranks
 * @since 2017/09/20.
 */
class LogsAdapter(options: FirebaseRecyclerOptions<FirebaseImageLog>) : FirebaseRecyclerAdapter<FirebaseImageLog, LogsViewHolder>(options) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogsViewHolder {
        return LogsViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_log, parent, false))
    }

    override fun onBindViewHolder(holder: LogsViewHolder, position: Int, model: FirebaseImageLog) {
        holder.setLog(model)
    }
}
