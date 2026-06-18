package com.abdulin.nutritionapp.domain.repository

import com.abdulin.nutritionapp.core.utils.Resource
import com.abdulin.nutritionapp.domain.model.HomeModel

interface HomeRepository {
    suspend fun getHomeData(): Resource<HomeModel>
}
