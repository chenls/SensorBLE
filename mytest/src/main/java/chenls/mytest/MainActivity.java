package chenls.mytest;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static List tempList = new ArrayList();
    public static Collection timeList = new ArrayList();
    final int HEIGHT=900;   //设置画图范围高度
    final int WIDTH=800;    //画图范围宽度
    final int Y_start=20;    //画图范围宽度
    final int Y_scale=60;    //画图范围宽度
    SurfaceView surface = null;
    final int X_OFFSET = 60;  //x轴（原点）起始位置偏移画图范围一点
    private int cx = X_OFFSET;  //实时x的坐标
    int centerY = HEIGHT /2;  //y轴的位置
    public static int Data[];
    int d[]=new int[100];
    public String[] YLabel={"19","20","21","22","23","24","25","26","27","28","29","30","31","32"};


    private SurfaceHolder holder = null;    //画图使用，可以控制一个SurfaceView

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SimpleDateFormat formatter = new SimpleDateFormat("mm:ss");
        for (int i = 0; i < 12; i++) {
            tempList.add(i+19);
            String strBeginDate = formatter.format(new Date());
            timeList.add(strBeginDate);
        }
        surface = (SurfaceView)findViewById(R.id.show);
        //初始化SurfaceHolder对象
        holder = surface.getHolder();
        holder.setFixedSize(WIDTH+20, HEIGHT+100);  //设置画布大小，要比实际的绘图位置大一点
        holder.addCallback(new SurfaceHolder.Callback() {
            public void surfaceChanged(SurfaceHolder holder,int format,int width,int height){
                drawBack(holder);
                //如果没有这句话，会使得在开始运行程序，整个屏幕没有白色的画布出现
                //直到按下按键，因为在按键中有对drawBack(SurfaceHolder holder)的调用
            }
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                // TODO Auto-generated method stub
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                // TODO Auto-generated method stub
            }
        });

    }
    private void drawBack(SurfaceHolder holder){
        Canvas canvas = holder.lockCanvas(); //锁定画布
        //绘制白色背景
        canvas.drawColor(Color.WHITE);
        Paint p = new Paint();
        Paint p1 = new Paint();
        Paint p2 = new Paint();
        Paint p3 = new Paint();
        Paint p4 = new Paint();
        p.setColor(Color.MAGENTA);
        p1.setColor(Color.BLACK);
        p2.setColor(Color.BLUE);
        p3.setColor(Color.RED);
        //p.setColor(Color.BLACK);
        p.setTextSize(30);//设置字体大小
        p1.setTextSize(30);//设置字体大小
        p2.setTextSize(30);//设置字体大小
        p3.setTextSize(30);//设置字体大小
        p4.setTextSize(20);//设置字体大小
        p.setStrokeWidth(3);
        p1.setStrokeWidth(3);
        p2.setStrokeWidth(3);
        p3.setStrokeWidth(3);
        p4.setStrokeWidth(2);
        p.setAntiAlias(true);// 设置画笔的锯齿效果。 true是去除，大家一看效果就明白了
        p3.setAntiAlias(true);// 设置画笔的锯齿效果。 true是去除，大家一看效果就明白了
        //绘制坐标轴
        canvas.drawLine(X_OFFSET, HEIGHT, WIDTH, HEIGHT, p1); //绘制X轴 前四个参数是起始坐标
        canvas.drawLine(X_OFFSET, 20, X_OFFSET, HEIGHT, p1); //绘制Y轴 前四个参数是起始坐标

        canvas.drawText("℃", X_OFFSET+20, Y_start+20, p1);
        //绘制Y轴箭头
        Path path = new Path();
        path.moveTo(X_OFFSET, Y_start-10);// 此点为多边形的起点
        path.lineTo(X_OFFSET-8, Y_start+20);
        path.lineTo(X_OFFSET+8, Y_start+20);
        path.close(); // 使这些点构成封闭的多边形
        canvas.drawPath(path, p1);

        for(int i = 0; i <YLabel.length; i++){
            canvas.drawLine(X_OFFSET,(HEIGHT-Y_scale*(i+1)), X_OFFSET+15, (HEIGHT-Y_scale*(i+1)), p1);
            canvas.drawText(YLabel[i], X_OFFSET-50,(HEIGHT-Y_scale*(i+1))+10, p1);
        }


        canvas.drawText("t", WIDTH-10, HEIGHT-20, p1);
        canvas.drawLine(X_OFFSET,YCoord(29), X_OFFSET+Y_scale*12, YCoord(29), p3);
        canvas.drawLine(X_OFFSET,YCoord(25), X_OFFSET+Y_scale*12, YCoord(25), p2);
        //绘制X轴箭头
        Path path1 = new Path();
        path1.moveTo(WIDTH+10, HEIGHT);// 此点为多边形的起点
        path1.lineTo(WIDTH-20, HEIGHT+8);
        path1.lineTo(WIDTH-20, HEIGHT-8);
        path1.close(); // 使这些点构成封闭的多边形
        canvas.drawPath(path1, p1);
        Log.v("BreakPoint","已经完成XY轴绘图");
        //      Log.v("BreakPoint","得到的list数组长度为："+MainActivity.tempList.size());
        //      Log.v("BreakPoint","得到的list的第一个值为："+MainActivity.tempList.get(1));
        Object obj[]=  MainActivity.tempList.toArray();
        Log.v("BreakPoint","获得的数据数组"+obj.length);
        for(int x=0; x<obj.length; x++){


            d[x]=Integer.parseInt(String.valueOf(obj[x])); ;

            Log.v("BreakPoint","获得的数据数组"+d[x]);
        }

        if(MainActivity.timeList.size()>=13){
            for(int i = MainActivity.timeList.size()-13, m = 0; i <MainActivity.timeList.size()&&(m<13); i++,m++){


                canvas.drawLine(X_OFFSET+Y_scale*m,HEIGHT, X_OFFSET+Y_scale*m, HEIGHT-15, p);
                canvas.drawText((String) ((List) MainActivity.timeList).get(i),X_OFFSET+Y_scale*m-20,HEIGHT+30, p4);

                if (m>0) {
                    canvas.drawLine(X_OFFSET+(m-1)*Y_scale, YCoord(d[i-1]), X_OFFSET+m*Y_scale, YCoord(d[i]), p1);
                }
                if(d[i]>28){
                    canvas.drawCircle(X_OFFSET+m*Y_scale,  YCoord(d[i]),8, p3);
                    canvas.drawText(String.valueOf(d[i]),X_OFFSET+m*Y_scale-15,YCoord(d[i])-15, p3);
                    for(int n = 0 ;n<(HEIGHT-YCoord(d[i]))/40 ;n++ ){
                        canvas.drawLine(X_OFFSET+m*Y_scale,HEIGHT-40*n,X_OFFSET+m*Y_scale,HEIGHT-40*n-30, p3);
                    }
                }
                if(d[i]<26){
                    canvas.drawCircle(X_OFFSET+m*Y_scale,  YCoord(d[i]),8, p2);
                    canvas.drawText(String.valueOf(d[i]),X_OFFSET+m*Y_scale-15,YCoord(d[i])-15, p2);
                    for(int n = 0 ;n<(HEIGHT-YCoord(d[i]))/40 ;n++ ){
                        canvas.drawLine(X_OFFSET+m*Y_scale,HEIGHT-40*n,X_OFFSET+m*Y_scale,HEIGHT-40*n-30, p2);
                    }
                }
                if((d[i]<=28)&&(d[i]>=26)){
                    canvas.drawCircle(X_OFFSET+m*Y_scale,  YCoord(d[i]),8, p);
                    canvas.drawText(String.valueOf(d[i]),X_OFFSET+m*Y_scale-15,YCoord(d[i])-15, p);
                    for(int n = 0 ;n<(HEIGHT-YCoord(d[i]))/40  ;n++ ){
                        canvas.drawLine(X_OFFSET+m*Y_scale,HEIGHT-40*n,X_OFFSET+m*Y_scale,HEIGHT-40*n-30, p);
                    }
                }
            }
        }
        else
        {
            for(int i = 0; i <MainActivity.timeList.size(); i++){
                canvas.drawLine(X_OFFSET+Y_scale*2*i,YCoord(29), X_OFFSET+Y_scale*(2*i+1)+40, YCoord(29), p3);
                canvas.drawLine(X_OFFSET+Y_scale*2*i,YCoord(25), X_OFFSET+Y_scale*(2*i+1)+40, YCoord(25), p2);
                canvas.drawLine(X_OFFSET+Y_scale*i,HEIGHT, X_OFFSET+Y_scale*i, HEIGHT-15, p);
                canvas.drawText((String) ((List) MainActivity.timeList).get(i),X_OFFSET+Y_scale*i-20,HEIGHT+30, p4);
                if (i>0) {
                    canvas.drawLine(X_OFFSET+(i-1)*Y_scale, YCoord(d[i-1]), X_OFFSET+i*Y_scale, YCoord(d[i]), p1);
                }
                if(d[i]>28){
                    canvas.drawCircle(X_OFFSET+i*Y_scale,  YCoord(d[i]),8, p3);
                    canvas.drawText(String.valueOf(d[i]),X_OFFSET+i*Y_scale-15,YCoord(d[i])-15, p3);
                    for(int n = 0 ;n<(HEIGHT-YCoord(d[i]))/40  ;n++ ){
                        canvas.drawLine(X_OFFSET+i*Y_scale,HEIGHT-40*n,X_OFFSET+i*Y_scale,HEIGHT-40*n-30, p3);
                    }
                }
                if(d[i]<26){
                    canvas.drawCircle(X_OFFSET+i*Y_scale,  YCoord(d[i]),8, p2);
                    canvas.drawText(String.valueOf(d[i]),X_OFFSET+i*Y_scale-15,YCoord(d[i])-15, p2);
                    for(int n = 0 ;n<(HEIGHT-YCoord(d[i]))/40  ;n++ ){
                        canvas.drawLine(X_OFFSET+i*Y_scale,HEIGHT-40*n,X_OFFSET+i*Y_scale,HEIGHT-40*n-30, p2);
                    }
                }
                if((d[i]<=28)&&(d[i]>=26)){
                    canvas.drawCircle(X_OFFSET+i*Y_scale,  YCoord(d[i]),8, p);
                    canvas.drawText(String.valueOf(d[i]),X_OFFSET+i*Y_scale-15,YCoord(d[i])-15, p);
                    for(int n = 0 ;n<(HEIGHT-YCoord(d[i]))/40 ;n++ ){
                        canvas.drawLine(X_OFFSET+i*Y_scale,HEIGHT-40*n,X_OFFSET+i*Y_scale,HEIGHT-40*n-30, p);
                    }
                }
            }

        }
        holder.unlockCanvasAndPost(canvas);  //结束锁定 显示在屏幕上
        holder.lockCanvas(new Rect(0,0,0,0)); //锁定局部区域，其余地方不做改变
        holder.unlockCanvasAndPost(canvas);

    }
    private int YCoord(int m) {
        // TODO Auto-generated method stub
        Log.v("BreakPoint","这是八个断点");
        int y=m-18;

        return HEIGHT-Y_scale*y;
    }
}
