package com.anf.core.models;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import com.anf.core.model.NewsFeed;

@Model(
        adaptables = Resource.class,
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class NewsFeedModel {

    @ValueMapValue
    private String newsFeedPath;

    @SlingObject
    private ResourceResolver resourceResolver;

    private List<NewsFeed> feedList;

    @PostConstruct
    protected void init() {

        feedList = new ArrayList<>();

        String currentDate = getCurrentDate();

        Resource feedResource = resourceResolver.getResource(newsFeedPath + "/newsData");

        Iterator<Resource> children = feedResource.listChildren();
        while (children.hasNext()) {
            final Resource child = children.next();
            ValueMap valueMap = child.getValueMap();
            NewsFeed feed = NewsFeed.builder()
                    .title(valueMap.get("title", StringUtils.EMPTY))
                    .url(valueMap.get("url", StringUtils.EMPTY))
                    .urlImage(valueMap.get("urlImage", StringUtils.EMPTY))
                    .author(valueMap.get("author", StringUtils.EMPTY))
                    .content(valueMap.get("content", StringUtils.EMPTY))
                    .description(valueMap.get("description", StringUtils.EMPTY))
                    .date(currentDate)
                    .build();
            feedList.add(feed);
        }

    }

    private String getCurrentDate() {

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy");
        return simpleDateFormat.format(new Date());
    }

    public List<NewsFeed> getFeedList() {
        return feedList;
    }

    public String getNewsFeedPath() {
        return newsFeedPath;
    }
}
