package com.mrjaya.sub2

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.telecom.Call
import android.util.Log
import android.view.View
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.mrjaya.sub2.databinding.ActivityHomeBinding
import retrofit2.Response
import javax.security.auth.callback.Callback

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private val title: String = "Github Search"
    val listUser = MutableLiveData<ArrayList<ItemsItem>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = title

        val layoutManager = LinearLayoutManager(this)
        binding.rvUsers.layoutManager = layoutManager
        val itemDecoration = DividerItemDecoration(this, layoutManager.orientation)
        binding.rvUsers.addItemDecoration(itemDecoration)
        val adapter = UserAdapter()
        adapter.notifyDataSetChanged()
        binding.rvUsers.adapter = adapter

        adapter.setOnItemClickCallback(object : UserAdapter.OnItemClickCallback {
            override fun onItemClicked(data: ItemsItem) {
                Intent(this@HomeActivity, DetailActivity::class.java).also {
                    it.putExtra(DetailActivity.EXTRA_USER, data.login)
                    startActivity(it)
                }
            }
        })

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                if (query.isEmpty())
                    showLoading(true)
                findUsers(query)
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                findUsers(newText)
                return false
            }
        })

        getSearchUsers().observe(this, {
            if (it != null) {
                adapter.setList(it)
                showLoading(false)
            }
        })

    }

    private fun findUsers(query: String) {
        showLoading(true)
        val client = ApiConfig.getApiService().getSearchUsers(query)
        client.enqueue(object : retrofit2.Callback<UserResponse> {
            override fun onResponse(
                call: retrofit2.Call<UserResponse>,
                response: Response<UserResponse>
            ) {
                showLoading(false)
                if (response.isSuccessful) {
                    listUser.postValue(response.body()?.items as ArrayList<ItemsItem>?)
                } else {
                    Log.e(ContentValues.TAG, "onFailure: ${response.message()}")
                }
            }

            override fun onFailure(call: retrofit2.Call<UserResponse>, t: Throwable) {
                showLoading(false)
                Log.e(ContentValues.TAG, "onFailure: ${t.message}")
            }
        })
    }

    private fun getSearchUsers(): LiveData<ArrayList<ItemsItem>> {
        return listUser
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.progressBar.visibility = View.GONE
        }
    }
}