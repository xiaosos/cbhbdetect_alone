package com.cbhb.com.cbhb.service;

import com.cbhb.dao.entity.DetectResult;
import com.cbhb.dao.mapper.DetectResultMapper;
import com.cbhb.util.Detect;
import com.cbhb.util.DetectResultDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Service
public class DetectService {
    @Resource
    @Autowired
    DetectResultMapper detectResultMapper;

    @Value("${logfilepath}")
    private String logfilepath;

    @Value("${imgPath}")
    private String imgPath;

    ThreadLocal<Detect> tl = new ThreadLocal<Detect>(){
        @Override
        protected Detect initialValue() {
            return new Detect(imgPath);
        }
    };

    public void autoDetect90(int cardnum,int loopNum){

        Detect detect = tl.get();
        for (int i = 0; i < loopNum; i++) {
            String car_str = "0000000000"+String.valueOf(cardnum+i);
            System.out.printf("Thread is %s  and car is %s !\n",Thread.currentThread().getName(),car_str);
            car_str = car_str.substring(car_str.length()-9);
            System.out.println(car_str );
            List<DetectResultDto> detectResultDtos = detect.detectCard90(car_str, logfilepath);
            List<DetectResultDto> saveList = detectResultDtos.stream().filter(dr -> dr.getDetectType() == DetectResultDto.DetectType.CAN || dr.getDetectType() == DetectResultDto.DetectType.ALREADY).collect(Collectors.toList());
            insertForeach(saveList);
        }
        tl.remove();
    }

    public int insertDetectResult(DetectResultDto dto)
    {
        DetectResult detectResult = new DetectResult();
        detectResult.setAmt(dto.getAmt());
        detectResult.setMessage(dto.getMessage());
        detectResult.setCardnum(dto.getCardnum());
//        detectResult.setId(12345);
        detectResult.setDate(dto.getDate().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        detectResult.setDetecttime(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));

        detectResult.setDetecttype(dto.getDetectType().toString());

        return detectResultMapper.insert(detectResult);
    }

    public void insertDtos(List<DetectResultDto> dtos){
        dtos.stream().forEach(dto-> insertDetectResult(dto));

    }

    public void insertForeach(List<DetectResultDto> dtos){
        List<DetectResult> list = new LinkedList<>();
        dtos.stream().forEach(dto->{
            DetectResult detectResult = new DetectResult();
            detectResult.setDetecttime(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
            detectResult.setDate(dto.getDate().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
            detectResult.setCardnum(dto.getCardnum());
            detectResult.setMessage(dto.getMessage());
            detectResult.setAmt(dto.getAmt());
            detectResult.setDetecttype(dto.getDetectType().toString());
            list.add(detectResult);
        });
        if(list.size()>0) {
            detectResultMapper.insertForeach(list);
        }
    }

    public List<DetectResult> search(String date){
//        DetectResult detectResult = detectResultMapper.selectByPrimaryKey(77981);
//        return detectResult;
        List<DetectResult> detectResults = detectResultMapper.selectByDate(date);
        return detectResults;
    }

    public Set<String> filter90cardnum(){
        LocalDate localDate = LocalDate.now();
        LocalDate minusDays = localDate.minusDays(89);
        String date = minusDays.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
//        Set<String> cardnumSet = new HashSet();
        Set<String> detectResults = search(date).stream().filter(dr->dr.getAmt()>0).map(DetectResult::getCardnum).collect(Collectors.toSet());
        return detectResults;
    }

    public boolean isSafe(String cardnum,String logfilepath){
        Detect detect = tl.get();
        return detect.isSecurityIn90(cardnum,logfilepath);
    }


}
