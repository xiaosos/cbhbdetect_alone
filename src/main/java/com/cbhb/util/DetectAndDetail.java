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

public class DetectAndDetail {

    public static String FIRST_PAGE = "http://www.shenzhentong.com/service/invoice_101007009.html";

    public static String YZM_PAGE = "http://www.shenzhentong.com/ajax/WaterMark.ashx";

    public static String LOGIN_PAGE = "http://www.shenzhentong.com/Ajax/ElectronicInvoiceAjax.aspx";

    public static String DETECT_PAGE = "http://www.shenzhentong.com/service/fplist_101007009_";

    public static String DETAIL_PAGE = "http://www.shenzhentong.com/Ajax/ElectronicInvoiceAjax.aspx";

    public String global_content = "";

    public static String DOWNLOAD_PAGE = "http://www.shenzhentong.com/service/fpdetail.aspx?nodecode=101007009&pid=";



    CloseableHttpClient httpClient = null;

    CookieStore cookieStore = null;

    private String imgPath;

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

    public static void main(String[] args) throws URISyntaxException {
        String cardnum = "689549173";
        String detectDate = "20191017";
        DetectAndDetail dad = new DetectAndDetail("e:/");
        dad.getCookie();

        dad.getYZM();
        for (int i = 0; i < 500; i++) {
            if (dad.buildSession("" + i, cardnum)) {
                System.out.println("Check Ok !!!");
                break;
            }
            if (i >= 300) throw new RuntimeException("验证码未通过......");
        }

        DetectResultDto dto = dad.find(cardnum, detectDate);

        System.out.println("DTO:\n" + dto);
        String pdfPath = "d:/" + cardnum + "-" + detectDate + "-开" + dto.getAmt() + "元.pdf";
        dad.download(dad.getDetail(), pdfPath);

        //开过的含有:发票信息
        //不能开的含有:display:none

    }


    public DetectAndDetail(String imgPath) {
        this.imgPath = imgPath;
    }

    public void getCookie() {

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

    public void getYZM() {
        try {
            URIBuilder uriBuilder = new URIBuilder(YZM_PAGE);
            HttpPost postYZM = new HttpPost(uriBuilder.build());

            CloseableHttpResponse yzmResponse = httpClient.execute(postYZM);
            HttpEntity entity = yzmResponse.getEntity();
            FileOutputStream fos = new FileOutputStream(new File(imgPath + "/javaimg.gif"));
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

    public boolean buildSession(String yzm, String cardnum) {
        URIBuilder uriBuilder = null;
        try {
            uriBuilder = new URIBuilder(LOGIN_PAGE);
            HttpPost postLogin = new HttpPost(uriBuilder.build());
            List<NameValuePair> list = new LinkedList<>();
            BasicNameValuePair tpNP = new BasicNameValuePair("tp", "1");
            BasicNameValuePair yzmNP = new BasicNameValuePair("yzm", yzm);
            BasicNameValuePair cardnumNP = new BasicNameValuePair("cardnum", cardnum);

            list.add(tpNP);
            list.add(yzmNP);
            list.add(cardnumNP);
            UrlEncodedFormEntity entityParam = new UrlEncodedFormEntity(list, "UTF-8");
            postLogin.setEntity(entityParam);
            CloseableHttpResponse loginResponse = httpClient.execute(postLogin);
            HttpEntity entity = loginResponse.getEntity();
            String response_content = IOUtils.toString(entity.getContent());
            if (response_content != null && response_content.contains("100")) {
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

    public DetectResultDto find(String cardnum, String detectDate) {
        DetectResultDto detectResultDto = new DetectResultDto();
        String message = null;
        LocalDate localDate = LocalDate.parse(detectDate, formatter);
        try {
            URIBuilder uriBuilder = new URIBuilder(DETECT_PAGE + cardnum + "_" + detectDate + ".html");
            HttpGet detectGet = new HttpGet(uriBuilder.build());
            CloseableHttpResponse detectResponse = httpClient.execute(detectGet);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            detectResponse.getEntity().writeTo(baos);
            String content = baos.toString("UTF-8");
            global_content = content;
            detectResultDto.setCardnum(cardnum);
            detectResultDto.setDate(localDate);
//FileOutputStream fos = new FileOutputStream("d:/test/"+detectDate);
//fos.write(content.getBytes());
//fos.close();
            if (content == null) {
                detectResultDto.setDetectType(DetectResultDto.DetectType.ERROR);
                detectResultDto.setMessage("异常");
                detectResultDto.setAmt(0);
                detectResponse.close();
                return detectResultDto;
            }
            if (content.contains("display:none")) {
                detectResultDto.setDetectType(DetectResultDto.DetectType.NORECORD);
                detectResultDto.setMessage("----无充值记录");
                detectResultDto.setAmt(0);
                detectResponse.close();
                return detectResultDto;
            } else if (content.contains("发票信息")) {

                detectResultDto.setDetectType(DetectResultDto.DetectType.ALREADY);
                detectResultDto.setMessage("----已经开过了------------");
                detectResultDto.setAmt(0);
                detectResponse.close();
                return detectResultDto;
            } else {
                StringBuilder sb = new StringBuilder(cardnum + ":" + detectDate + "  ******可以开******");
                Pattern pattern = Pattern.compile("((\\d)*)元");
                int amt = 0;
                for (String s : content.split("\n")) {
                    if (s != null && s.contains("odd_body") && s.contains("ipcbcxjg")) {
                        Matcher matcher = pattern.matcher(s);
                        if (matcher.find()) {
                            sb.append(matcher.group()).append("###");
//                            System.out.println("match.group0:"+matcher.group(0));
//                            System.out.println("match.group1:"+matcher.group(1));
//                            System.out.println("match.group2:"+matcher.group(2));
                            amt += Integer.parseInt(matcher.group(1));
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


    public String getDetail() {
        URIBuilder uriBuilder = null;

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

        for (String s : global_content.split("\n")) {
            if (s != null && s.contains("odd_body") && s.contains("ipcbcxjg")) {
                Matcher matcher = lshPattern.matcher(s);
                if (matcher.find()) {
                    lsh = matcher.group(1);
                }
                matcher = zdhPattern.matcher(s);
                if (matcher.find()) {
                    zdh = matcher.group(1);
                }
                matcher = khPattern.matcher(s);
                if (matcher.find()) {
                    kh = matcher.group(1);
                }
                matcher = rqPattern.matcher(s);
                if (matcher.find()) {
                    rq = matcher.group(1);
                }
                matcher = sjPattern.matcher(s);
                if (matcher.find()) {
                    sj = matcher.group(1);
                }
            }
        }
        String dataStr = "";
        try {
            uriBuilder = new URIBuilder(DETAIL_PAGE);
            HttpPost detectPost = new HttpPost(uriBuilder.build());

            List<NameValuePair> list = new LinkedList<>();
            BasicNameValuePair tpNP = new BasicNameValuePair("tp", "3");
            BasicNameValuePair jlshNP = new BasicNameValuePair("jlsh", lsh);
            BasicNameValuePair jzdhNP = new BasicNameValuePair("jzdh", zdh);
            BasicNameValuePair jkhNP = new BasicNameValuePair("jkh", kh);
            BasicNameValuePair jrqNP = new BasicNameValuePair("jrq", rq);
            BasicNameValuePair jsjNP = new BasicNameValuePair("jsj", sj);
            //发票抬头名称
            BasicNameValuePair jfirmfpmcNP = new BasicNameValuePair("jfirmfpmc", "渤海银行股份有限公司");
            //纳税人识别号
            BasicNameValuePair jfirmsbhNP = new BasicNameValuePair("jfirmsbh", "911200007109339563");
            //地址
            BasicNameValuePair jfirmaddreNP = new BasicNameValuePair("jfirmaddre", "");
            //电话号码：
            BasicNameValuePair jfirmtelNP = new BasicNameValuePair("jfirmtel", "");
            //开户银行：
            BasicNameValuePair jfirmyhNP = new BasicNameValuePair("jfirmyh", "");
            //银行账号：
            BasicNameValuePair jfirmyhzhNP = new BasicNameValuePair("jfirmyhzh", "");
            //手机号码：
            BasicNameValuePair jfirmphoneNP = new BasicNameValuePair("jfirmphone", "13526627725");

            list.add(tpNP);
            list.add(jlshNP);
            list.add(jzdhNP);
            list.add(jkhNP);
            list.add(jrqNP);
            list.add(jsjNP);
            list.add(jfirmfpmcNP);
            list.add(jfirmsbhNP);
            list.add(jfirmaddreNP);
            list.add(jfirmtelNP);
            list.add(jfirmyhNP);
            list.add(jfirmyhzhNP);
            list.add(jfirmphoneNP);


            UrlEncodedFormEntity entityParam = new UrlEncodedFormEntity(list, "UTF-8");

            detectPost.setEntity(entityParam);

            CloseableHttpResponse detectResponse = httpClient.execute(detectPost);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            detectResponse.getEntity().writeTo(baos);
            String content = baos.toString("UTF-8");

            System.out.println("开发票后的报文: \n" + content);

            Pattern strsPattern = Pattern.compile("strs\":\"((\\d)*)");

            Matcher matcher = strsPattern.matcher(content);

            if (matcher.find()) {
                dataStr = matcher.group(1);
            } else {
                //未匹配成功
                return "";
            }


        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return dataStr;
    }

//    public void download(){
//        //http://www.shenzhentong.com/service/fpdetail.aspx?nodecode=101007009&pid=2234975104208271806
//
//        try {
//            String dataStr=getDetail();
//            System.out.println("dataStr==========:"+dataStr);
//            if(dataStr==null || "".equals(dataStr))return;
//
//            URIBuilder uriBuilder = new URIBuilder(DOWNLOAD_PAGE+dataStr);
//            HttpGet downloadGet = new HttpGet(uriBuilder.build());
//            CloseableHttpResponse downloadResponse = httpClient.execute(downloadGet);
//            FileOutputStream downloadFile = new FileOutputStream("d:/hello.pdf");
//            downloadResponse.getEntity().writeTo(downloadFile);
////            ByteArrayOutputStream baos = new ByteArrayOutputStream();
////            downloadResponse.getEntity().writeTo(baos);
////            String content = baos.toString("UTF-8");
//        } catch (URISyntaxException e) {
//            e.printStackTrace();
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        } catch (ClientProtocolException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//
//    }


    public void download(String dataStr, String downpath) {


        try {
            Thread.sleep(Long.parseLong("3000"));
            if (dataStr == null || "".equals(dataStr)) {
                throw new RuntimeException("特征码不能为空");
            }
            CookieStore downCookie = new BasicCookieStore();
            CloseableHttpClient downClient = HttpClients.custom().setDefaultCookieStore(downCookie).build();
//        URIBuilder uriBuilder = new URIBuilder("http://dzfp.szhtxx.cn:10000/downInvoice/download/914403007703110594/2020/01/14/TG401656028850163712.pdf");
            URIBuilder uriBuilder = new URIBuilder("http://www.shenzhentong.com//service/fpdetail.aspx?nodecode=101007009&pid=" + dataStr);
            HttpGet downloadGet = new HttpGet(uriBuilder.build());
            CloseableHttpResponse downloadResponse = downClient.execute(downloadGet);
//        FileOutputStream downloadFile = new FileOutputStream("d:/hello1");
//        downloadResponse.getEntity().writeTo(downloadFile);
//        downloadFile.close();

            URIBuilder downURI = new URIBuilder("http://www.shenzhentong.com/ajax/electronicinvoiceajax.aspx");
            HttpPost postInvocation = new HttpPost(downURI.build());
            List<NameValuePair> list = new LinkedList<>();
            BasicNameValuePair tpNP = new BasicNameValuePair("tp", "4");
            BasicNameValuePair pidNP = new BasicNameValuePair("pid", dataStr);
            list.add(tpNP);
            list.add(pidNP);
            UrlEncodedFormEntity entityParam = new UrlEncodedFormEntity(list, "UTF-8");

            postInvocation.setEntity(entityParam);
            CloseableHttpResponse downloadExec = downClient.execute(postInvocation);
            ByteArrayOutputStream downBaos = new ByteArrayOutputStream();
            downloadExec.getEntity().writeTo(downBaos);
            String downloadResponse1 = downBaos.toString();
            System.out.println("downloadResponse1:" + downloadResponse1);
            if(downloadResponse1.split("strs\":\"").length<2){
                return;
            }
            String urlArr = downloadResponse1.split("strs\":\"")[1];
            String realURL = "";
            if (urlArr != null && urlArr.length() > 1) {
                System.out.println(urlArr.substring(0, urlArr.length() - 2));
                realURL = urlArr.substring(0, urlArr.length() - 2);
            }


            URIBuilder pdfURI = new URIBuilder(realURL);
            HttpGet pdfGet = new HttpGet(pdfURI.build());
            CloseableHttpResponse pdfResponse = downClient.execute(pdfGet);
            FileOutputStream pdfFOS = new FileOutputStream(downpath);
            pdfResponse.getEntity().writeTo(pdfFOS);

            pdfFOS.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
