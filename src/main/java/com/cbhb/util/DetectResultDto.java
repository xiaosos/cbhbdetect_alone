package com.cbhb.util;

import java.time.LocalDate;

public class DetectResultDto {
    private String cardnum;
    private LocalDate date;
    private String message;
    private DetectType detectType;
    private int amt;

    @Override
    public String toString() {
        return "DetectResultDto{" +
                "cardnum='" + cardnum + '\'' +
                ", date=" + date +
                ", message='" + message + '\'' +
                ", detectType=" + detectType +
                ", amt=" + amt +
                '}';
    }

    public String getCardnum() {
        return cardnum;
    }

    public void setCardnum(String cardnum) {
        this.cardnum = cardnum;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public DetectType getDetectType() {
        return detectType;
    }

    public void setDetectType(DetectType detectType) {
        this.detectType = detectType;
    }

    public int getAmt() {
        return amt;
    }

    public void setAmt(int amt) {
        this.amt = amt;
    }

   public  static enum DetectType{
        ALREADY,//已经开过的
        CAN,//可以开
        NORECORD,//没充值记录
        ERROR//异常情况

    }

}
