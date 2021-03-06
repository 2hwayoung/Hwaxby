package spring.Hwaxby_back.controller;

import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import spring.Hwaxby_back.domain.*;
import spring.Hwaxby_back.domain.OpenWeather.CurrentWeather;
import spring.Hwaxby_back.domain.OpenWeather.ForecastWeather;
import spring.Hwaxby_back.domain.OpenWeather.OpenWeather;
import spring.Hwaxby_back.domain.VoiceItem.TextParsed;
import spring.Hwaxby_back.domain.VoiceItem.VoiceType;
import spring.Hwaxby_back.service.*;

import java.util.ArrayList;
import java.util.Optional;

@Controller
public class ResponseController {

    private final VoiceService voiceService;
    private final WeatherService weatherService;
    private final CoordService coordService;
    private final ScriptService scriptService;
    private final ResponseService responseService;

    @Autowired
    public ResponseController(VoiceService voiceService, WeatherService weatherService, CoordService coordService, ScriptService scriptService, ResponseService responseService) {
        this.voiceService = voiceService;
        this.weatherService = weatherService;
        this.coordService = coordService;
        this.scriptService = scriptService;
        this.responseService = responseService;
    }

    @PostMapping("response")
    public ResponseEntity<?> getResponse(@RequestBody Ask askData) throws Exception {

//        System.out.println("/response API called");
        // [TEMP] tester
//        Coordinates test_cor = new Coordinates();
//        test_cor.setLat(36.504658); test_cor.setLon(129.44539);
//        askData.setCoordinates(test_cor);
//
//        Voice test_voice = new Voice();
//        test_voice.setId(0L);
//        test_voice.setText("화스비 서울에 비나 눈 와?");
//        askData.setVoice(test_voice);


        Optional<Coordinates> opcoor = coordService.findOne(askData.getCoordinates().getId()); // get Coordinates
        Coordinates coordinates = null;

        if (opcoor.isPresent()) {
            coordinates = opcoor.get();
        } else {
            System.out.println("There's no Coordinates entity match to id: "+ askData.getCoordinates().getId());
        }

        /** 0. Response 객체 생성 */
        Response response = new Response();

        /** 1. Ask-Voice-Text Tokenizing */
        Optional<Voice> opvoice = voiceService.findOne(askData.getVoice().getId()); // get Voice
        Voice voice = null;
        boolean nothing = false;

        if (opvoice.isPresent()) {
            voice = opvoice.get();
            voice = voiceService.voiceParsing(voice);
            if (voice.getText().equals("ASR_NOTOKEN")){
                nothing = true;
            }
        } else {
            System.out.println("There's no voice entity match to id: "+ askData.getVoice().getId());
        }



        /** 2. 1번 결과로부터 Keyword 추출 */
        // OpenWeatherType (CURRENT, FORECAST) 추출
        OpenWeatherType type;
        type = voice.getTextParsed().getOpenWeatherType();
        response.setType(type);
        response.setDay(voice.getTextParsed().getDay());

        // city(지역)이 주어진 경우 -> geocoder 함수 호출 -> Coordinates 추출
        if (!voice.getTextParsed().getCity().equals("HERE")){ // city가 HERE이 아니다 = 별도의 city 요청
            Coordinates city_coor = weatherService.geocoder(voice.getTextParsed().getCity());
            coordinates.setLat(city_coor.getLat());
            coordinates.setLon(city_coor.getLon());
        }


        /** 3. 2번 결과로부터 Keyword에 대한 (Current, Forecast) 분기  -> OpenWeather API */
        OpenWeather api_result = new OpenWeather() {};

        if (type.equals(OpenWeatherType.CURRENT)){
            api_result = new CurrentWeather();
        } else if (type.equals(OpenWeatherType.FORECAST)){
            api_result = new ForecastWeather();
        }

//        response.setApiData(weatherService.getCurrentByCoor(api_result, type, coordinates.getLat(), coordinates.getLon()));
        response.setCurrentApiData(weatherService.getCurrentByCoor(api_result, type, coordinates.getLat(), coordinates.getLon()).get(0));
        response.setForecastApiData(weatherService.getCurrentByCoor(api_result, type, coordinates.getLat(), coordinates.getLon()).get(1));

        /** 5. Display 객체 생성 및 setting */
        Display display = new Display();
//        JSONObject data = new JSONObject();
        Display.DisplayINFO data = new Display.DisplayINFO();

        ArrayList<String> needed_Info = voice.getTextParsed().getInfo();
        display.setInfo(needed_Info);


//        System.out.println(needed_Info.size());

        if (type.equals(OpenWeatherType.CURRENT)){
            CurrentWeather currentApiData = (CurrentWeather) response.getCurrentApiData();

            for (int i = 0 ; i < needed_Info.size() ; i++){
                if (needed_Info.get(i).equals("습도")){
                    data.setHumidity(currentApiData.getCurrent().getHumidity());
//                    data.put(needed_Info.get(i), currentApiData.getCurrent().getHumidity());
                } else if (needed_Info.get(i).equals("바람")){  // speed, gust, deg
//                    System.out.println("바람");
                    data.setWind(new Display.DisplayINFO.Wind(
                            currentApiData.getCurrent().getWind_speed(),
                            currentApiData.getCurrent().getWind_gust(),
                            currentApiData.getCurrent().getWind_deg()
                    ));
                } else if (needed_Info.get(i).equals("온도")){
//                    System.out.println("온도");
                    data.setTemp(new Display.DisplayINFO.Temp(
                            currentApiData.getCurrent().getTemp(),
                            currentApiData.getCurrent().getFeels_like()
                    ));
                } else if (needed_Info.get(i).equals("구름")){
//                    System.out.println("구름");
                    data.setClouds(currentApiData.getCurrent().getClouds());
                } else if (needed_Info.get(i).equals("자외선")){
//                    System.out.println("자외선");
                    data.setUvi(currentApiData.getCurrent().getUvi());
                } else if (needed_Info.get(i).equals("비")){
//                    System.out.println("비");
                    data.setRain(new Display.DisplayINFO.Rain(
                            currentApiData.getCurrent().getRain().getRain1h()
                    ));
                } else if (needed_Info.get(i).equals("눈")){
//                    System.out.println("눈");
                    data.setSnow(new Display.DisplayINFO.Snow(
                            currentApiData.getCurrent().getSnow().getSnow1h()
                    ));
                }
            }
            display.setDisplayData(data);

        } else if (type.equals(OpenWeatherType.FORECAST)){
            ForecastWeather forecastApiData = (ForecastWeather) response.getForecastApiData();
            response.setDay(voice.getTextParsed().getDay());
//            System.out.println(voice.getTextParsed().getDay());

            for (int i = 0 ; i < needed_Info.size() ; i++){
                if (needed_Info.get(i).equals("습도")){
//                    System.out.println("습도");
                    data.setHumidity(forecastApiData.getDaily().get(voice.getTextParsed().getDay()).getHumidity());
                } else if (needed_Info.get(i).equals("바람")){  // speed, gust, deg
//                    System.out.println("바람");
                    data.setWind(new Display.DisplayINFO.Wind(
                            forecastApiData.getDaily().get(voice.getTextParsed().getDay()).getWind_speed(),
                            forecastApiData.getDaily().get(voice.getTextParsed().getDay()).getWind_gust(),
                            forecastApiData.getDaily().get(voice.getTextParsed().getDay()).getWind_deg()
                    ));
                } else if (needed_Info.get(i).equals("온도")){
//                    System.out.println("온도");
                    data.setTemp(new Display.DisplayINFO.Temp(
                            // 온도 관련 (Min, Max, Morn, Day, Eve, Night)
                            forecastApiData.getDaily().get(voice.getTextParsed().getDay()).getTemp().getMin(),
                            forecastApiData.getDaily().get(voice.getTextParsed().getDay()).getTemp().getMax(),
                            forecastApiData.getDaily().get(voice.getTextParsed().getDay()).getTemp().getMorn(),
                            forecastApiData.getDaily().get(voice.getTextParsed().getDay()).getTemp().getDay(),
                            forecastApiData.getDaily().get(voice.getTextParsed().getDay()).getTemp().getEve(),
                            forecastApiData.getDaily().get(voice.getTextParsed().getDay()).getTemp().getNight(),

                            // 체감 온도 관련 (Morn, Day, Eve, Night)
                            forecastApiData.getDaily().get(voice.getTextParsed().getDay()).getFeels_like().getMorn(),
                            forecastApiData.getDaily().get(voice.getTextParsed().getDay()).getFeels_like().getDay(),
                            forecastApiData.getDaily().get(voice.getTextParsed().getDay()).getFeels_like().getEve(),
                            forecastApiData.getDaily().get(voice.getTextParsed().getDay()).getFeels_like().getNight()
                    ));
                } else if (needed_Info.get(i).equals("구름")){
//                    System.out.println("구름");
                    data.setClouds(forecastApiData.getDaily().get(voice.getTextParsed().getDay()).getClouds());
                } else if (needed_Info.get(i).equals("자외선")){
//                    System.out.println("자외선");
                    data.setUvi(forecastApiData.getDaily().get(voice.getTextParsed().getDay()).getUvi());
                } else if (needed_Info.get(i).equals("비")){
//                    System.out.println("비");
//                    System.out.println(forecastApiData.getDaily().get(voice.getTextParsed().getDay()).getRain());
                    data.setRain(new Display.DisplayINFO.Rain(
                            forecastApiData.getDaily().get(voice.getTextParsed().getDay()).getRain()
                    ));
                } else if (needed_Info.get(i).equals("눈")){
//                    System.out.println("눈");
//                    System.out.println(forecastApiData.getDaily().get(voice.getTextParsed().getDay()).getSnow());
                    data.setSnow(new Display.DisplayINFO.Snow(
                            forecastApiData.getDaily().get(voice.getTextParsed().getDay()).getSnow()
                    ));
                }
            }
            display.setDisplayData(data);
        }
        response.setDisplay(display);

        /** 4. Model Input 문장 생성 및 Model 요청 -> Voice(Type:Response) 객체 생성*/
        Voice resvoice = new Voice();
        Script script = new Script();
        if (nothing){
            script.setScript("다시 말해주세요.");
            nothing = false;
        } else if (response.getType().equals(OpenWeatherType.CURRENT) && response.getDisplay().getInfo().size() == 0){ // current, 요청x
            scriptService.current_begin(((CurrentWeather)response.getCurrentApiData()).getCurrent().getWeather().get(0).getId(), script);
            scriptService.current_temp(script, (CurrentWeather) response.getCurrentApiData(), (ForecastWeather) response.getForecastApiData());
        } else if (response.getType().equals(OpenWeatherType.FORECAST) && response.getDisplay().getInfo().size() == 0){ // forecast, 요청x
            scriptService.forecast_begin(((CurrentWeather)response.getCurrentApiData()).getCurrent().getWeather().get(0).getId(), script, response.getDay());
            scriptService.forecast_temp(script, (CurrentWeather) response.getCurrentApiData(), (ForecastWeather) response.getForecastApiData(), response.getDay());
        } else if (response.getType().equals(OpenWeatherType.CURRENT)) { // current, 요청 o
            scriptService.specific_current(response.getDisplay().getInfo(), script, (CurrentWeather) response.getCurrentApiData(), (ForecastWeather) response.getForecastApiData());
        } else if (response.getType().equals(OpenWeatherType.FORECAST)){ // forecast, 요청 o
            scriptService.specific_forecast(response.getDisplay().getInfo(), script, (CurrentWeather) response.getCurrentApiData(), (ForecastWeather) response.getForecastApiData(), response.getDay());
        } else {
            script.setScript("다시 말해주세요.");
//            System.out.println("아무거나");
        }
//        System.out.println(((CurrentWeather) response.getCurrentApiData()).getCurrent().getWeather().get(0).getId());
        ((CurrentWeather) response.getCurrentApiData()).getCurrent().getWeather().get(0).setDescription(script.getWeather_map().get(Integer.toString(((CurrentWeather) response.getCurrentApiData()).getCurrent().getWeather().get(0).getId())));
        for (int i=0; i<8; i++) {
//            System.out.println(((ForecastWeather) response.getForecastApiData()).getDaily().get(i).getWeather().get(0).getId());
            ((ForecastWeather) response.getForecastApiData()).getDaily().get(i).getWeather().get(0).setDescription(script.getWeather_map().get(Integer.toString(((ForecastWeather) response.getForecastApiData()).getDaily().get(i).getWeather().get(0).getId())));
        }
        resvoice.setType(VoiceType.RESPONSE);
        resvoice.setText(script.getScript());
//        String command = String.format("python3 /mnt/c/Users/USER/git/Hwaxby/Model/infer-v2.py \"%s\" > /dev/null 2>&1 && cat output", resvoice.getText());
//        resvoice.setData(voiceService.textToVoice(resvoice, "python3 /mnt/c/Users/USER/git/Hwaxby/Model/infer-v2.py \""+resvoice.getText()+"\" > /dev/null 2>&1 && cat /mnt/c/Users/USER/git/Hwaxby/Model/output\""));
        resvoice.setData(voiceService.textToVoice(resvoice, resvoice.getText()));
        String result = voiceService.textToVoice(resvoice, resvoice.getText());
//        String news = new String();
//        int c = 0;
//        for (int i=0; i < result.length(); i++) {
//            if (48 <= result.charAt(i) && result.charAt(i) <= 57) {
//                c += 1;
//                news += result.charAt(i);
//            }else{
//                news = Numbers(news);
//                result = result.substring(0, i-c+1) + news+result.substring(i+1, result.length());
//                c = 0;
//                news = "";
//            }
//        }
        resvoice.setData(result);
//        System.out.println("--------------------------------------");
//        System.out.println(resvoice.getText());
//        System.out.println(resvoice.getData());
//        resvoice.setData(resvoice.getData().substring(0, 300));
        response.setVoice(resvoice);


        Gson gson = new Gson();
        String json = gson.toJson(response);
//        System.out.println(json);
        /** 6. return */

        responseService.save(response);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("test")
    public ResponseEntity<?> tester(@RequestBody TextParsed askData) throws Exception {
        System.out.println("testing");
        Coordinates response = weatherService.geocoder(askData.getCity());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("script")
    public ResponseEntity<?> tester(@RequestBody Response dd) throws Exception {
        System.out.println("script 작성 중");
        Script script = new Script();

        Response data = responseService.findOne(dd.getId()).get();
        CurrentWeather currentApiData = (CurrentWeather) data.getCurrentApiData();
        ForecastWeather forecastApiData = (ForecastWeather) data.getForecastApiData();
//        System.out.println(data.getDay());
//        System.out.println(data.getType());
//        System.out.println(data.getDisplay().getInfo().size());
//        System.out.println("script 시작하기");

        if (data.getType().equals(OpenWeatherType.CURRENT) && data.getDisplay().getInfo().size() == 0){ // current, 요청x
            scriptService.current_begin(currentApiData.getCurrent().getWeather().get(0).getId(), script);
            scriptService.current_temp(script, currentApiData, forecastApiData);
        } else if (data.getType().equals(OpenWeatherType.FORECAST) && data.getDisplay().getInfo().size() == 0){ // forecast, 요청x
            scriptService.forecast_begin(currentApiData.getCurrent().getWeather().get(0).getId(), script, data.getDay());
            scriptService.forecast_temp(script, currentApiData, forecastApiData, data.getDay());
        } else if (data.getType().equals(OpenWeatherType.CURRENT)) { // current, 요청 o
            scriptService.specific_current(data.getDisplay().getInfo(), script, currentApiData, forecastApiData);
        } else if (data.getType().equals(OpenWeatherType.FORECAST)){ // forecast, 요청 o
            scriptService.specific_forecast(data.getDisplay().getInfo(), script, currentApiData, forecastApiData, data.getDay());
        } else {
            script.setScript("다시 말해주세요.");
        }


        return new ResponseEntity<>(script.getScript(), HttpStatus.OK);
    }

    public String Numbers(String number) {
        String output = number;
        // 스캐너로 정수형태 받기

        String[] unit = { "", "십 ", "백 ", "천 ", "만 ", "십 ", "백 ", "천 " };
        String result = "";
        int out = output.length() - 1;

        for (int i = 0; i < output.length(); i++) {
            //// 문자열의 길이 만큼 반복문 실행
            int n1 = output.charAt(i) - '0';
            // 2
            String n = new String(String.valueOf(n1));

            if (read(n) != null) {
                // 숫자가 0일 경우는 출력하지 않음
                result += read(n)+unit[out];
                // 3
            }
            out--;
        }
        return result;
    }

    public String read(String num) {
        switch (num) {
            case "1":
                return "일";
            case "2":
                return "이";
            case "3":
                return "삼";
            case "4":
                return "사";
            case "5":
                return "오";
            case "6":
                return "육";
            case "7":
                return "칠";
            case "8":
                return "팔";
            case "9":
                return "구";
        }
        return null;
    }

}
