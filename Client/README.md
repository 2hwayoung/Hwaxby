# 프론트엔드 README.md

<img src = "https://user-images.githubusercontent.com/35593748/156006882-74d01f1e-22bf-4176-b246-d380a213b9b7.png" width="30%" height="30%">

# Client
  - style.js: style 관련 code
  - Screen
    - HomePage: Main Page code

   
   
   
# 사용 기술 스택
    - react17.0.1
    - react-native 0.64.2
    - typescript 4.3.4
    - axios 0.21.1
    - [react-native-geolocation-service](https://www.npmjs.com/package/react-native-geolocation-service) 5.3.0-beta.1
    - [react-native-sound](https://www.npmjs.com/package/react-native-sound) 0.11.0
    - [react-native-audio-record](https://www.npmjs.com/package/react-native-sound) 0.2.2
    
# 음성 Issue
- 음성 관련 library가 매우 많지만 그만큼 관리 부실한 library들 다수
- 음성 ⇒ 문자 변환 api가 SampleRate 16000 이어야한다
- 재생, 녹음 둘 다 되는 library에는 해당 조건 만족 없음
- 따라서 둘이 다른 library 사용
   - 음성 변환 (react-native-audio-record)
   - 음성 재생 (react-native-sound)
- library 고를 때 docs, 이용자 수, 관리되는지 여부, github issue 확인 필요
