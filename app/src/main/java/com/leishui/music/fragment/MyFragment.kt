package com.leishui.music.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
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
    private lateinit var adapterMy: RecyclerPlayListAdapter
    private lateinit var adapterCollection: RecyclerPlayListAdapter
    private var isMyOpen = true
    private var isCollectionOpen = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_my, container, false)
    }

    override fun onResume() {
        super.onResume()
        initData()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val uid = context?.let { Model.getUserInfoByShared(it, "uid") }
        val savedList = context?.let { Model.getPlayListByShared(it) }

        adapterMy = RecyclerPlayListAdapter(false, uid!!.toInt())
        adapterCollection = RecyclerPlayListAdapter(true, uid.toInt())

        rv_playlist_collection_music.layoutManager = LinearLayoutManager(context)
        rv_playlist_created_music.layoutManager = LinearLayoutManager(context)
        rv_playlist_collection_music.adapter = adapterCollection
        rv_playlist_created_music.adapter = adapterMy
        if (savedList != null) {
            adapterCollection.updateList(savedList)
            adapterMy.updateList(savedList)
        }
        initData()
        ll_collection_music.setOnClickListener {
            if (isCollectionOpen) {
                rv_playlist_collection_music.isVisible = false
                iv_open_2.setImageResource(R.drawable.ic_close_24dp)
                isCollectionOpen = false
            }else{
                rv_playlist_collection_music.isVisible = true
                iv_open_2.setImageResource(R.drawable.ic_open_24dp)
                isCollectionOpen = true
            }
        }
        ll_created_music.setOnClickListener {
            if (isMyOpen) {
                rv_playlist_created_music.isVisible = false
                iv_open_1.setImageResource(R.drawable.ic_close_24dp)
                isMyOpen = false
            }else{
                rv_playlist_created_music.isVisible = true
                iv_open_1.setImageResource(R.drawable.ic_open_24dp)
                isMyOpen = true
            }
        }
        adapterCollection.setOnItemClickListener(object :
            RecyclerPlayListAdapter.OnItemClickListener {
            override fun onItemClick(list: List<Playlist>, position: Int) {
                val intent = Intent(context, PlayListDetailActivity::class.java)
                intent.putExtra("id", list[position].id.toString())
                intent.putExtra("bacUrl", list[position].coverImgUrl.toString())
                startActivity(intent)
            }
        })
        adapterMy.setOnItemClickListener(object : RecyclerPlayListAdapter.OnItemClickListener {
            override fun onItemClick(list: List<Playlist>, position: Int) {
                val intent = Intent(context, PlayListDetailActivity::class.java)
                intent.putExtra("id", list[position].id.toString())
                intent.putExtra("bacUrl", list[position].coverImgUrl.toString())
                startActivity(intent)
            }
        })
    }

    private fun initData() {
        Model.playList(
            Model.getUserInfoByShared(context!!, "uid"),
            object : Model.CallBack<PlayListBean> {
                override fun onSuccess(response: Response<PlayListBean>) {
                    val list = response.body()!!.playlist
                    adapterMy.updateList(list)
                    adapterCollection.updateList(list)
                    context?.let { Model.savePlayListByShared(it, list) }
                }

                override fun onFailed(t: Throwable) {
                    Toast.makeText(context, t.toString(), Toast.LENGTH_SHORT).show()
                }
            })
    }
}