package com.leishui.music.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.leishui.music.R
import com.leishui.music.result.PlayListBean.Playlist
import kotlinx.android.synthetic.main.recycler_item_playlist.view.*

open class RecyclerPlayListAdapter(private val type: Boolean, private val uid: Int) :
    RecyclerView.Adapter<RecyclerPlayListAdapter.RecyclerHolder>() {

    var list = ArrayList<Playlist>()

    private var onItemClickListener: OnItemClickListener? = null

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
        val temp = getCount()
        if (!type)
            return temp
        else
            return list.size - temp
    }

    private fun getCount(): Int {
        var temp = 0
        for (i in 0 until list.size) {
            if (list[i].creator.userId == uid) {
                temp++
            }
        }
        return temp
    }

    override fun onBindViewHolder(holder: RecyclerHolder, position: Int) {
        var data = list[position]
        if (type) {
            data = list[position + getCount()]
            holder.itemView.setOnClickListener {
                onItemClickListener?.onItemClick(list, position + getCount())
            }
        } else holder.itemView.setOnClickListener {
            onItemClickListener?.onItemClick(list, position)
        }

        holder.playlistName.text = data.name
        Glide.with(holder.itemView).load(data.coverImgUrl).into(holder.img)
        val string: String
        var playCount = data.playCount.toString()
        if (playCount.length > 4)
            playCount = playCount.substring(
                0,
                playCount.length - 4
            ) + "." + playCount[playCount.length - 4] + "万"
        if (data.ordered)
            string = "${data.trackCount}首， by ${data.creator.nickname}， 播放${playCount}次"
        else
            string = "${data.trackCount}首， 播放${playCount}次"
        holder.musicCounts.text = string
    }

    class RecyclerHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val img = itemView.image_playlist!!
        val playlistName = itemView.tv_playlist_name_item!!
        val musicCounts = itemView.tv_playlist_musicinfo_item!!
    }
}