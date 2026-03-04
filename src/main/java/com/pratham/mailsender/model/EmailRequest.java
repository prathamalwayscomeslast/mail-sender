package com.pratham.mailsender.model;

import java.io.Serial;
import java.io.Serializable;
import lombok.Data;

@Data
public class EmailRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String to;
    private String cc;
    private String bcc;
    private String replyTo;
    private String subject;
    private String body;
    private boolean isHtml = true;
}
