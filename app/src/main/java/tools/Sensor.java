package tools;

import java.util.Random;

public class Sensor {
    public int id;
    public int locx;
    public int locy;
    public double TotalDust;
    public double BreatheDust;
    public double Temp;
    public double Humidity;

    //  TODO: VideoDest(视频地址-string)
    public Sensor(){
        Random r = new Random();
        id = -1;
        locx = 0;
        locy = 0;
        TotalDust = r.nextDouble() * 4 + 0.01;     // 生成[0, 4.01)区间的小数，正常的情况是不正常情况的400倍
        BreatheDust = r.nextDouble() * 2.5 + 0.01; // 生成[0, 2.51)区间的小数，正常的情况是不正常情况的250倍
        Temp = r.nextDouble() * 20 + 20;           // 生成[20,40)区间的小数
        Humidity = r.nextDouble() * 20 + 50;       // 生成[50,70)区间的小数
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    public double getLocx() {
        return locx;
    }

    public void setLocx(int locx) {
        this.locx = locx;
    }

    public double getLocy() {
        return locy;
    }

    public void setLocy(int locy) {
        this.locy = locy;
    }

    public double getTotalDust() {
        return TotalDust;
    }

    public void setTotalDust(double totalDust) {
        TotalDust = totalDust;
    }

    public double getBreatheDust() {
        return BreatheDust;
    }

    public void setBreatheDust(double breatheDust) {
        BreatheDust = breatheDust;
    }

    public double getTemp() {
        return Temp;
    }

    public void setTemp(double temp) {
        Temp = temp;
    }

    public double getHumidity() {
        return Humidity;
    }

    public void setHumidity(double humidity) {
        Humidity = humidity;
    }
}
