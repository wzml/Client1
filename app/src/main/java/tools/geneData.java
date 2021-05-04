package tools;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class geneData {
    static public int SensorNum = 5;
    static public int WorkerNum = 1;

    static List<Worker>  workers = new ArrayList<>(WorkerNum);
    public static List<Worker> getBeginW(){
        for(int i = 0; i < WorkerNum; i++){
            workers.add(new Worker());
            workers.get(i).setId(i+1);
        }
        return workers;
    }
    // 更新位置的工人信息
    public static List<Worker> getProcessW(List<Worker> workeri){
        Random r = new Random();
        int i = 0,loxi=0,loyi=0,oldx,oldy;
        for (;i < workeri.size();i++){
            loxi = r.nextInt(3)  - 1;
            loyi = r.nextInt(3)  - 1;
            oldx = workeri.get(i).locx;
            oldy = workeri.get(i).locy;
            int flag = r.nextInt(2);
            if (flag == 0){  // 前后走动
                while (loxi + oldx < 0 || loxi + oldx > 100)  // 保证不会走出地图
                    loxi = r.nextInt(3)  - 1;
                workeri.get(i).setLocx(loxi + oldx);
            }else { // 上下走动
                while (loyi + oldy < 0 || loyi + oldy > 100)
                    loyi = r.nextInt(3) - 1;
                workeri.get(i).setLocy(loyi + oldy);
            }
        }
        return workeri;
    }

    static List<Sensor> sensors = new ArrayList(SensorNum);
    public static List<Sensor> getBeginS() {
        for(int i = 0;i < SensorNum;i++){     // 初始化列表
            sensors.add(new Sensor());
            sensors.get(i).setId(i+1);
        }
        //  设置位置信息
        sensors.get(0).setLocx(0);
        sensors.get(0).setLocy(0);
        sensors.get(1).setLocx(0);
        sensors.get(1).setLocy(100);
        sensors.get(2).setLocx(100);
        sensors.get(2).setLocy(100);
        sensors.get(3).setLocx(100);
        sensors.get(3).setLocy(0);
        sensors.get(4).setLocx(50);
        sensors.get(4).setLocy(50);
        return sensors;
    }
    // 更新传感器值的所有传感器信息
    public static List<Sensor> getProcessS(List<Sensor> sensorsi){
        Random r = new Random();
        int i = 0;
        for(; i < sensorsi.size(); i++){
            if(i!=2){
                sensorsi.get(i).setTotalDust(r.nextDouble() * 4 + 0.01);
                sensorsi.get(i).setBreatheDust(r.nextDouble() * 2.5 + 0.01);
            }else {
                sensorsi.get(i).setTotalDust(r.nextDouble() * 2 + 0.01);
                sensorsi.get(i).setBreatheDust(r.nextDouble() * 1 + 0.01);
            }
            sensorsi.get(i).setTemp(r.nextDouble() * 20 + 20);
            sensorsi.get(i).setHumidity(r.nextDouble() * 20 + 50);
        }
        return sensorsi;
    }

    public static Double getTime(int id){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());					//放入Date类型数据
        double dtime = id * 10000;
        int y = calendar.get(Calendar.YEAR);					//获取年份
        dtime += y;
        y = calendar.get(Calendar.MONTH);					//获取月份
        dtime = dtime * 100 + y;
        y = calendar.get(Calendar.DATE);					//获取日
        dtime = dtime * 100 + y;
        y = calendar.get(Calendar.HOUR_OF_DAY);				//时（24小时制）
        dtime = dtime + y * 0.01;
        y = calendar.get(Calendar.MINUTE);					//分
        dtime = dtime + y * 0.0001;
        y = calendar.get(Calendar.SECOND);					//秒
        dtime = dtime + y * 0.000001;
        return dtime;
    }
}