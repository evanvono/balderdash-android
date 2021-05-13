package com.evanvonoehsen.balderdash.adapter

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.evanvonoehsen.balderdash.FragmentChooseWord
import com.evanvonoehsen.balderdash.FragmentMenu
import com.evanvonoehsen.balderdash.FragmentPastWords

class ViewPagerAdapter (activity: AppCompatActivity, val itemsCount: Int) :
    FragmentStateAdapter(activity) {

    override fun getItemCount(): Int {
        return itemsCount
    }

    override fun createFragment(position: Int): Fragment {
        return if (position == 0) {
            FragmentMenu.newInstance()
        } else {
            FragmentPastWords.newInstance()
        }
    }
}