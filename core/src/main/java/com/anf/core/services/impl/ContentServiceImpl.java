package com.anf.core.services.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

import com.anf.core.error.AppException;
import com.anf.core.error.ErrorCode;
import com.anf.core.model.AgeConfig;
import com.anf.core.model.User;
import com.anf.core.services.ContentService;
import com.day.cq.search.PredicateGroup;
import com.day.cq.search.Query;
import com.day.cq.search.QueryBuilder;
import com.day.cq.search.result.SearchResult;

import lombok.extern.log4j.Log4j;

@Component(
        immediate = true,
        service = ContentService.class)
@Designate(
        ocd = ContentServiceImpl.Config.class)
@Log4j
public class ContentServiceImpl implements ContentService {
    
    // OSGI configurations
    /**
     * @author Venkatesh K A
     *
     */
    @ObjectClassDefinition(
            name = "ANF Code Challenge - Content Service Configuration",
            description = "OSGI service providing configuration options for Content Service")
    @interface Config {
        
        @AttributeDefinition(
                name = "Age Config Path",
                description = "Node path that contains age configuration for validation")
        String age_config_path() default "/etc/age";
        
        @AttributeDefinition(
                name = "Users Root Path",
                description = "Root path under which user details should be saved")
        String users_root_path() default "/var/anf-code-challenge";
    }

    @Reference
    private ResourceResolverFactory resolverFactory;

    private String ageConfigPath;
    private String usersRootPath;

    @Activate
    protected void activate(Config config) {
        this.ageConfigPath = config.age_config_path();
        this.usersRootPath = config.users_root_path();
    }

    /*
     * (non-Javadoc)
     * @see com.anf.core.services.ContentService#commitUserDetails(com.anf.core.model.User)
     */
    @Override
    public void commitUserDetails(User user) throws RepositoryException, LoginException {
        // Add your logic. Modify method signature as per need.

        // validate inputs
        validate(user);

        // commit user
        commit(user);
    }

    private void validate(User user) {

        // Considering all user fields as mandatory and throwing an error otherwise
        if (StringUtils.isEmpty(user.getFirstName()) || StringUtils.isEmpty(user.getLastName())
                || StringUtils.isEmpty(user.getCountry())) {
            throw new AppException(ErrorCode.USER_FIELDS_MISSING);
        }

        AgeConfig ageConfig = null;
        try {
            
            // Read age configuration for validation
            ageConfig = getAgeConfig();

            int age = user.getAge();

            // throw an error if age is not within configured range
            if (age < Integer.valueOf(ageConfig.getMinAge()) || age > Integer.valueOf(ageConfig.getMaxAge())) {
                throw new AppException(ErrorCode.USER_AGE_INELIGIBLE);
            }
            
        } catch (RepositoryException | LoginException e) {
            // in case unable to access the age configuration node
            log.error("Unable to read age configurations for validation", e);
            throw new AppException(ErrorCode.AGE_CONFIG_ISSUE);
        }
    }

    private AgeConfig getAgeConfig() throws RepositoryException, LoginException {

        // Access the age config node and read min and max age values configured 
        Node ageConfigNode = resolverFactory.getServiceResourceResolver(null)
                .getResource(ageConfigPath)
                .adaptTo(Node.class);

        return AgeConfig.builder()
                .minAge(ageConfigNode.getProperty("minAge")
                        .getString())
                .maxAge(ageConfigNode.getProperty("maxAge")
                        .getString())
                .build();

    }

    private void commit(User user) throws RepositoryException, LoginException {

        // Get session based on configured service user for this bundle
        Session session = resolverFactory.getServiceResourceResolver(null)
                .adaptTo(Session.class);

        // Under user's root node, sub nodes are created based on first character
        // of first name to organize user nodes better instead of creating all user nodes
        // under single folder.
        final String firstName = user.getFirstName()
                .toLowerCase();
        final char firstChar = firstName.charAt(0);
        Node usersRootNode = JcrUtils.getOrCreateByPath(usersRootPath + "/" + firstChar, "sling:Folder",
                "sling:OrderedFolder", session, true);

        // Create user node using first name for easy readability with node repository.
        // Numbered suffix will be automatically added when same first name is used e.g., john, john0, john1 etc.
        Node userNode = JcrUtils.getOrCreateUniqueByPath(usersRootNode, firstName, "nt:unstructured");

        // save user information as properties
        userNode.setProperty("firstName", user.getFirstName());
        userNode.setProperty("lastName", user.getLastName());
        userNode.setProperty("country", user.getCountry());
        userNode.setProperty("age", user.getAge());

        // save changes
        session.save();
    }

    /*
     * (non-Javadoc)
     * @see com.anf.core.services.ContentService#getExercise3PagesUsingQueryBuilder(org.apache.sling.api.SlingHttpServletRequest)
     */
    @Override
    public List<String> getExercise3PagesUsingQueryBuilder(final SlingHttpServletRequest request)
            throws RepositoryException {

        // create query description as hash map (simplest way, same as form
        // post)
        Map<String, String> map = new HashMap<>();
        map.put("path", "/content");
        map.put("type", "cq:Page");
        map.put("1_property", "jcr:content/anfCodeChallenge");
        map.put("1_property.value", "true");
        map.put("p.limit", "10");

        final QueryBuilder queryBuilder = request.getResourceResolver()
                .adaptTo(QueryBuilder.class);
        final Session session = request.getResourceResolver()
                .adaptTo(Session.class);

        // create query and execute
        final Query query = queryBuilder.createQuery(PredicateGroup.create(map), session);
        final SearchResult result = query.getResult();

        Node resultNode = null;
        List<String> pagePaths = new ArrayList<>();
        
        // iterate through results and consolidate page paths
        if (null != result) {
            final Iterator<Node> nodeItr = result.getNodes();
            while (nodeItr.hasNext()) {
                resultNode = nodeItr.next();
                pagePaths.add(resultNode.getPath());
            }
        }

        return pagePaths;
    }
    
    /*
     * (non-Javadoc)
     * @see com.anf.core.services.ContentService#getExercise3PagesUsingSQL2(org.apache.sling.api.SlingHttpServletRequest)
     */
    @Override
    public List<String> getExercise3PagesUsingSQL2(final SlingHttpServletRequest request) {

        final String queryString = "SELECT parent.* FROM [cq:Page] AS parent \n"
                + "INNER JOIN [nt:base] AS child ON ISCHILDNODE(child,parent) \n"
                + "WHERE ISDESCENDANTNODE(parent, '/content/anf-code-challenge/us/en') AND child.[anfCodeChallenge] = 'true'";

        ResourceResolver resolver = request.getResourceResolver();
        Iterator<Resource> result = resolver.findResources(queryString, javax.jcr.query.Query.JCR_SQL2);

        // Alternatively this can be done using QueryManager API as well like shown below:
        // Workspace workspace = session.getWorkspace();
        // QueryManager queryManager = workspace.getQueryManager();
        // Query query = queryManager.createQuery("{JCR-SQL2 query}", Query.JCR_SQL2);
        // QueryResult result = query.execute();

        List<String> pagePaths = new ArrayList<>();
        result.forEachRemaining(resource -> pagePaths.add(resource.getPath()));

        return pagePaths;
    }

}
