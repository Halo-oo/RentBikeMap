package com.example.rentbikemap.dto

/* Open API 서울시 공공자전거 실시간 대여정보(JSON)를 받아오기 위한 데이터 클래스 */
data class Repository(
    val rentBikeStatus: RentBikeStatus
)

data class RentBikeStatus(
    val RESULT: RESULT,
    val list_total_count: Int,
    val row: List<Row>
)

data class RESULT(
    val CODE: String,
    val MESSAGE: String
)

data class Row(
    val parkingBikeTotCnt: String,
    val rackTotCnt: String,
    val shared: String,
    val stationId: String,
    val stationLatitude: String,
    val stationLongitude: String,
    val stationName: String
)