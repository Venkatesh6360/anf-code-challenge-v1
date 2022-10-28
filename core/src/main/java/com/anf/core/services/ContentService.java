package com.anf.core.services;

import java.util.List;

import javax.jcr.RepositoryException;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.LoginException;

import com.anf.core.model.User;

public interface ContentService {

    /**
     * Service commits user information in the repository under pre-defined
     * configured location.
     * 
     * @param user
     *            User object
     * @throws RepositoryException
     *             Exception thrown when unable to access underlying repository
     * @throws LoginException
     *             Exception thrown when unable to get resource resolver or
     *             session
     */

    void commitUserDetails(User user) throws RepositoryException, LoginException;

    /**
     * Service demonstrates querying pages using Query Builder API.
     * 
     * @param request
     *            Sling request
     * @return Returns page paths from the query result
     * @throws RepositoryException
     *             throws repository exception when unable to read the result
     *             path
     */
    List<String> getExercise3PagesUsingQueryBuilder(final SlingHttpServletRequest request) throws RepositoryException;

    /**
     * Service demonstrates querying pages using JCR-SQL2 queries.
     * 
     * @param request
     *            Sling request
     * @return Returns page paths from the query result
     */
    List<String> getExercise3PagesUsingSQL2(final SlingHttpServletRequest request);
}
