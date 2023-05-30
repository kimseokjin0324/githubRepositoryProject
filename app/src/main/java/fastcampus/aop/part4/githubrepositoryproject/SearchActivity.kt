package fastcampus.aop.part4.githubrepositoryproject

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import fastcampus.aop.part4.githubrepositoryproject.data.entity.GithubRepoEntity
import fastcampus.aop.part4.githubrepositoryproject.databinding.ActivitySearchBinding
import fastcampus.aop.part4.githubrepositoryproject.utility.RetrofitUtil
import fastcampus.aop.part4.githubrepositoryproject.view.RepositoryRecyclerAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

class SearchActivity : AppCompatActivity(), CoroutineScope {

    private val job = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    private lateinit var binding: ActivitySearchBinding
    private lateinit var adapter: RepositoryRecyclerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initAdapter()
        initViews()
        bindViews()
    }

    //Adapter 초기화
    private fun initAdapter() = with(binding) {
        adapter = RepositoryRecyclerAdapter()

    }

    private fun initViews() = with(binding) {
        emptyResultTextView.isGone = true
        recyclerView.adapter = adapter
    }

    private fun bindViews() = with(binding) {
        searchButton.setOnClickListener {
            searchKeyword(searchBarInputView.text.toString())
        }
    }

    private fun searchKeyword(keywordString: String) = launch {
        //검색시 withContext를 이용해서(IO)로 처리
        withContext(Dispatchers.IO) {
            //response를 처리
            val response = RetrofitUtil.githubApiService.searchRepositories(keywordString)
            if (response.isSuccessful) {
                val body = response.body()
                withContext(Dispatchers.Main) {
                    Log.e("response", body.toString())
                    body?.let { searchResponse ->
                        setData(searchResponse.items)
                    }
                }
            }
        }

    }

    private fun setData(items: List<GithubRepoEntity>) {
        adapter.setSearchResultList(items) {
            Toast.makeText(this, "이름 : ${it.fullName}", Toast.LENGTH_SHORT).show()
            startActivity(
                Intent(this@SearchActivity, RepositoryActivity::class.java).apply {
                    putExtra(RepositoryActivity.REPOSITORY_OWNER_KEY, it.owner.login)
                    putExtra(RepositoryActivity.REPOSITORY_NAME_KEY, it.name)
                }
            )
        }
    }
}