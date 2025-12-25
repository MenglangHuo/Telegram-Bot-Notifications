package com.bronx.notification.dto.telegramSender;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class MediaRequest {
    //metadata of file
    private String title;
    private String performer;
    private String description;
    private Integer duration;

}
