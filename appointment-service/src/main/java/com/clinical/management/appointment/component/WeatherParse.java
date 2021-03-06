package com.clinical.management.appointment.component;

import com.clinical.management.appointment.domain.Weather;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class WeatherParse {

    public List<Weather> parse(JSONObject json) throws ParseException {
        List<Weather> allWeathers = getAllWeathers(json);
        return reduceWeathers(allWeathers);
    }

    private List<Weather> getAllWeathers(JSONObject json) throws ParseException {
        List<Weather> weathers = new ArrayList<>();
        HashMap object = (HashMap) json.get("city");
        ArrayList list = (ArrayList) json.get("list");
        String cityName = (String) object.get("name");
        String country = (String) object.get("country");

        for (Object obj : list) {
            Weather weather = getWeather((HashMap) obj);
            weather.setCity(cityName);
            weather.setCountry(country);

            weathers.add(weather);
        }

        return weathers;
    }

    private Weather getWeather(HashMap obj) throws ParseException {
        Weather weather = new Weather();
        Date date = convertStringToDate((String) obj.get("dt_txt"));
        HashMap main = (HashMap) obj.get("main");
        HashMap weatherInfo = (HashMap) ((ArrayList) obj.get("weather")).get(0);

        weather.setDate(date);
        weather.setTempMax(convertObjectToDouble(main.get("temp_max")));
        weather.setTempMin(convertObjectToDouble(main.get("temp_min")));
        weather.setWeatherDescription((String) weatherInfo.get("description"));

        return weather;
    }

    private List<Weather> reduceWeathers(List<Weather> weathers) {
        List<Weather> reduceWeathers = new ArrayList<>();

        if (weathers.isEmpty()) {
            return reduceWeathers;
        }

        Weather currentWeather = weathers.get(0);

        for (Weather weather : weathers) {
            if (sameDay(currentWeather, weather)) {
                reduceWeather(currentWeather, weather);
            } else {
                reduceWeathers.add(currentWeather);
                currentWeather = weather;
            }
        }

        return reduceWeathers;
    }

    private void reduceWeather(Weather currentWeather, Weather nextWeather) {
        if (currentWeather.getTempMin() > nextWeather.getTempMin()) {
            currentWeather.setTempMin(nextWeather.getTempMin());
        }

        if (currentWeather.getTempMax() < nextWeather.getTempMax()) {
            currentWeather.setTempMax(nextWeather.getTempMax());
        }
    }

    private boolean sameDay(Weather currentWeather, Weather nextWeather) {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(currentWeather.getDate());
        cal2.setTime(nextWeather.getDate());

        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) && cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    private Double convertObjectToDouble(Object tmp) {
        if (tmp instanceof Long) {
            return ((Long) tmp).doubleValue();
        }

        if (tmp instanceof Integer) {
            return ((Integer) tmp).doubleValue();
        }

        return (Double) tmp;
    }

    private Date convertStringToDate(String date) throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return formatter.parse(date);
    }

}
