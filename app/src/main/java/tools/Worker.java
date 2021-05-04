package tools;

import java.util.Random;

public class Worker {
    public int id;
    public int locx;
    public int locy;
    public double CurrentDust;
    //  TODO: VideoDest(视频地址-string)
    public Worker(){
        Random r = new Random();
        locx = 0; // 生成[0,100]区间的小数
//        locx = r.nextInt(101) ; // 生成[0,100]区间的小数
//        locy = 0;
        locy = r.nextInt(101) ;
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
}
