package com.cbhb.job;

import com.cbhb.com.cbhb.service.DetectService;
import com.cbhb.util.DetectAndDetail;
import com.cbhb.util.DetectResultDto;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.Set;

public class CronJob extends QuartzJobBean {

    @Autowired
    DetectService detectService;
    @Value("${logfilepath}")
    String logfilepath;
    @Value("${imgPath}")
    private String imgPath;
    @Value("${pdfPath}")
    private String pdfPath;

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        System.out.println("====="+System.currentTimeMillis()+"============CronJob start  =============");
        LocalDate localDate = LocalDate.now();
        LocalDate minusDays = localDate.minusDays(89);
        String date = minusDays.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        Set<String> cardnums = detectService.filter90cardnum();
        for(String cardnum: cardnums){
            if(detectService.isSafe(cardnum,logfilepath)){
                DetectAndDetail dad = new DetectAndDetail(imgPath);
                dad.getCookie();
                dad.getYZM();
                for (int i = 0; i < 500; i++) {
                    if (dad.buildSession("" + i, cardnum)) {
                        System.out.println("Check Ok !!!");
                        break;
                    }
                    if (i >= 300) throw new RuntimeException("验证码未通过......");
                }
                DetectResultDto dto = dad.find(cardnum, date);
                String pdfFilePath = pdfPath+"/" + cardnum + "-" + date + "-" + dto.getAmt() + ".pdf";
                dad.download(dad.getDetail(), pdfFilePath);

            }
        }

    }
}
