package com.leishui.music.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.leishui.music.Model
import com.leishui.music.R
import com.leishui.music.adapter.RecyclerPlayListDetailAdapter
import com.leishui.music.result.SongBean.SongBean
import com.leishui.music.result.SongBean.Track
import com.leishui.music.result.SongUrlBean.SongUrlBean
import kotlinx.android.synthetic.main.activity_play_list_detail.*
import retrofit2.Response

class PlayListDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play_list_detail)
        val adapter = RecyclerPlayListDetailAdapter()
        recycler_song.layoutManager = LinearLayoutManager(this)
        recycler_song.adapter = adapter
        val url = intent.getStringExtra("bacUrl")
        val id = intent.getStringExtra("id")!!
        Glide.with(this).load(url).into(image_background)
        val savedList = Model.getMusicListByShared(this, id)
        if (savedList != null)
            adapter.updateList(savedList)
        Model.playlistSong(id, object : Model.CallBack<SongBean> {
            override fun onSuccess(response: Response<SongBean>) {
                val list = response.body()!!.playlist.tracks
                adapter.updateList(list)
                Model.saveMusicListByShared(this@PlayListDetailActivity, list, id)
            }

            override fun onFailed(t: Throwable) {
                Toast.makeText(this@PlayListDetailActivity, t.toString(), Toast.LENGTH_SHORT).show()
                println("----------------->>>>>>>>>>>>>>>>>>>>$t")
            }

        })
        adapter.setOnItemClickListener(object : RecyclerPlayListDetailAdapter.OnItemClickListener {
            override fun onItemClick(list: List<Track>, position: Int) {
                val intent = Intent(this@PlayListDetailActivity, PlayActivity::class.java)
                intent.putExtra("alUrl", list[position].al.picUrl)
                Model.songUrl(list[position].id.toString(), object : Model.CallBack<SongUrlBean> {
                    override fun onSuccess(response: Response<SongUrlBean>) {
                        val url = response.body()!!.data[0].url
                        if (url != null) {
                            Model.saveCurrentListIdByShared(this@PlayListDetailActivity, id)
                            intent.putExtra("position", position.toString())
                            startActivity(intent)
                        } else {
                            Toast.makeText(this@PlayListDetailActivity, "音乐已失效", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailed(t: Throwable) {
                        Toast.makeText(this@PlayListDetailActivity, t.toString(), Toast.LENGTH_SHORT).show()
                    }

                })
            }

        })
    }
}
