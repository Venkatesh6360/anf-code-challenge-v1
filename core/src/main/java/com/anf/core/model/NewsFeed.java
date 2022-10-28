package com.anf.core.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NewsFeed {

    private String title;
    private String url;
    private String urlImage;
    private String author;
    private String content;
    private String description;
    private String date;
}
