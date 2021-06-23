package spring.Hwaxby_back.controller;

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
import spring.Hwaxby_back.service.CoordService;
import spring.Hwaxby_back.service.VoiceService;
import spring.Hwaxby_back.service.WeatherService;

import java.util.Optional;

@Controller
public class ResponseController {

    private final VoiceService voiceService;
    private final WeatherService weatherService;

    @Autowired
    public ResponseController(VoiceService voiceService, WeatherService weatherService) {
        this.voiceService = voiceService;
        this.weatherService = weatherService;
    }

    @PostMapping("response")
    public ResponseEntity<?> getResponse(@RequestBody Ask askData) throws Exception {
        // [TEMP] tester
        System.out.println("here");
//        Coordinates test_cor = new Coordinates();
//        test_cor.setLat(36.504658); test_cor.setLon(129.44539);
//        askData.setCoordinates(test_cor);
        OpenWeatherType type;
//        type = OpenWeatherType.CURRENT;

        /** 0. Response 객체 생성 */
        Response response = new Response();

        /** 1. Ask-Voice-Text Tokenizing */
        Optional<Voice> opvoice = voiceService.findOne(askData.getVoice().getId());
        Voice voice = null;

        if (opvoice.isPresent()) {
            voice = opvoice.get();
            voice = voiceService.voiceParsing(voice);
        } else {
            System.out.println("There's no voice entity match to id: "+ askData.getVoice().getId());
        }


        /** 2. 1번 결과로부터 Keyword 추출 */
        type = voice.getTextParsed().getOpenWeatherType();
        response.setType(type);


        /** 3. 2번 결과로부터 Keyword에 대한 (Current, Forecast) 분기  -> OpenWeather API */
        OpenWeather api_result = new OpenWeather() {};

        if (type.equals(OpenWeatherType.CURRENT)){
            api_result = new CurrentWeather();
        } else if (type.equals(OpenWeatherType.FORECAST)){
            api_result = new ForecastWeather();
        }

        response.setApiData(weatherService.getCurrentByCoor(api_result, type, askData.getCoordinates().getLat(), askData.getCoordinates().getLon()));

        /** 4. Model Input 문장 생성 및 Model 요청 -> Voice(Type:Response) 객체 생성*/

        /** 5. Display 객체 생성 */
        Display display1 = new Display();

        /** 6. 최종적인 Response 객체 setting  */

        /** 7. return */
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

<<<<<<< HEAD
    @PostMapping("test")
    public String test() throws Exception {
=======
    @GetMapping("test")
    public ResponseEntity<?> tester(@RequestBody Ask askData) throws Exception {
>>>>>>> 6d9325df... [update] geocoder func. tester
        System.out.println("testing");
        Coordinates response = weatherService.geocoder("영덕");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
