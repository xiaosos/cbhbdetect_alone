package com.cbhb.dao.entity;

public class DetectResult {
    @Override
    public String toString() {
        return "DetectResult{" +
                "id=" + id +
                ", cardnum='" + cardnum + '\'' +
                ", date='" + date + '\'' +
                ", message='" + message + '\'' +
                ", detecttype='" + detecttype + '\'' +
                ", amt=" + amt +
                ", detecttime='" + detecttime + '\'' +
                '}';
    }

    private Integer id;

    private String cardnum;

    private String date;

    private String message;

    private String detecttype;

    private Integer amt;

    private String detecttime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCardnum() {
        return cardnum;
    }

    public void setCardnum(String cardnum) {
        this.cardnum = cardnum == null ? null : cardnum.trim();
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date == null ? null : date.trim();
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message == null ? null : message.trim();
    }

    public String getDetecttype() {
        return detecttype;
    }

    public void setDetecttype(String detecttype) {
        this.detecttype = detecttype == null ? null : detecttype.trim();
    }

    public Integer getAmt() {
        return amt;
    }

    public void setAmt(Integer amt) {
        this.amt = amt;
    }

    public String getDetecttime() {
        return detecttime;
    }

    public void setDetecttime(String detecttime) {
        this.detecttime = detecttime == null ? null : detecttime.trim();
    }
}