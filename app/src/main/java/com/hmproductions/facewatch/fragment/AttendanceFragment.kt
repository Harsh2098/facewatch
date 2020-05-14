package com.hmproductions.facewatch.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.hmproductions.facewatch.FaceWatchClient
import com.hmproductions.facewatch.R
import com.hmproductions.facewatch.adapter.HistoryRecyclerAdapter
import com.hmproductions.facewatch.dagger.ContextModule
import com.hmproductions.facewatch.dagger.DaggerFaceWatchApplicationComponent
import com.hmproductions.facewatch.data.FaceWatchViewModel
import kotlinx.android.synthetic.main.fragment_attendance.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AttendanceFragment : Fragment() {

    @Inject
    lateinit var client: FaceWatchClient

    private lateinit var model: FaceWatchViewModel
    private var adapter: HistoryRecyclerAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        model = activity?.run { ViewModelProvider(this).get(FaceWatchViewModel::class.java) }
            ?: throw Exception("Invalid activity")
        adapter = HistoryRecyclerAdapter(requireContext())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        DaggerFaceWatchApplicationComponent.builder().contextModule(ContextModule(context!!)).build().inject(this)
        return inflater.inflate(R.layout.fragment_attendance, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupAttendanceHistory()
    }

    private fun setupAttendanceHistory() = lifecycleScope.launch {
        val attendanceList = withContext(Dispatchers.IO) {
            model.getAttendance(client)
        }

        historyRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        historyRecyclerView.adapter = adapter
        historyRecyclerView.setHasFixedSize(false)

        if(attendanceList.isNotEmpty()) {
            historyRecyclerView.visibility = View.VISIBLE
            noHistoryLayout.visibility = View.GONE
            adapter?.swapData(attendanceList)
        } else {
            historyRecyclerView.visibility = View.GONE
            noHistoryLayout.visibility = View.VISIBLE
        }
    }
}