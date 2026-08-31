package com.first.train.business.req;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.util.Date;

public class DailyTrainTicketSaveReq {

    private Long id;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @NotNull(message = "【日期】不能为空")
    private Date date;

    @NotBlank(message = "【车次编号】不能为空")
    private String trainCode;

    @NotBlank(message = "【出发站】不能为空")
    private String start;

    @NotBlank(message = "【出发站拼音】不能为空")
    private String startPinyin;

    @JsonFormat(pattern = "HH:mm:ss", timezone = "GMT+8")
    @NotNull(message = "【出发时间】不能为空")
    private Date startTime;

    @NotNull(message = "【出发站序】不能为空")
    private Integer startIndex;

    @NotBlank(message = "【到达站】不能为空")
    private String end;

    @NotBlank(message = "【到达站拼音】不能为空")
    private String endPinyin;

    @JsonFormat(pattern = "HH:mm:ss", timezone = "GMT+8")
    @NotNull(message = "【到站时间】不能为空")
    private Date endTime;

    @NotNull(message = "【到站站序】不能为空")
    private Integer endIndex;

    @NotNull(message = "【一等座余票】不能为空")
    private Integer ydz;

    @NotNull(message = "【一等座票价】不能为空")
    private BigDecimal ydzPrice;

    @NotNull(message = "【二等座余票】不能为空")
    private Integer edz;

    @NotNull(message = "【二等座票价】不能为空")
    private BigDecimal edzPrice;

    @NotNull(message = "【软卧余票】不能为空")
    private Integer rw;

    @NotNull(message = "【软卧票价】不能为空")
    private BigDecimal rwPrice;

    @NotNull(message = "【硬卧余票】不能为空")
    private Integer yw;

    @NotNull(message = "【硬卧票价】不能为空")
    private BigDecimal ywPrice;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getTrainCode() {
        return trainCode;
    }

    public void setTrainCode(String trainCode) {
        this.trainCode = trainCode;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getStartPinyin() {
        return startPinyin;
    }

    public void setStartPinyin(String startPinyin) {
        this.startPinyin = startPinyin;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Integer getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(Integer startIndex) {
        this.startIndex = startIndex;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public String getEndPinyin() {
        return endPinyin;
    }

    public void setEndPinyin(String endPinyin) {
        this.endPinyin = endPinyin;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public Integer getEndIndex() {
        return endIndex;
    }

    public void setEndIndex(Integer endIndex) {
        this.endIndex = endIndex;
    }

    public Integer getYdz() {
        return ydz;
    }

    public void setYdz(Integer ydz) {
        this.ydz = ydz;
    }

    public BigDecimal getYdzPrice() {
        return ydzPrice;
    }

    public void setYdzPrice(BigDecimal ydzPrice) {
        this.ydzPrice = ydzPrice;
    }

    public Integer getEdz() {
        return edz;
    }

    public void setEdz(Integer edz) {
        this.edz = edz;
    }

    public BigDecimal getEdzPrice() {
        return edzPrice;
    }

    public void setEdzPrice(BigDecimal edzPrice) {
        this.edzPrice = edzPrice;
    }

    public Integer getRw() {
        return rw;
    }

    public void setRw(Integer rw) {
        this.rw = rw;
    }

    public BigDecimal getRwPrice() {
        return rwPrice;
    }

    public void setRwPrice(BigDecimal rwPrice) {
        this.rwPrice = rwPrice;
    }

    public Integer getYw() {
        return yw;
    }

    public void setYw(Integer yw) {
        this.yw = yw;
    }

    public BigDecimal getYwPrice() {
        return ywPrice;
    }

    public void setYwPrice(BigDecimal ywPrice) {
        this.ywPrice = ywPrice;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}
