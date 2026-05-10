package com.social.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.List;

@Data
public class ShareDTO {
    @NotBlank(message = "内容不能为空")
    private String content;

    private String visibility = "PUBLIC";

    private List<String> images;
}