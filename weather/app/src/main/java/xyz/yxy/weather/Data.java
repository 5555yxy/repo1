package xyz.yxy.weather;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Data {
    public String shidu;//湿度
    public int pm25;//pm2.5
    public int pm10;//pm10
    public String quality;//空气质量
    public String wendu;//温度
    public String ganmao;//感冒提醒
    @SerializedName("forecast")
    public List<Forecast> forecastList;//今天+未来4天
    public Yesterday yesterday;
}
