package fastcampus.aop.part4.githubrepositoryproject

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import fastcampus.aop.part4.githubrepositoryproject.data.database.DataBaseProvider
import fastcampus.aop.part4.githubrepositoryproject.data.entity.GithubOwner
import fastcampus.aop.part4.githubrepositoryproject.data.entity.GithubRepoEntity
import fastcampus.aop.part4.githubrepositoryproject.databinding.ActivityMainBinding
import fastcampus.aop.part4.githubrepositoryproject.view.RepositoryRecyclerAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date
import kotlin.coroutines.CoroutineContext

class MainActivity : AppCompatActivity(), CoroutineScope {

    val job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job


    private val repositoryDao by lazy {
        DataBaseProvider.provideDB(applicationContext).repositoryDao()
    }
    private lateinit var adapter: RepositoryRecyclerAdapter

    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initAdapter()
        initViews()


    }


    private fun initAdapter() {
        adapter = RepositoryRecyclerAdapter()
    }

    override fun onResume() {
        super.onResume()
        launch(coroutineContext) {
            loadLikedRepositoryList()
        }
    }

    private suspend fun loadLikedRepositoryList() = withContext(Dispatchers.IO) {
        val repoList = repositoryDao.getHistory()
        withContext(Dispatchers.Main) {
            setData(repoList)
        }
    }

    private fun setData(githubRepositoryList: List<GithubRepoEntity>) = with(binding) {
        //받은리스트 nullcheck
        if (githubRepositoryList.isEmpty()) {
            emptyResultTextView.isGone = false
            recyclerView.isGone = true
        } else {
            emptyResultTextView.isGone = true
            recyclerView.isGone = false
            adapter.setSearchResultList(githubRepositoryList) {
                startActivity(
                    Intent(this@MainActivity, RepositoryActivity::class.java).apply {
                        putExtra(RepositoryActivity.REPOSITORY_OWNER_KEY, it.owner.login)
                        putExtra(RepositoryActivity.REPOSITORY_NAME_KEY, it.name)
                    }
                )
            }
        }
    }

    private fun initViews() = with(binding) {
        recyclerView.adapter = adapter
        searchButton.setOnClickListener {
            startActivity(
                Intent(this@MainActivity, SearchActivity::class.java)
            )
        }
    }

//    private suspend fun addMockData() = withContext(Dispatchers.IO) {
//        val mockData = (0 until 10).map {
//            GithubRepoEntity(
//                name = "repo $it",
//                fullName = "name $it",
//                owner = GithubOwner(
//                    "login",
//                    "avatarUrl"
//                ),
//                description = null,
//                language = null,
//                updatedAt = Date().toString(),
//                stargazersCount = it
//            )
//        }
//        repositoryDao.insertAll(mockData)
//
//    }


}
