package com.example.rentbikemap

import android.annotation.SuppressLint
import android.graphics.PointF
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.annotation.UiThread
import androidx.appcompat.app.AppCompatActivity
import com.example.rentbikemap.databinding.ActivityMainBinding
import com.example.rentbikemap.dto.Row
import com.example.rentbikemap.retrofit.RetrofitClient
import com.example.rentbikemap.retrofit.RetrofitService
import com.example.rentbikemap.retrofit.RetrofitService.Companion.rentBikeInfoList
import com.example.rentbikemap.retrofit.RetrofitService.Companion.rentBikeInfoTotalList
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.overlay.InfoWindow
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.Overlay
import com.naver.maps.map.overlay.Overlay.OnClickListener
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.util.FusedLocationSource
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlin.system.exitProcess


/* #21# [네이버 지도] MapView의 lifecycle 메서드 호출
*  - onCreate(), onMapReady(), onStart(), onResume(), onPause(), onSaveInstanceState(), onStop(), onDestroy(), onLowMemory() */
class MainActivity : AppCompatActivity(), OnClickListener, OnMapReadyCallback {

    private lateinit var binding: ActivityMainBinding
    private lateinit var naverMap: NaverMap
    private lateinit var mapView: MapView
    private val zoomLevel :Double = 15.0                            // 네이버지도 zoom 레벨
    private lateinit var locationSource: FusedLocationSource        // 네이버지도 현재 위치 사용을 위해 선언


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 위치 사용을 위한 권한 확인
        locationSource = FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)

        /* [종료/뒤로가기 버튼] 앱 종료 */
        binding.mainPreviousBtn.setOnClickListener {
            onStop()
            onDestroy()
            exitProcess(0)
        }

        /* [네이버 지도] */
        mapView = binding.mainMap
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        /* [지도] Retrofit, Rx를 사용하여 Open API 실시간 자전거 대여소 정보(JSON) 가져오기 */
        getRentBikeInfo()
    }

    /* [지도] NaverMap 객체가 준비되면 onMapReady() 메서드 호출 */
    @UiThread
    override fun onMapReady(naverMap: NaverMap) {
        this.naverMap = naverMap

        /* [초기설정] 네이버 지도 현재 위치 추적 버튼 외 UI 비활성화 */
        val uiSettings: UiSettings = naverMap.uiSettings
        uiSettings.isCompassEnabled = false
        uiSettings.isScaleBarEnabled = false
        uiSettings.isZoomControlEnabled = false
        uiSettings.isLocationButtonEnabled = true

        /* [초기설정] 네이버 지도 초기 위치 설정 _서울숲역 위치로 설정 해둠 */
        val initMapCameraPosition = LatLng(37.543617, 127.044707)       // LatLng(위도, 경도)
        val initMoveMapCameraPosition = CameraUpdate.scrollAndZoomTo(LatLng(initMapCameraPosition.latitude, initMapCameraPosition.longitude), zoomLevel)        // scrollTo: 카메라의 대상 지점을 지정한 좌표로 변경, zoomTo: zoom 레벨
        naverMap.moveCamera(initMoveMapCameraPosition)

        /* [초기설정] 네이버 지도 초기 위치 2km 반경 내 자전거 대여소 마커 생성 */
        if (!rentBikeInfoList.isNullOrEmpty()) {
            Log.d("MainActivity", "#21# 지도 초기 위치 2km 반경 내 자전거 대여소 마커 표시")
            createRentBikeMarker(rentBikeInfoList!!, initMapCameraPosition)
        }

        /* 모바일 기기의 현재 위치 추적 (for. 현재 위치 버튼 클릭 시 동작을 위해) */
        naverMap.locationSource = locationSource

        /* [지도 카메라 이동 시] 이동한 지도 중심 위치에 따른 자전거 대여소 마커 생성 */
        naverMap.addOnCameraChangeListener { reason, animated ->
            // 1) 현재 지도의 중심 좌표 가져오기
            var mapCenterLatLng: LatLng = naverMap.cameraPosition.target
            Log.d("MainActivity", "#21# 현재 지도의 중심점 좌표 가져오기 위도[${mapCenterLatLng.latitude}] / 경도[${mapCenterLatLng.longitude}]")

            // 2) 지도 상의 marker 초기화 + 현재 지도 위치 반경 2km 자전거 대여소 marker 생성
            initMarker()
            if (!rentBikeInfoList.isNullOrEmpty()) {
                Log.d("MainActivity", "#21# marker 재생성")
                createRentBikeMarker(rentBikeInfoList!!, mapCenterLatLng)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onStart()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onDestroy() {
        RetrofitService.rentBikeInfoList = mutableListOf()          // Retrofit을 통해 받아온 자전거 대여소 정보 초기화
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }


    /* 현재 위치 권한 요청을 위하여 선언 */
    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }

    /* Marker 정보를 저장할 변수 선언 */
    private var activeMarkers: MutableList<Marker> = mutableListOf()


    /* [@함수] 모바일 기기의 위치 권한 요청 */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (locationSource.onRequestPermissionsResult(requestCode, permissions, grantResults)) {
            if (!locationSource.isActivated) {      // 권한 거부할 경우
                naverMap.locationTrackingMode = LocationTrackingMode.None
            }
            return
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    /* [@함수] Retrofit을 사용하여 공공자전거 대여소 정보 받아오기 */
    @SuppressLint("CheckResult")
    fun getRentBikeInfo() {
        RetrofitService.rentBikeInfoList = mutableListOf()             // 자전거 대여소 정보 초기화

        // Open API 자전거 대여소 정보 호출 시 한 번에 1000건을 초과할 수 없어 3회에 나누어 호출
        for (i in 1..3) {
            when (i) {
                1 -> {
                    // 1번 ~ 1000번 호출
                    RetrofitClient.getInstance().getRentBikeInfo1000()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe { response ->
                            // # 성공 #
                            if (response.isSuccessful) {
                                rentBikeInfoTotalList = response.body()
                                rentBikeInfoList!!.add(rentBikeInfoTotalList!!.rentBikeStatus.row)
                                Log.d("MainActivity", "#21# Retrofit, 1-1000번 자전거 대여소 정보 가져오기 Success- 응답코드[${response.code()}]")
                            }
                            // # 실패 #
                            else {
                                Log.e("MainActivity", "#21# Retrofit 1-1000번 자전거 대여소 정보 가져오기 Fail- 응답코드[${response.code()}]")
                            }
                        }
                }
                2 -> {
                    // 1001번 ~ 2000번 호출
                    RetrofitClient.getInstance().getRentBikeInfo2000()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe { response ->
                            // # 성공 #
                            if (response.isSuccessful) {
                                rentBikeInfoTotalList = response.body()
                                rentBikeInfoList!!.add(rentBikeInfoTotalList!!.rentBikeStatus.row)
                                Log.d("MainActivity", "#21# Retrofit, 1001-2000번 자전거 대여소 정보 가져오기 Success- 응답코드[${response.code()}]")
                            }
                            // # 실패 #
                            else {
                                Log.e("MainActivity", "#21# Retrofit 1001-2000번 자전거 대여소 정보 가져오기 Fail- 응답코드[${response.code()}]")
                            }
                        }
                }
                3 -> {
                    // 2001번 ~ 3000번 호출
                    RetrofitClient.getInstance().getRentBikeInfo3000()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe { response ->
                            // # 성공 #
                            if (response.isSuccessful) {
                                rentBikeInfoTotalList = response.body()
                                rentBikeInfoList!!.add(rentBikeInfoTotalList!!.rentBikeStatus.row)
                                Log.d("MainActivity", "#21# Retrofit, 2001-3000번 자전거 대여소 정보 가져오기 Success- 응답코드[${response.code()}]")
                            }
                            // # 실패 #
                            else {
                                Log.e("MainActivity", "#21# Retrofit 2001-3000번 자전거 대여소 정보 가져오기 Fail- 응답코드[${response.code()}]")
                            }
                        }
                }
            }
        }
        onStart()
    }

    /* [@함수] 생성하려는 Marker의 위치가 반경 2km 이내 인지 확인 > Boolean 반환
     *  - (참고) 위도/경도 길이: Lat(1도) 1Km = 1 / 109.958489129649955, Lng(1도) 1Km = 1 / 88.74 */
    private fun withinRadiusMarker(initCameraPosition: LatLng, markerPositionLat: Double, markerPositionLng: Double): Boolean {
        val latX2 = 2 / 109.958489129649955
        val lngX2 = 2 / 88.74

        val withinLat = Math.abs(initCameraPosition.latitude - markerPositionLat) <= latX2
        val withinLng = Math.abs(initCameraPosition.longitude - markerPositionLng) <= lngX2
        return withinLat && withinLng
    }

    /* [@함수] 네이버 지도 자전거 대여소 위치에 marker 생성 */
    private fun createRentBikeMarker(rentBikeInfoList: MutableList<List<Row>>, currentMapCameraPosition: LatLng) {
        for (rentBike in rentBikeInfoList) {
            for (row in rentBike) {
                // [2km 반경 이내 여부 확인] 2km 반경 밖의 자전거 대여소는 marker를 생성하지 않음 _withinRadiusMarker() 함수 호출
                if (!withinRadiusMarker(currentMapCameraPosition, row.stationLatitude.toDouble(), row.stationLongitude.toDouble()))
                    continue
                // [marker 생성]
                val marker = Marker()
                marker.position = LatLng(row.stationLatitude.toDouble(), row.stationLongitude.toDouble())
                marker.icon = OverlayImage.fromResource(R.drawable.ic_marker_rentbike_green)
                marker.anchor = PointF(0.5f, 0.5f)    // marker와 좌표 간의 이격 조정
                marker.map = naverMap                       // marker를 지도에 표시

                marker.tag = row                            // 생성한 marker에 자전거 대여소 정보 자체를 붙임(tag)
                marker.onClickListener = this
                activeMarkers.add(marker)                   // 생성한 marker 저장 (marker 초기화를 위해 저장)
            }
        }
    }

    /* [지도 marker 클릭 시 호출] */
    override fun onClick(overlay: Overlay): Boolean {
        val marker = overlay as Marker
        val rentBike = marker.tag as Row
        Log.d("MainActivity", "#21# marker 클릭 @선택한 대여소: ${rentBike.stationName}")

        /* 선택한 marker의 정보를 표시하기 위하여 네이버 지도의 infoWindow 사용 */
        val infoWindow = InfoWindow()
        if (infoWindow.marker != null) {
            infoWindow.close()
        }else {
            infoWindow.adapter = object : InfoWindow.DefaultViewAdapter(this) {
                override fun getContentView(infoWindow: InfoWindow): View {
                    val view = View.inflate(this@MainActivity, R.layout.marker_view_info_window, null)
                    (view.findViewById<View>(R.id.info_bikeRental_Name_Txt) as TextView).text = rentBike.stationName
                    (view.findViewById<View>(R.id.info_bikeRental_ableCnt_Txt) as TextView).text = "${rentBike.parkingBikeTotCnt} 대"

                    return view
                }
            }
            infoWindow.open(marker)
        }
        return false
    }

    /* [@함수] 지도 상에 표시 되어 있는 marker 초기화 */
    private fun initMarker() {
        Log.d("MainActivity", "#21# marker 초기화 진행: 생성되어 있는 marker[${activeMarkers.toString()}]")
        for (activeMarker in activeMarkers!!) {
            activeMarker.map = null
        }
        activeMarkers = mutableListOf()
    }

}