package com.mrjaya.sub2

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.mrjaya.sub2.databinding.ActivityDetailBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_USER = "extra_user"

        @StringRes
        private val TAB_TITLES = intArrayOf(
            R.string.followers,
            R.string.following
        )
    }

    private lateinit var binding: ActivityDetailBinding
    val user = MutableLiveData<DetailResponse>()
    private val title: String = "Github Search Detail"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = title
        val username = intent.getStringExtra(EXTRA_USER)

        val bundle = Bundle()
        bundle.putString(EXTRA_USER, username)

        val sectionsPagerAdapter = SectionPagerAdapter(this, bundle)
        val viewPager: ViewPager2 = findViewById(R.id.view_pager)
        viewPager.adapter = sectionsPagerAdapter
        val tabs: TabLayout = findViewById(R.id.tabs)
        TabLayoutMediator(tabs, viewPager) { tab, position ->
            tab.text = resources.getString(TAB_TITLES[position])
        }.attach()

        supportActionBar?.elevation = 0f

        if (username != null) {
            setUserDetail(username)
        }

        getUserDetail().observe(this, {
            if (it != null) {
                binding.apply {
                    Glide.with(this@DetailActivity)
                        .load(it.avatarUrl)
                        .into(profilePhoto)
                    profileName.text = it.name
                    profileUsername.text = it.login
                    followers.text = "${it.followers}"
                    following.text = "${it.following}"
                }
            }
        })


    }

    private fun setUserDetail(username: String) {
        val client = ApiConfig.getApiService().getUserDetail(username)
        client.enqueue(object : Callback<DetailResponse> {
            override fun onResponse(
                call: Call<DetailResponse>,
                response: Response<DetailResponse>
            ) {
                if (response.isSuccessful) {
                    user.postValue(response.body())
                } else {
                    Log.e(ContentValues.TAG, "onFailure: ${response.message()}")
                }
            }
            override fun onFailure(call: Call<DetailResponse>, t: Throwable) {
                Log.e(ContentValues.TAG, "onFailure: ${t.message}")
            }
        })
    }

    private fun getUserDetail() : LiveData<DetailResponse> {
        return user
    }
}