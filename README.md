# 서울시 공공자전거(따릉이) 실시간 대여소 지도 App

<img src="https://user-images.githubusercontent.com/87004079/176683808-cc72a269-244d-487e-90d0-c9a38cb9344a.png" width="400" height="850">

## 🛠 사용기술
* 서울시 공공자전거 실시간 대여정보 Open API (http://data.seoul.go.kr/dataList/OA-15493/A/1/datasetView.do)
* 네이버 지도 안드로이드(naverMap)
* Retrofit2
* RxAndroid, RxKotlin (비동기 통신)
* OkHttp3
* Gson
* ViewBinding, DataBinding

## 💡 구현기능
* Marker 클릭 시 대여소 정보 표시
  *  InfoWindow 사용
  *  대여소 이름, 현재 대여 가능한 자전거 대수 정보 표시
  
* 현재 지도의 중심 위치로부터 2km 이내의 자전거 대여소 Marker 표시
  *  지도 제공자 라이센스, 현재 위치 버튼 외 네이버 지도 UI 비활성화

* 지도 왼쪽 하단의 현재 위치 버튼 클릭 시 현재 위치의 지도 출력
  *  FusedLocationSource 사용
  
## 📸 스크린샷
* Marker 클릭 시 대여소 정보 표시
  * <img src="https://user-images.githubusercontent.com/87004079/176686780-4340eda1-c5b2-442a-8244-d6d6d90595f8.png" width="400" height="850">
  
* 현재 지도의 중심 위치로부터 2km 이내의 자전거 대여소 Marker 표시
  * <img src="https://user-images.githubusercontent.com/87004079/176686969-b9fa6bdf-5a53-4ba7-9efb-f2c0cdca3678.png" width="400" height="850">

* 지도 왼쪽 하단의 현재 위치 버튼 클릭 시 현재 위치의 지도 출력
  * <img src="https://user-images.githubusercontent.com/87004079/176687156-ad18956f-c95e-431a-9d36-210787b3016b.png" width="1200" height="500">
  
  
