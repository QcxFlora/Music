package com.leishui.music.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.leishui.music.Model
import com.leishui.music.R
import com.leishui.music.activity.PlayListDetailActivity
import com.leishui.music.adapter.RecyclerPlayListAdapter
import com.leishui.music.result.PlayListBean.PlayListBean
import com.leishui.music.result.PlayListBean.Playlist
import kotlinx.android.synthetic.main.fragment_my.*
import retrofit2.Response

class MyFragment : Fragment() {
    val adapter = RecyclerPlayListAdapter()
    val savedList = context?.let { Model.getPlayListByShared(it) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_my, container, false)
    }

    override fun onResume() {
        super.onResume()
        initData()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recycler_playlist.layoutManager = LinearLayoutManager(context)
        recycler_playlist.adapter = adapter
        if (savedList != null)
            adapter.updateList(savedList)
        initData()
        adapter.setOnItemClickListener(object : RecyclerPlayListAdapter.OnItemClickListener {
            override fun onItemClick(list: List<Playlist>, position: Int) {
                val intent = Intent(context, PlayListDetailActivity::class.java)
                intent.putExtra("id", list[position].id.toString())
                intent.putExtra("bacUrl", list[position].coverImgUrl.toString())
                startActivity(intent)
            }
        })
    }

    private fun initData() {
        Model.playList(Model.getUserInfoByShared(context!!, "uid"), object : Model.CallBack<PlayListBean> {
            override fun onSuccess(response: Response<PlayListBean>) {
                val list = response.body()!!.playlist
                adapter.updateList(list)
                context?.let { Model.savePlayListByShared(it, list) }
            }

            override fun onFailed(t: Throwable) {
                Toast.makeText(context, t.toString(), Toast.LENGTH_SHORT).show()
            }
        })
    }
}