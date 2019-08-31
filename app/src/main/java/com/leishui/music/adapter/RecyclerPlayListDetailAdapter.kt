package com.leishui.music.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.ColorUtils
import androidx.recyclerview.widget.RecyclerView
import com.leishui.music.result.SongBean.Track
import kotlinx.android.synthetic.main.recycler_item_playlist_detail.view.*
import com.leishui.music.util.StringUtil


class RecyclerPlayListDetailAdapter() :
    RecyclerView.Adapter<RecyclerPlayListDetailAdapter.RecyclerHolder>() {

    private var list = ArrayList<Track>()
    private var playingPosition = -1
    private lateinit var onItemClickListener: OnItemClickListener

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.onItemClickListener = listener
    }

    interface OnItemClickListener {
        fun onItemClick(list: List<Track>, position: Int)
    }

    fun setItemBackgroud(position: Int) {
        playingPosition = position
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
        if (position == playingPosition)
            holder.itemView.setBackgroundColor(Color.WHITE)
        holder.pos.text = (position + 1).toString()
        StringUtil.musicName(data, holder.songName)
        //多个作者
        val info = data.ar[0].name + " - " + data.al.name
        holder.songInfo.text = info
        holder.itemView.setOnClickListener {
            onItemClickListener.onItemClick(list, position)
        }
    }

    class RecyclerHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val pos = itemView.tv_pos_detail_item!!
        val songName = itemView.tv_name_detail_item!!
        val songInfo = itemView.tv_music_info_detail_item!!
    }
}