package com.hmproductions.facewatch.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.hmproductions.facewatch.dagger.ContextModule

class HomeFragment: Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        DaggerFaceWatchApplicationComponent.builder().contextModule(ContextModule(context!!)).build().inject(this)
        return inflater.inflate(R.layout.fragment_inner, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        carParkAdapter = CarParkSmallAdapter(context, null, this, preferences.getBoolean(getString(R.string.prediction_pref_key), false))

        innerRecyclerView.adapter = carParkAdapter
        innerRecyclerView.layoutManager = GridLayoutManager(context, MAX_COL_SPAN)
        innerRecyclerView.setHasFixedSize(false)
        innerRecyclerView.itemAnimator = FlipAnimator()

        innerSwipeRefreshLayout.setOnRefreshListener(this)

        getLiveCarParkDetails()
    }
}