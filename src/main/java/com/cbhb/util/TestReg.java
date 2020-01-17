package com.cbhb.util;

import org.apache.commons.io.IOUtils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestReg {


    public static void main(String[] args) {

        try {

            String hello = "{\"state\":\"100\",\"strs\":\"http://dzfp.szhtxx.cn:10000/downInvoice/download/914403007703110594/2020/01/14/TG401656028850163712.pdf\"}";


            String urlArr = hello.split("strs\":\"")[1];
            System.out.println(urlArr.substring(0,urlArr.length()-2));



            //{"state":"1","strs":"5400015600554142288","message":""}

//            Pattern strsPattern = Pattern.compile("strs()");
            Pattern strsPattern = Pattern.compile("strs\":\"((\\d)*)");

            String test = "{\"state\":\"1\",\"strs\":\"05400015600554142288\",\"message\":\"\"}";
            String ttt = "strsxxxxxxx";
            Matcher matcher = strsPattern.matcher(test);
            matcher.find();
            System.out.println(matcher.group(1));

            FileInputStream fis = new FileInputStream("e:/6.txt");

            String content = IOUtils.toString(fis,"GBK");

//            System.out.println(content);
//            Pattern pattern = Pattern.compile("((\\d)*)元");

            Pattern lshPattern = Pattern.compile("lsh=\"((\\d)*)");
            Pattern zdhPattern = Pattern.compile("zdh=\"((\\d)*)");
            Pattern khPattern = Pattern.compile("kh=\"((\\d)*)");
            Pattern rqPattern = Pattern.compile("rq=\"((\\d)*)");
            Pattern sjPattern = Pattern.compile("sj=\"((\\d)*)");

            String lsh = "";
            String zdh = "";
            String kh = "";
            String rq = "";
            String sj = "";


            StringBuilder sb = new StringBuilder();
            System.out.println();
            for (String s : content.split("\n")) {
//                System.out.println("++++"+s);
                if (s!=null && s.contains("odd_body") && s.contains("ipcbcxjg")){
                    Matcher match1 = lshPattern.matcher(s);
                    if(match1.find()){
                        lsh = match1.group(1);
                    }
                    match1 = zdhPattern.matcher(s);
                    if(match1.find()){
                        zdh = match1.group(1);
                    }
                    match1 = khPattern.matcher(s);
                    if(match1.find()){
                        kh = match1.group(1);
                    }
                    match1 = rqPattern.matcher(s);
                    if(match1.find()){
                        rq = match1.group(1);
                    }
                    match1 = sjPattern.matcher(s);
                    if(match1.find()){
                        sj = match1.group(1);
                    }
                }
            }

//            System.out.println("lsh:"+lsh);
//            System.out.println("zdh:"+zdh);
//            System.out.println("kh:"+kh);
//            System.out.println("rq:"+rq);
//            System.out.println("sj:"+sj);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
