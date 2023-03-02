package org.listenbrainz.android.ui.screens.newsbrainz

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.aemerse.share.SharableItem
import com.aemerse.share.Share
import dagger.hilt.android.AndroidEntryPoint
import org.listenbrainz.android.R
import org.listenbrainz.android.model.Post
import org.listenbrainz.android.databinding.ActivityNewsbrainzBinding
import org.listenbrainz.android.util.IntentFactory
import org.listenbrainz.android.util.Log.d
import org.listenbrainz.android.util.Log.e
import org.listenbrainz.android.viewmodel.NewsListViewModel

@AndroidEntryPoint
class NewsBrainzActivity : AppCompatActivity(), BlogAdapter.ClickListener {

    private lateinit var binding: ActivityNewsbrainzBinding
    private var viewModel: NewsListViewModel? = null
    private var adapter: BlogAdapter? = null
    private lateinit var postsList: ArrayList<Post>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewsbrainzBinding.inflate(layoutInflater)

        supportActionBar!!.setBackgroundDrawable(ColorDrawable(resources.getColor(R.color.app_bg)))
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.title = "NewsBrainz"
        setContentView(binding.root)

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        viewModel = ViewModelProvider(this)[NewsListViewModel::class.java]

        postsList  = ArrayList()
        adapter = BlogAdapter(postsList, this)
        binding.recyclerView.adapter = adapter
        binding.loadingAnimation.root.visibility = VISIBLE

        viewModel!!.fetchBlogs().observe(this) {
            postsList.clear()
            postsList.addAll(it.posts)
            adapter!!.notifyDataSetChanged()
            binding.loadingAnimation.root.visibility = GONE
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.dash, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_preferences -> {
                startActivity(IntentFactory.getSettings(this))
                true
            }
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()     // Replaced onBackPressed() as its deprecated.
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onUserClicked(position: Int) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(postsList[position].URL)))
    }

    override fun onUserLongClicked(position: Int) {
        Share.with(context = this)
            .item(SharableItem(
                pictureUrl = null,
                data = postsList[position].URL + "\n",
                shareAppLink = true,
                downloadOurAppMessage = "Download our app"
            ),
                onStart = {
                    d( "onStart Sharing")
                },
                onFinish = { isSuccessful: Boolean, errorMessage: String ->
                    // if isSuccessful you will see an intent chooser else check the error message
                    when {
                        isSuccessful -> {
                            e("Successfully shared")
                        }
                        else -> {
                            e("error happened : $errorMessage")
                        }
                    }
                }
            )
    }
}