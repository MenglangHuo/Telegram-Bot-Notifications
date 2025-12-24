package com.bronx.notification.dto.telegram;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class MediaRequest {
    // Media fields
    private String fileUrl;          // URL to file
    private byte[] fileData;         // Raw file bytes
    private String fileName;         // File name
    private String caption;          // Caption for media
    private String mimeType;//

    //metadata of audio
    private String title;
    private String performer;
    private Integer duration;

}
