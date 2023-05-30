package fastcampus.aop.part4.githubrepositoryproject.data.response

import fastcampus.aop.part4.githubrepositoryproject.data.entity.GithubRepoEntity

data class GithubRepoSearchResponse(
    val totalCount: Int,
    val items: List<GithubRepoEntity>
)