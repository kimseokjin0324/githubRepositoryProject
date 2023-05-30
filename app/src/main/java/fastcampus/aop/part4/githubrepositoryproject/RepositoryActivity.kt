package fastcampus.aop.part4.githubrepositoryproject

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import fastcampus.aop.part4.githubrepositoryproject.data.database.DataBaseProvider
import fastcampus.aop.part4.githubrepositoryproject.data.entity.GithubRepoEntity
import fastcampus.aop.part4.githubrepositoryproject.databinding.ActivityRepositoryBinding
import fastcampus.aop.part4.githubrepositoryproject.extensions.loadCenterInside
import fastcampus.aop.part4.githubrepositoryproject.utility.RetrofitUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

class RepositoryActivity : AppCompatActivity(), CoroutineScope {

    val job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job
    private lateinit var binding: ActivityRepositoryBinding

    companion object {
        const val REPOSITORY_OWNER_KEY = "REPOSITORY_OWNER_KEY"
        const val REPOSITORY_NAME_KEY = "REPOSITORY_NAME_KEY"
    }

    private val repositoryDao by lazy {
        DataBaseProvider.provideDB(applicationContext).repositoryDao()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRepositoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //intent로 넘어온 REPOSITORY_OWNER_KEY,REPOSITORY_NAME_KEY NULLCHECK
        val repositoryOwner = intent.getStringExtra(REPOSITORY_OWNER_KEY) ?: kotlin.run {
            toast("REPOSITORY_OWNER_KEY 이름이 없습니다")
            finish()
            return
        }
        val repositoryName = intent.getStringExtra(REPOSITORY_NAME_KEY) ?: kotlin.run {
            toast("REPOSITORY_NAME_KEY 이름이 없습니다")
            finish()
            return
        }
        launch {
            loadRepository(
                repositoryOwner, repositoryName
            )?.let {
                setData(it)
            } ?: run {
                toast("Repository 정보가 없습니다")
                finish()
            }

        }

    }

    private suspend fun loadRepository(
        repositoryOwner: String,
        repositoryName: String
    ): GithubRepoEntity? = with(coroutineContext) {
        var repositoryEntity: GithubRepoEntity? = null
        withContext(Dispatchers.IO) {
            val response = RetrofitUtil.githubApiService.getRepository(
                ownerLogin = repositoryOwner,
                repoName = repositoryName
            )
            if (response.isSuccessful) {
                val body = response.body()
                withContext(Dispatchers.Main) {
                    body?.let { repo ->
                        repositoryEntity = repo
                    }
                }
            }
        }
        repositoryEntity
    }


    private fun setData(githubRepoEntity: GithubRepoEntity) = with(binding) {
        showLoading(false)
        Log.e("data", githubRepoEntity.toString())
        ownerProfileImageView.loadCenterInside(githubRepoEntity.owner.avatarUrl, 42f)
        ownerNameAndRepoNameTextView.text =
            "${githubRepoEntity.owner.login}/${githubRepoEntity.name}"
        stargazersCountText.text = githubRepoEntity.stargazersCount.toString()
        githubRepoEntity.language?.let { language ->
            languageText.isGone = false
            languageText.text = language
        } ?: kotlin.run {
            languageText.isGone = true
            languageText.text = ""
        }
        descriptionTextView.text = githubRepoEntity.description
        updateTimeTextView.text = githubRepoEntity.updatedAt

        setLikeState(githubRepoEntity)

    }

    private fun setLikeState(githubRepoEntity: GithubRepoEntity) = launch {
        with(Dispatchers.IO) {
            val repository = repositoryDao.getRepository(githubRepoEntity.fullName)
            val isLike = repository != null
            withContext(Dispatchers.Main) {
                setLikeImage(isLike)
                binding.likeButton.setOnClickListener {
                    likeGithubRepository(githubRepoEntity, isLike)
                }
            }
        }
    }

    private fun setLikeImage(isLike: Boolean) {
        binding.likeButton.setImageDrawable(
            ContextCompat.getDrawable(
                this,
                if (isLike) {
                    R.drawable.ic_like
                } else {
                    R.drawable.ic_dislike
                }
            )
        )
    }

    private fun likeGithubRepository(githubRepoEntity: GithubRepoEntity, isLike: Boolean) = launch {
        withContext(Dispatchers.IO) {
            if (isLike) {
                repositoryDao.remove(githubRepoEntity.fullName)
            } else {
                repositoryDao.insert(githubRepoEntity)
            }
            withContext(Dispatchers.Main) {
                setLikeImage(isLike.not())
            }
        }

    }

    private fun showLoading(isShown: Boolean) = with(binding) {
        progressBar.isGone = isShown.not()
    }

    private fun Context.toast(message: String) =
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}