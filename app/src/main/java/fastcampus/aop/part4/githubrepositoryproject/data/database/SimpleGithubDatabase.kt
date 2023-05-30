package fastcampus.aop.part4.githubrepositoryproject.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import fastcampus.aop.part4.githubrepositoryproject.data.dao.RepositoryDao
import fastcampus.aop.part4.githubrepositoryproject.data.entity.GithubRepoEntity

@Database(entities = [GithubRepoEntity::class], version = 1)
abstract class SimpleGithubDatabase : RoomDatabase() {

    abstract fun repositoryDao(): RepositoryDao
}