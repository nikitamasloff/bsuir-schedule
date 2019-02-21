package com.nikitamaslov.bsuirschedule.ui.base

import android.support.annotation.CallSuper
import android.support.annotation.LayoutRes
import android.view.ViewStub
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import android.widget.ProgressBar
import com.nikitamaslov.bsuirschedule.R

abstract class BaseLazyFragment : Fragment() {

    private var savedInstanceState: Bundle? = null
    private var hasInflated = false
    private var viewStub: ViewStub? = null

    @get:LayoutRes
    protected abstract val viewStubLayoutResId: Int

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_lazy, container, false)
        viewStub = view.findViewById(R.id.fragment_view_stub)
        viewStub?.layoutResource = viewStubLayoutResId
        this.savedInstanceState = savedInstanceState

        if (userVisibleHint && !hasInflated) {
            val inflatedView = viewStub!!.inflate()
            onCreateViewAfterViewStubInflated(inflatedView, this.savedInstanceState)
            afterViewStubInflated(view)
        }

        return view
    }

    protected abstract fun onCreateViewAfterViewStubInflated(inflatedView: View, savedInstanceState: Bundle?)

    @CallSuper
    protected open fun afterViewStubInflated(originalViewContainerWithViewStub: View?) {
        hasInflated = true
        if (originalViewContainerWithViewStub != null) {
            val pb: ProgressBar = originalViewContainerWithViewStub.findViewById(R.id.inflate_progress_bar)
            pb.visibility = View.GONE
        }
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)

        if (isVisibleToUser && viewStub != null && !hasInflated) {
            val inflatedView = viewStub!!.inflate()
            onCreateViewAfterViewStubInflated(inflatedView, savedInstanceState)
            afterViewStubInflated(view)
        }
    }

    override fun onDetach() {
        super.onDetach()
        hasInflated = false
    }

}