package com.leishui.music.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.leishui.music.R
import com.leishui.music.result.PlayListBean.Playlist
import com.leishui.music.result.UserInfoBean.UserInfoBean
import kotlinx.android.synthetic.main.fragment_my.view.*
import kotlinx.android.synthetic.main.recycler_item_playlist.view.*

class RecyclerPlayListAdapter : RecyclerView.Adapter<RecyclerPlayListAdapter.RecyclerHolder>() {

    var list = ArrayList<Playlist>()

    private lateinit var onItemClickListener: OnItemClickListener

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.onItemClickListener = listener
    }

    interface OnItemClickListener {
        fun onItemClick(list: List<Playlist>, position: Int)
    }

    //刷新 初始数据
    fun updateList(list: List<Playlist>) {
        this.list.clear()
        this.list.addAll(list)
        notifyDataSetChanged()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerHolder {
        return RecyclerHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.recycler_item_playlist,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: RecyclerHolder, position: Int) {
        val data = list[position]
        holder.playlistName.text = data.name
        Glide.with(holder.itemView).load(data.coverImgUrl).into(holder.img)
        val string: String
        var playCount = data.playCount.toString()
        if (playCount.length > 4)
            playCount = playCount.substring(0, playCount.length - 4) + "." + playCount[playCount.length - 4] + "万"
        if (data.ordered)
            string = "${data.trackCount}首， by ${data.creator.nickname}， 播放${playCount}次"
        else
            string = "${data.trackCount}首， 播放${playCount}次"
        holder.musicCounts.text = string
        holder.itemView.setOnClickListener {
            onItemClickListener.onItemClick(list,position)
        }
    }

    class RecyclerHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val img = itemView.image_playlist!!
        val playlistName = itemView.tv_playlist_name!!
        val musicCounts = itemView.tv_playlist_musiccount!!
    }
}