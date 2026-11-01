package com.ezatullah.runneradventure;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.*;
import android.view.*;
import android.content.*;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.GradientDrawable;
import java.util.*;

public class MainActivity extends Activity {
    GameView game;
    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        game = new GameView(this);
        setContentView(game);
    }
    @Override protected void onPause() { super.onPause(); if (game != null) game.pauseGame(); }
    @Override protected void onResume() { super.onResume(); if (game != null) game.resumeGame(); }

    static class GameView extends View {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        Canvas c;
        int screen = 0; // 0 menu, 1 game, 2 game over, 3 about
        float w,h, ground, playerY, vy, speed;
        float playerX, playerSize;
        boolean jumping = false, paused = false;
        long last = 0;
        int score = 0, level = 1, coins = 0, best;
        float obstacleX, obstacleW, obstacleH;
        float coinX, coinY;
        Random random = new Random();
        SharedPreferences prefs;
        String playerName = "Ezatullah";
        GameView(Context ctx) {
            super(ctx);
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            prefs = ctx.getSharedPreferences("runner_data", Context.MODE_PRIVATE);
            best = prefs.getInt("best", 0);
            setFocusable(true);
        }
        void color(int col) { p.setColor(col); p.setStyle(Paint.Style.FILL); }
        void text(String s, float x, float y, float size, int col, boolean bold) {
            color(col); p.setTextSize(size); p.setTypeface(bold?Typeface.create("sans-serif",Typeface.BOLD):Typeface.create("sans-serif",Typeface.NORMAL));
            p.setTextAlign(Paint.Align.CENTER); c.drawText(s,x,y,p);
        }
        void rounded(float l,float t,float r,float b,float rad,int col) {
            color(col); c.drawRoundRect(l,t,r,b,rad,rad,p);
        }
        @Override protected void onDraw(Canvas canvas) {
            super.onDraw(canvas); c=canvas; w=getWidth(); h=getHeight();
            if(w==0||h==0)return;
            if(screen==0) drawMenu();
            else if(screen==3) drawAbout();
            else { drawWorld(); if(screen==2) drawGameOver(); }
            if(screen==1 && !paused) update();
            postInvalidateDelayed(16);
        }
        void drawBackground() {
            color(Color.rgb(116,202,245)); c.drawRect(0,0,w,h,p);
            color(Color.rgb(255,220,120)); c.drawCircle(w*.80f,h*.15f,w*.075f,p);
            color(Color.rgb(94,181,130));
            Path hills=new Path(); hills.moveTo(0,h*.62f); hills.quadTo(w*.22f,h*.40f,w*.48f,h*.62f); hills.quadTo(w*.75f,h*.42f,w,h*.60f); hills.lineTo(w,h); hills.lineTo(0,h); hills.close(); c.drawPath(hills,p);
            color(Color.rgb(68,151,91)); c.drawRect(0,ground,w,h,p);
            color(Color.rgb(240,220,150)); c.drawRect(0,ground,w,ground+8,p);
        }
        void drawWorld() {
            ground=h*.79f; playerSize=Math.min(w,h)*.105f; playerX=w*.20f;
            drawBackground();
            // track dashes
            color(Color.rgb(230,238,190));
            for(int i=0;i<7;i++){float x=(i*w/6f+(score*2)% (w/6f)); c.drawRoundRect(x,ground+ h*.09f,x+35,ground+h*.105f,5,5,p);}
            // obstacle
            if(screen==1 || screen==2) {
                rounded(obstacleX,ground-obstacleH,obstacleX+obstacleW,ground,12,Color.rgb(190,76,48));
                rounded(obstacleX+obstacleW*.18f,ground-obstacleH+10,obstacleX+obstacleW*.42f,ground-8,5,Color.rgb(240,145,70));
                // coin
                color(Color.rgb(255,190,30)); c.drawCircle(coinX,coinY,playerSize*.22f,p);
                color(Color.rgb(255,235,120)); p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(4); c.drawCircle(coinX,coinY,playerSize*.14f,p); p.setStyle(Paint.Style.FILL);
                // runner
                float py=playerY;
                color(Color.rgb(30,55,105)); c.drawRoundRect(playerX-playerSize*.30f,py-playerSize*.05f,playerX+playerSize*.30f,py+playerSize*.55f,playerSize*.16f,playerSize*.16f,p);
                color(Color.rgb(245,190,145)); c.drawCircle(playerX,py-playerSize*.25f,playerSize*.25f,p);
                color(Color.WHITE); c.drawCircle(playerX+playerSize*.09f,py-playerSize*.30f,playerSize*.045f,p);
                color(Color.rgb(35,35,35)); c.drawCircle(playerX+playerSize*.105f,py-playerSize*.30f,playerSize*.02f,p);
                color(Color.rgb(240,240,240)); c.drawRoundRect(playerX-playerSize*.28f,py+playerSize*.48f,playerX-playerSize*.02f,py+playerSize*.82f,8,8,p);
                c.drawRoundRect(playerX+playerSize*.02f,py+playerSize*.48f,playerX+playerSize*.28f,py+playerSize*.82f,8,8,p);
            }
            text("SCORE: "+score,w*.22f,h*.07f, h*.032f,Color.WHITE,true);
            text("LEVEL: "+level,w*.78f,h*.07f,h*.032f,Color.WHITE,true);
            text("BEST: "+best,w*.50f,h*.115f,h*.023f,Color.WHITE,true);
            if(screen==1) text("Ⅱ",w*.94f,h*.07f,h*.045f,Color.WHITE,true);
            if(paused && screen==1) {
                rounded(w*.25f,h*.35f,w*.75f,h*.58f,24,0xDD17324D);
                text("PAUSED",w*.5f,h*.43f,h*.055f,Color.WHITE,true);
                text("Tap to continue",w*.5f,h*.51f,h*.028f,Color.WHITE,false);
            } else if(screen==1) text("TAP TO JUMP",w*.5f,h*.95f,h*.025f,0xEEFFFFFF,true);
        }
        void reset() {
            score=0; level=1; coins=0; speed=w*.42f; ground=h*.79f;
            playerSize=Math.min(w,h)*.105f;
            playerY=ground-playerSize*.82f; vy=0; jumping=false;
            obstacleW=playerSize*.48f; obstacleH=playerSize*(.65f+random.nextFloat()*.45f);
            obstacleX=w+playerSize*2;
            coinX=w+playerSize*5; coinY=ground-playerSize*(1.2f+random.nextFloat()*.8f);
            last=System.currentTimeMillis(); paused=false; screen=1;
        }
        void update() {
            long now=System.currentTimeMillis(); float dt=Math.min(.04f,(now-last)/1000f); last=now;
            if(dt<=0)return;
            ground=h*.79f; playerSize=Math.min(w,h)*.105f;
            if(speed==0) speed=w*.42f;
            speed=w*(.42f+(level-1)*.055f);
            obstacleX-=speed*dt;
            coinX-=speed*dt;
            if(jumping) {
                vy += h*2.7f*dt;
                playerY += vy*dt;
                if(playerY>=ground-playerSize*.82f){playerY=ground-playerSize*.82f;vy=0;jumping=false;}
            }
            if(obstacleX+obstacleW<0) {
                obstacleX=w+random.nextInt(Math.max(1,(int)(w*.35f)));
                obstacleH=playerSize*(.65f+random.nextFloat()*.65f);
                score+=10;
                int newLevel=score/100+1;
                if(newLevel>level) level=newLevel;
            }
            if(coinX<playerX && coinX>w*.0f && Math.abs(coinY-(playerY-playerSize*.1f))<playerSize*1.25f) {
                coins++; score+=5; coinX=-100;
            }
            if(coinX+playerSize<0){coinX=w+random.nextInt(Math.max(1,(int)(w*.6f)));coinY=ground-playerSize*(1.2f+random.nextFloat()*1.1f);}
            float playerBottom=playerY+playerSize*.82f;
            float playerTop=playerY-playerSize*.5f;
            if(obstacleX<playerX+playerSize*.22f && obstacleX+obstacleW>playerX-playerSize*.22f &&
               playerBottom>ground-obstacleH+8 && playerTop<ground) {
                screen=2; if(score>best){best=score;prefs.edit().putInt("best",best).apply();}
            }
        }
        void drawMenu() {
            ground=h*.79f; drawBackground();
            text("RUNNER",w*.5f,h*.20f,h*.095f,Color.WHITE,true);
            text("ADVENTURE",w*.5f,h*.27f,h*.045f,Color.rgb(255,235,150),true);
            // runner icon
            color(Color.rgb(30,55,105)); c.drawRoundRect(w*.43f,h*.38f,w*.57f,h*.52f,20, p);
            color(Color.rgb(245,190,145)); c.drawCircle(w*.5f,h*.35f,w*.055f,p);
            rounded(w*.35f,h*.63f,w*.65f,h*.72f,24,Color.rgb(28,95,145));
            text("PLAY",w*.5f,h*.695f,h*.045f,Color.WHITE,true);
            rounded(w*.35f,h*.75f,w*.65f,h*.83f,24,Color.rgb(35,110,80));
            text("ABOUT ME",w*.5f,h*.805f,h*.032f,Color.WHITE,true);
            text("Best score: "+best,w*.5f,h*.90f,h*.028f,Color.WHITE,true);
            text("Jump over obstacles • Collect coins",w*.5f,h*.95f,h*.022f,Color.WHITE,false);
        }
        void drawGameOver() {
            rounded(w*.13f,h*.28f,w*.87f,h*.70f,28,0xEE17324D);
            text("GAME OVER",w*.5f,h*.38f,h*.06f,Color.WHITE,true);
            text("Score: "+score,w*.5f,h*.47f,h*.04f,Color.WHITE,true);
            text("Coins: "+coins+"     Level: "+level,w*.5f,h*.53f,h*.03f,Color.WHITE,false);
            text("Best: "+best,w*.5f,h*.59f,h*.03f,Color.rgb(255,220,100),true);
            rounded(w*.28f,h*.62f,w*.72f,h*.69f,18,Color.rgb(28,120,80));
            text("PLAY AGAIN",w*.5f,h*.665f,h*.03f,Color.WHITE,true);
        }
        void drawAbout() {
            drawBackground();
            text("ABOUT THE CREATOR",w*.5f,h*.13f,h*.045f,Color.WHITE,true);
            String[] lines={
                "Name: Ezatullah Mirza",
                "Computer Science Graduate",
                "Information Technology",
                "Computer Teacher",
                "Interested in Networking",
                "Learning: CCNA → CCNP → CCIE",
                "",
                "This game was made as a",
                "personal Android project."
            };
            float y=h*.27f;
            for(String s:lines){text(s,w*.5f,y,h*.029f,Color.WHITE,s.startsWith("Name"));y+=h*.052f;}
            rounded(w*.32f,h*.82f,w*.68f,h*.90f,20,Color.rgb(28,95,145));
            text("BACK",w*.5f,h*.87f,h*.035f,Color.WHITE,true);
        }
        @Override public boolean onTouchEvent(android.view.MotionEvent e) {
            if(e.getAction()!=MotionEvent.ACTION_DOWN)return true;
            float x=e.getX(), y=e.getY();
            if(screen==0) {
                if(y>h*.60f && y<h*.74f) reset();
                else if(y>h*.74f && y<h*.85f) screen=3;
            } else if(screen==3) { if(y>h*.79f)screen=0; }
            else if(screen==2) { if(y>h*.60f && y<h*.73f) reset(); else screen=0; }
            else if(screen==1) {
                if(paused) paused=false;
                else if(y<h*.16f && x>w*.78f) paused=true;
                else if(!jumping) {vy=-h*1.05f;jumping=true;}
            }
            invalidate(); return true;
        }
        void pauseGame(){ if(screen==1) paused=true; }
        void resumeGame(){ if(screen==1 && paused) { last=System.currentTimeMillis(); } }
    }
}
