package xyz.yxy.calculator;

import android.os.Bundle;
import android.os.Vibrator;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    public String s="";
    public String lasts="";
    public String result="";
    public static String removeCharAt(String s, int pos) {
        return s.substring(0, pos) + s.substring(pos + 1);// 使用substring()方法截取0-pos之间的字符串+pos之后的字符串，相当于将要把要删除的字符串删除
    }
    public void input_output(String input){
        float a=0;
        int b=0;
        int l=0;
        TextView text = findViewById(R.id.input_message);
        if(lasts=="="){
            a=Float.parseFloat(result);
            if(a%1==0){
                b=(int)a;
                result=b+"";
            }
            s=result;
        }
        if(input=="down"){
            s=result;
            text.setText(s);
            return;
        }
        if(input=="="){
            TextView text2 = findViewById(R.id.output_message);
            try {
                result=getResult(s);
                text2.setText(result);
            } catch (Exception e) {
                Toast.makeText(MainActivity.this,"表达式有误",Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
            return;
        }
        if(input=="del" && s.length()>0){
//            s=removeCharAt(s, s.length());
            l=s.length();
            s=s.substring(0,l-1);
            text.setText(s);
            return;
        }
        if(input!="del"){
            s=s+input;
            text.setText(s);
            if(input==null){
                text.setText(null);
                s="";
            }
            lasts=input;
        }

    }

    public static double doubleCal(double a1, double a2, char operator) throws Exception {
        switch (operator) {
            case '+':
                return a1 + a2;
            case '-':
                return a1 - a2;
            case '*':
                return a1 * a2;
            case '/':
                return a1 / a2;
            default:
                break;
        }
        throw new Exception("illegal operator!");
    }

    public static String getResult(String str) throws NumberFormatException, Exception {

        //处理一下计算过程中出现的--情况,首位--直接去掉，中间--变为+
        str = str.startsWith("--") ? str.substring(2) : str;
        str = str.replaceAll("--", "+");
        str = str.replaceAll("\\+-", "-");
        System.out.println("新表达式：" + str);
        if (str.matches("-{0,1}[0-9]+([.][0-9]+){0,1}"))//不存在运算符了，即递归结束，这里的正则为匹配所有的正负整数及小数
            return str;

        /*表示每次递归计算完一步后的表达式*/
        String newExpr = null;
        // 第一步：去括号至无括号
        if (str.contains("(")) {
            /*最后一个左括号的索引值*/
            int lIndex = str.lastIndexOf("(");
            /*该左括号对应的右括号的索引*/
            int rIndex = str.indexOf(")", lIndex);
            /*括号中的字表达式*/
            String subExpr = str.substring(lIndex + 1, rIndex);
            System.out.println("准备括号：(" + subExpr + ")");
            newExpr = str.substring(0, lIndex) + getResult(subExpr) //调用本身，计算括号中表达式结果
                    + str.substring(rIndex + 1);
            return getResult(newExpr);
        }

        // 第二步：去乘除至无乘除
        if (str.contains("*") || str.contains("/")) {
            /*该正则表示匹配一个乘除运算，如1.2*3  1.2/3  1.2*-2 等*/
            Pattern p = Pattern.compile("[0-9]+([.][0-9]+){0,1}[*/]-{0,1}[0-9]+([.][0-9]+){0,1}");
            Matcher m = p.matcher(str);
            if (m.find()) {
                /*第一个乘除表达式*/
                String temp = m.group();
                System.out.println("计算乘除：" + temp);
                String[] a = temp.split("[*/]");
                newExpr = str.substring(0, m.start())
                        + doubleCal(Double.valueOf(a[0]), Double.valueOf(a[1]), temp.charAt(a[0].length()))
                        + str.substring(m.end());
            }
            return getResult(newExpr);
        }
        // 第三步：去加减至无加减
        if (str.contains("+") || str.contains("-")) {
            /*该正则表示匹配一个乘除运算，如1.2+3  1.2-3  1.2--2  1.2+-2等*/
            Pattern p = Pattern.compile("-{0,1}[0-9]+([.][0-9]+){0,1}[+-][0-9]+([.][0-9]+){0,1}");
            Matcher m = p.matcher(str);
            if (m.find()) {
                /*第一个加减表达式*/
                String temp = m.group();
                System.out.println("计算加减："+temp);
                String[] a = temp.split("\\b[+-]", 2);
                newExpr = str.substring(0, m.start())
                        + doubleCal(Double.valueOf(a[0]), Double.valueOf(a[1]), temp.charAt(a[0].length()))
                        + str.substring(m.end());
            }
            return getResult(newExpr);
        }
        throw new Exception("Calculation error");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this,"清空",Toast.LENGTH_LONG).show();
                input_output(null);
            }
        });

        Button b0=findViewById(R.id.b0);

        b0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v0) {
                Toast.makeText(MainActivity.this,"0",Toast.LENGTH_SHORT).show();
                input_output("0");
            }
        });
        Button b1=findViewById(R.id.b1);
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this,"1",Toast.LENGTH_SHORT).show();
                input_output("1");
            }
        });
        Button b2=findViewById(R.id.b2);
        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this,"2",Toast.LENGTH_SHORT).show();
                input_output("2");
            }
        });
        Button b3=findViewById(R.id.b3);
        b3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this,"3",Toast.LENGTH_SHORT).show();
                input_output("3");
            }
        });
        Button b4=findViewById(R.id.b4);
        b4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this,"4",Toast.LENGTH_SHORT).show();
                input_output("4");
            }
        });
        Button b5=findViewById(R.id.b5);
        b5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this,"5",Toast.LENGTH_SHORT).show();
                input_output("5");
            }
        });
        Button b6=findViewById(R.id.b6);
        b6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this,"6",Toast.LENGTH_SHORT).show();
                input_output("6");
            }
        });
        Button b7=findViewById(R.id.b7);
        b7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this,"7",Toast.LENGTH_SHORT).show();
                input_output("7");
            }
        });
        Button b8=findViewById(R.id.b8);
        b8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this,"8",Toast.LENGTH_SHORT).show();
                input_output("8");
            }
        });
        Button b9=findViewById(R.id.b9);
        b9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this,"9",Toast.LENGTH_SHORT).show();
                input_output("9");
            }
        });
        Button dengyu=findViewById(R.id.dengyu);
        dengyu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this,"=",Toast.LENGTH_SHORT).show();
                input_output("=");
            }
        });
        Button jia=findViewById(R.id.jia);
        jia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this,"+",Toast.LENGTH_SHORT).show();
                input_output("+");
            }
        });
        Button jian=findViewById(R.id.jian);
        jian.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this,"-",Toast.LENGTH_SHORT).show();
                input_output("-");
            }
        });
        Button cheng=findViewById(R.id.cheng);
        cheng.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this,"x",Toast.LENGTH_SHORT).show();
                input_output("*");
            }
        });
        Button chu=findViewById(R.id.chu);
        chu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this,"/",Toast.LENGTH_SHORT).show();
                input_output("/");
            }
        });
        Button dian=findViewById(R.id.dian);
        dian.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this,".",Toast.LENGTH_SHORT).show();
                input_output(".");
            }
        });
        Button del=findViewById(R.id.del);
        del.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this,"del",Toast.LENGTH_SHORT).show();
                input_output("del");
            }
        });
        Button left=findViewById(R.id.left);
        left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this,"(",Toast.LENGTH_SHORT).show();
                input_output("(");
            }
        });
        Button right=findViewById(R.id.right);
        right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this,")",Toast.LENGTH_SHORT).show();
                input_output(")");
            }
        });
        Button down=findViewById(R.id.down);
        down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this,"down",Toast.LENGTH_SHORT).show();
                input_output("down");
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
