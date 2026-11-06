package com.santi.mucom88

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val pager = ViewPager2(this)
        pager.adapter = PageAdapter()
        pager.offscreenPageLimit = 1
        setContentView(pager)
    }
}

class PageAdapter : RecyclerView.Adapter<PageAdapter.Holder>() {

    class Holder(v: View) : RecyclerView.ViewHolder(v)

    override fun getItemCount(): Int = 2

    override fun getItemViewType(position: Int): Int = position

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val v: View = if (viewType == 0) EditorPage(parent.context) else ToneEditorPage(parent.context)
        v.layoutParams = ViewGroup.LayoutParams(MATCH, MATCH)
        return Holder(v)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {}
}
