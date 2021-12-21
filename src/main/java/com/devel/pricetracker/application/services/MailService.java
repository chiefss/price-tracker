package com.devel.pricetracker.application.services;

public interface MailService {

    public void sendAdmin(String subject, String body);
    public void sendMail(String subject, String body, String to);
}
