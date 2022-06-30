package com.example.rentbikemap.retrofit

import com.example.rentbikemap.dto.Repository
import com.example.rentbikemap.dto.Row
import io.reactivex.Single
import retrofit2.Response
import retrofit2.http.GET

interface RentBikeService{

    @GET("556c7a7a4368696831333477716f6550/json/bikeList/1/1000/")
    fun getRentBikeInfo1000(): Single<Response<Repository>>

    @GET("556c7a7a4368696831333477716f6550/json/bikeList/1001/2000/")
    fun getRentBikeInfo2000(): Single<Response<Repository>>

    @GET("556c7a7a4368696831333477716f6550/json/bikeList/2001/3000/")
    fun getRentBikeInfo3000(): Single<Response<Repository>>
}


class RetrofitService {

    companion object {
        var rentBikeInfoTotalList: Repository? = null
        var rentBikeInfoList: MutableList<List<Row>>? = mutableListOf()       // 받아온 JSON 데이터 중 Row만 저장하기 위하여 선언
    }
}