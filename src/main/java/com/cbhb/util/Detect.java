package com.cbhb.util;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

import java.io.*;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Detect {

    public static String FIRST_PAGE="http://www.shenzhentong.com/service/invoice_101007009.html";

    public static String YZM_PAGE="http://www.shenzhentong.com/ajax/WaterMark.ashx";

    public static String LOGIN_PAGE="http://www.shenzhentong.com/Ajax/ElectronicInvoiceAjax.aspx";

    public static String DETECT_PAGE="http://www.shenzhentong.com/service/fplist_101007009_";

     CloseableHttpClient httpClient = null;

     CookieStore cookieStore = null;

     private String imgPath;

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
    public static void main(String[] args) throws URISyntaxException {





            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
            LocalDate localDate = LocalDate.now();
            System.out.println(localDate.format(formatter));

            System.out.println(localDate.plusDays(1).format(formatter));
            System.out.println(localDate.minusDays(31));



        //开过的含有:发票信息
        //不能开的含有:display:none

    }

    public Detect(String imgPath){
        this.imgPath = imgPath;
    }

    public  void getCookie()  {

        try {
            URIBuilder uriBuilder = new URIBuilder(FIRST_PAGE);
            cookieStore = new BasicCookieStore();
            httpClient = HttpClients.custom().setDefaultCookieStore(cookieStore).build();
            HttpGet httpGet = new HttpGet(uriBuilder.build());
            CloseableHttpResponse response = httpClient.execute(httpGet);
            response.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public  void getYZM(){
        try {
            URIBuilder uriBuilder = new URIBuilder(YZM_PAGE);
            HttpPost postYZM = new HttpPost(uriBuilder.build());

            CloseableHttpResponse yzmResponse = httpClient.execute(postYZM);
            HttpEntity entity = yzmResponse.getEntity();
            FileOutputStream fos = new FileOutputStream(new File(imgPath+"/javaimg.gif"));
            entity.writeTo(fos);
            yzmResponse.close();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public  boolean buildSession(String yzm,String cardnum){
        URIBuilder uriBuilder = null;
        try {
            uriBuilder = new URIBuilder(LOGIN_PAGE);
            HttpPost postLogin = new HttpPost(uriBuilder.build());
            List<NameValuePair> list = new LinkedList<>();
            BasicNameValuePair tpNP = new BasicNameValuePair("tp","1");
            BasicNameValuePair yzmNP = new BasicNameValuePair("yzm",yzm);
            BasicNameValuePair cardnumNP = new BasicNameValuePair("cardnum",cardnum);

            list.add(tpNP);
            list.add(yzmNP);
            list.add(cardnumNP);
            UrlEncodedFormEntity entityParam = new UrlEncodedFormEntity(list, "UTF-8");
            postLogin.setEntity(entityParam);
            CloseableHttpResponse loginResponse = httpClient.execute(postLogin);
            HttpEntity entity = loginResponse.getEntity();
            String response_content = IOUtils.toString(entity.getContent());
            if (response_content!=null && response_content.contains("100")){
                return true;
            }
            loginResponse.close();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public DetectResultDto find(String cardnum, String detectDate){
        DetectResultDto detectResultDto = new DetectResultDto();
        String message = null;
        LocalDate localDate = LocalDate.parse(detectDate,formatter);
        try {
            URIBuilder uriBuilder = new URIBuilder(DETECT_PAGE+cardnum+"_"+detectDate+".html");
            HttpGet detectGet = new HttpGet(uriBuilder.build());
            CloseableHttpResponse detectResponse = httpClient.execute(detectGet);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            detectResponse.getEntity().writeTo(baos);
            String content = baos.toString("UTF-8");
            detectResultDto.setCardnum(cardnum);
            detectResultDto.setDate(localDate);
//FileOutputStream fos = new FileOutputStream("d:/test/"+detectDate);
//fos.write(content.getBytes());
//fos.close();
            if(content==null){
                detectResultDto.setDetectType(DetectResultDto.DetectType.ERROR);
                detectResultDto.setMessage("异常");
                detectResultDto.setAmt(0);
                detectResponse.close();
                return detectResultDto;
            }
            if(content.contains("display:none")){
                detectResultDto.setDetectType(DetectResultDto.DetectType.NORECORD);
                detectResultDto.setMessage("----无充值记录");
                detectResultDto.setAmt(0);
                detectResponse.close();
                return detectResultDto;
            }else if (content.contains("发票信息")){

                detectResultDto.setDetectType(DetectResultDto.DetectType.ALREADY);
                detectResultDto.setMessage("----已经开过了------------");
                detectResultDto.setAmt(0);
                detectResponse.close();
                return detectResultDto;
            }else{
                StringBuilder sb = new StringBuilder(cardnum+":"+detectDate+"  ******可以开******");
                Pattern pattern = Pattern.compile("((\\d)*)元");
                int amt =0;
                for (String s : content.split("\n")) {
                    if (s!=null && s.contains("odd_body") && s.contains("ipcbcxjg")){
                        Matcher matcher = pattern.matcher(s);
                        if(matcher.find()){
                            sb.append(matcher.group()).append("###");
//                            System.out.println("match.group0:"+matcher.group(0));
//                            System.out.println("match.group1:"+matcher.group(1));
//                            System.out.println("match.group2:"+matcher.group(2));
                            amt+=Integer.parseInt(matcher.group(1));
                        }
                    }
                }
                detectResultDto.setDetectType(DetectResultDto.DetectType.CAN);
                detectResultDto.setMessage(sb.toString());
                detectResultDto.setAmt(amt);
                detectResponse.close();
                return detectResultDto;
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return detectResultDto;
    }
    //探测指定卡号90天的记录
    public List<DetectResultDto> detectCard90(String cardnum,String logfilepath){
        List<DetectResultDto> list = new ArrayList<>();
        getCookie();
        getYZM();
        for (int i  = 0; i < 500; i++) {
            if(buildSession(""+i,cardnum)){
                break;
            }
            if(i>=300) throw new RuntimeException("验证码未通过......");
        }

        LocalDate localDate = LocalDate.now();

        for (int i = 0; i <=90; i++) {
            localDate = localDate.minusDays(1);
            String detectDate = localDate.format(formatter);
            DetectResultDto detectResultDto = find(cardnum, detectDate);
            list.add(detectResultDto);
        }

        list.stream().filter(dr->dr.getDetectType()== DetectResultDto.DetectType.CAN || dr.getDetectType()== DetectResultDto.DetectType.ALREADY).forEach(System.out::println);

        try {
            FileOutputStream fos = new FileOutputStream(logfilepath+"/detect-"+Thread.currentThread().getName()+".log",true);
            fos.write(("\n======"+cardnum+"========START"+LocalDate.now().format(formatter)+"=========================\n").getBytes());
            list.stream().filter(dr->dr.getDetectType()== DetectResultDto.DetectType.CAN || dr.getDetectType()== DetectResultDto.DetectType.ALREADY).forEach(d -> {
                try {
                    fos.write((d.toString()+"\n").getBytes());

                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            fos.write(("\n======"+cardnum+"========END"+LocalDate.now().format(formatter)+"=========================\n").getBytes());
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return list;
    }

}
