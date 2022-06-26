package com.breens.githubapp.data.repositoryimplementation

import com.breens.githubapp.data.local.dao.UserDao
import com.breens.githubapp.data.local.mappers.toDomain
import com.breens.githubapp.data.local.mappers.toEntity
import com.breens.githubapp.data.network.GithubApi
import com.breens.githubapp.domain.models.User
import com.breens.githubapp.domain.repository.GetUserProfileRepository
import com.breens.githubapp.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException

class GetUserInfoRepositoryImplementation(
    private val githubApi: GithubApi,
    private val userDao: UserDao
) : GetUserProfileRepository {

    override fun getUserProfile(name: String?): Flow<Resource<User>> = flow {
        emit(Resource.Loading())

        val getUserFromCache = userDao.getUser(name).toDomain()
        Resource.Loading(data = getUserFromCache)

        try {
            val networkResponse = githubApi.getUserProfile(name ?: "")
            userDao.deleteUser()
            userDao.insertUser(networkResponse.toEntity())
        } catch (exception: HttpException) {
            emit(
                Resource.Error(
                    message = exception.message(),
                    data = getUserFromCache
                )
            )
        } catch (exception: IOException) {
            emit(
                Resource.Error(
                    message = exception.message.toString(),
                    data = getUserFromCache
                )
            )
        }

        val user = userDao.getUser(name).toDomain()
        emit(Resource.Success(user))
    }
}