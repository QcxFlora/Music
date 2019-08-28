package com.leishui.music.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.leishui.music.fragment.FindFragment
import com.leishui.music.fragment.MyFragment
import com.leishui.music.fragment.VideoFragment
import com.leishui.music.fragment.YuncunFragment

class MainViewPageAdapter(fragmentManager: FragmentManager) :FragmentPagerAdapter(fragmentManager){
    override fun getItem(position: Int): Fragment {
        return when(position){
            0->MyFragment()
            1->FindFragment()
            2->YuncunFragment()
            3->VideoFragment()
            else -> MyFragment()
        }
    }

    override fun getCount(): Int {
        return 4
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return when(position){
            0->"我的"
            1->"发现"
            2->"云村"
            3->"视频"
            else -> ""
        }
    }
}