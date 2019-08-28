package com.leishui.music.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.leishui.music.result.SongBean.Track
import kotlinx.android.synthetic.main.recycler_item_playlist_detail.view.*
import android.os.Build
import android.text.Html
import androidx.annotation.RequiresApi
import com.leishui.music.result.SongBean.SongBean
import com.leishui.music.util.StringUtil


class RecyclerPlayListDetailAdapter : RecyclerView.Adapter<RecyclerPlayListDetailAdapter.RecyclerHolder>() {

    var list = ArrayList<Track>()

    private lateinit var onItemClickListener: OnItemClickListener

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.onItemClickListener = listener
    }

    interface OnItemClickListener {
        fun onItemClick(list: List<Track>, position: Int)
    }

    //刷新 初始数据
    fun updateList(list: List<Track>) {
        this.list.clear()
        this.list.addAll(list)
        notifyDataSetChanged()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerHolder {
        return RecyclerHolder(
            LayoutInflater.from(parent.context).inflate(
                com.leishui.music.R.layout.recycler_item_playlist_detail,
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
        holder.pos.text = (position + 1).toString()
        StringUtil.musicName(data,holder.songName)
        //多个作者
        val info = data.ar[0].name + " - " + data.al.name
        holder.songInfo.text = info
        holder.itemView.setOnClickListener {
            onItemClickListener.onItemClick(list,position)
        }
    }

    class RecyclerHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val pos = itemView.tv_pos!!
        val songName = itemView.tv_song_name!!
        val songInfo = itemView.tv_song_info!!
    }
}