package com.campusfp.remarket.Fragmentos

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.bumptech.glide.manager.Lifecycle
import com.campusfp.remarket.R
import com.campusfp.remarket.databinding.FragmentInicioBinding
import com.campusfp.remarket.databinding.FragmentMisAnunciosBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener

class FragmentMisAnuncios : Fragment() {

    private lateinit var binding : FragmentMisAnunciosBinding
    private lateinit var mContext : Context
    private lateinit var mTabsViewPagerAdapter: MyTabsViewPagerAdapter

    override fun onAttach(context: Context) {
        this.mContext = context
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        binding = FragmentMisAnunciosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Mis Anuncios"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Favoritos"))

        val fragmentManager = childFragmentManager

        mTabsViewPagerAdapter = MyTabsViewPagerAdapter(fragmentManager, lifecycle)
        binding.viewPager.adapter =mTabsViewPagerAdapter

        // Indicar la ventana seleccionada
        binding.tabLayout.addOnTabSelectedListener(object : OnTabSelectedListener{

            override fun onTabSelected(tab: TabLayout.Tab) {
                binding.viewPager.currentItem = tab.position
            }

            override fun onTabUnselected(p0: TabLayout.Tab?) {

            }

            override fun onTabReselected(p0: TabLayout.Tab?) {

            }
        })

        // Mostrar la ventana seleccionada
        binding.viewPager.registerOnPageChangeCallback(object : OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                binding.tabLayout.selectTab(binding.tabLayout.getTabAt(position))
            }
        })
    }

    // Creacion de las ventanas de mis anuncios y anuncios favoritos
    // Si se encuentra en mis anuncios (posicion 0), se muestra su fragment y con anuncios favoritos (posicion 1 ), lo mismo
    class MyTabsViewPagerAdapter (fragmentManager : FragmentManager, lifecycle : androidx.lifecycle.Lifecycle):
        FragmentStateAdapter(fragmentManager, lifecycle){

        override fun createFragment(position: Int): Fragment {

            if (position == 0){
                return Mis_Anuncios_Publicados_Fragment()
            }else{
                return Fav_Anuncios_Fragment()
            }
        }

        override fun getItemCount(): Int {
            return 2
        }
    }
}