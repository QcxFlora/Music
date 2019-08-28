package com.leishui.music.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.leishui.music.fragment.FindFragment
import com.leishui.music.fragment.MyFragment
import com.leishui.music.fragment.YuncunFragment

class UserInfoViewPageAdapter(fragmentManager: FragmentManager): FragmentPagerAdapter(fragmentManager) {
    override fun getItem(position: Int): Fragment {
        return when(position){
            0-> MyFragment()
            1-> FindFragment()
            2-> YuncunFragment()
            else -> MyFragment()
        }
    }

    override fun getCount(): Int {
        return 3
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return when(position){
            0->"音乐"
            1->"动态"
            2->"关于我"
            else -> ""
        }
    }
}