package com.pratham.mailsender.model;

import java.io.Serial;
import java.io.Serializable;
import lombok.Data;

@Data
public class EmailConfig implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private String clientId;
    private String clientSecret;
    private String tenantId;
    private String fromEmail;
}
