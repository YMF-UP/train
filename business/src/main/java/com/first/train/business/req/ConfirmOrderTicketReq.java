package com.first.train.business.req;

import jakarta.validation.constraints.NotBlank;

public class ConfirmOrderTicketReq {


    private Long passengerId;

    @NotBlank(message = "【乘客类型】不能为空")
    private String passengerType;

    @NotBlank(message = "【乘客姓名】不能为空")
    private String passengerName;

    @NotBlank(message = "【乘客身份证】不能为空")
    private String passengerIdCard;

    @NotBlank(message = "【座位类型】不能为空")
    private String seatTypeCode;

    private String seat;

    public Long getPassengerId() {
        return passengerId;
    }

    public void setPassengerId(Long passengerId) {
        this.passengerId = passengerId;
    }

    public String getPassengerType() {
        return passengerType;
    }

    public void setPassengerType(String passengerType) {
        this.passengerType = passengerType;
    }

    public String getPassengerName() {
        return passengerName;
    }

    public void setPassengerName(String passengerName) {
        this.passengerName = passengerName;
    }

    public String getPassengerIdCard() {
        return passengerIdCard;
    }

    public void setPassengerIdCard(String passengerIdCard) {
        this.passengerIdCard = passengerIdCard;
    }

    public String getSeatTypeCode() {
        return seatTypeCode;
    }

    public void setSeatTypeCode(String seatTypeCode) {
        this.seatTypeCode = seatTypeCode;
    }

    public String getSeat() {
        return seat;
    }

    public void setSeat(String seat) {
        this.seat = seat;
    }

    @Override
    public String toString() {
        return "ConfirmOrderTicketReq{" +
                "passengerId=" + passengerId +
                ", passengerType='" + passengerType + '\'' +
                ", passengerName='" + passengerName + '\'' +
                ", passengerIdCard='" + passengerIdCard + '\'' +
                ", seatTypeCode='" + seatTypeCode + '\'' +
                ", seat='" + seat + '\'' +
                '}';
    }
}
