package com.anf.core.models;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.apache.sling.api.resource.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.anf.core.model.NewsFeed;
import com.day.cq.wcm.api.Page;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

/**
 *  @author Venkatesh K A
 * JUnit test verifying the NewsFeedModel
 */

@ExtendWith(AemContextExtension.class)
public class NewsFeedModelTest {
    
    private NewsFeedModel newsFeedModel;
    
    private Page page;
    private Resource resource;

    @BeforeEach
    public void setup(AemContext context) throws Exception {

        // setup mock page and component resource
        page = context.create().page("/content/mypage");
        resource = context.create().resource(page, "newsfeed",
            "sling:resourceType", "anf-code-challenge/components/newsfeed",
            "newsFeedPath", "/var/commerce/products/anf-code-challenge");
        
        // load news feed data
        context.load().json("/newsfeed-data.json", "/var/commerce/products/anf-code-challenge");
        
        // create sling model
        newsFeedModel = resource.adaptTo(NewsFeedModel.class);

    }
    
    @Test
    void testGetFeedList_whenNoNewsDataNode() throws Exception {
        
        assertTrue(newsFeedModel.getNewsFeedPath().equals("/var/commerce/products/anf-code-challenge"));
        List<NewsFeed> feedList = newsFeedModel.getFeedList();
        assertNotNull(feedList);
        assertTrue(feedList.size() == 10);
        assertTrue(feedList.get(0).getTitle().equals("UFC 273: Five things we learned as Alexander Volkanovski dominates 'Korean Zombie'"));
        assertTrue(feedList.get(0).getAuthor().equals("Caroline Fox"));
    }
    
    @Test
    void testGetFeedList() throws Exception {
        
        assertTrue(newsFeedModel.getNewsFeedPath().equals("/var/commerce/products/anf-code-challenge"));
        List<NewsFeed> feedList = newsFeedModel.getFeedList();
        assertNotNull(feedList);
        assertTrue(feedList.size() == 10);
        assertTrue(feedList.get(0).getTitle().equals("UFC 273: Five things we learned as Alexander Volkanovski dominates 'Korean Zombie'"));
        assertTrue(feedList.get(0).getAuthor().equals("Caroline Fox"));
    }
}
