package com.cbhb.controller;

import com.cbhb.com.cbhb.service.DetectService;
import com.cbhb.util.Detect;
import com.cbhb.util.DetectResultDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class DetectController {

    @Value("${logfilepath}")
    private String logfilepath;

    @Value("${imgPath}")
    private String imgPath;

    @Autowired
    DetectService detectService;

    @GetMapping("/index")
    public String index(Model model){
        model.addAttribute("hello","worldd");
        return "index";
    }

    @RequestMapping("/detect")
//    @ResponseBody
    public String add(@RequestParam String cardnum,Model model){
        Detect detect = new Detect(imgPath);
        List<DetectResultDto> detectResultDtos = detect.detectCard90(cardnum, logfilepath);

        List<DetectResultDto> saveList = detectResultDtos.stream().filter(dr -> dr.getDetectType() == DetectResultDto.DetectType.CAN || dr.getDetectType() == DetectResultDto.DetectType.ALREADY).collect(Collectors.toList());

        detectService.insertForeach(saveList);

        model.addAttribute("detectList",saveList);
        return "index";
    }

    @RequestMapping("/autodetect/{cardnum}/{loopNum}")
    public String autoDetect(@PathVariable int cardnum,@PathVariable int loopNum){
        Detect detect = new Detect(imgPath);
        for (int i = 0; i < loopNum; i++) {
            String car_str = "0000000000"+String.valueOf(cardnum+i);
            System.out.println(car_str);
            car_str = car_str.substring(car_str.length()-9);
            System.out.println(car_str);
            List<DetectResultDto> detectResultDtos = detect.detectCard90(car_str, logfilepath);
            List<DetectResultDto> saveList = detectResultDtos.stream().filter(dr -> dr.getDetectType() == DetectResultDto.DetectType.CAN || dr.getDetectType() == DetectResultDto.DetectType.ALREADY).collect(Collectors.toList());
            detectService.insertForeach(saveList);
        }

        return "ok";
    }
    @RequestMapping("/auto90/{cardnum}/{loopNum}")
    public String autoDetect90(@PathVariable int cardnum,@PathVariable int loopNum){

        detectService.autoDetect90(cardnum, loopNum);

        return "ok";
    }

}
