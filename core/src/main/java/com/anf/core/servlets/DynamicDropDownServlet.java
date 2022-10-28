package com.anf.core.servlets;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.Servlet;

import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceMetadata;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.apache.sling.servlets.annotations.SlingServletResourceTypes;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.propertytypes.ServiceDescription;

import com.adobe.granite.ui.components.ds.DataSource;
import com.adobe.granite.ui.components.ds.SimpleDataSource;
import com.adobe.granite.ui.components.ds.ValueMapResource;
import com.day.crx.JcrConstants;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Venkatesh K A
 *
 */
@Component(
        service = Servlet.class)
@SlingServletResourceTypes(
        resourceTypes = "/bin/dropDownList",
        methods = HttpConstants.METHOD_GET)
@ServiceDescription("Populate drop down values based on JSON file in DAM")
@Slf4j
public class DynamicDropDownServlet extends SlingSafeMethodsServlet {

    private static final long serialVersionUID = -577432911477057127L;

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) {

        final String datasourcePath = getDataSourcePath(request);

        final ResourceResolver resolver = request.getResourceResolver();

        try (final InputStream jsonStream = readStreamFromFile(datasourcePath, resolver)) {

            if (jsonStream != null) {
                final Map<String, String> data = deserialize(jsonStream);
                final List<Resource> valueMapResourceList = dataToResources(data, resolver);

                // Feed resources into dataSource
                final DataSource dataSource = new SimpleDataSource(valueMapResourceList.iterator());
                request.setAttribute(DataSource.class.getName(), dataSource);
            }

        } catch (final IOException e) {
            log.error("Could not close JSON input stream", e);
        }

    }

    /**
     * Finds the correct data source in the JCR from the information in the
     * request.
     *
     * @param request
     *            the request to a component depending on a data source
     * @return the data source file path
     */
    private String getDataSourcePath(final SlingHttpServletRequest request) {

        // read data source node
        final Resource pathResource = request.getResource();
        final Resource datasourceResource = pathResource.getChild("datasource");

        // read jsonFilePath property from datasource node
        if (datasourceResource != null && datasourceResource.getValueMap()
                .containsKey("jsonFilePath")) {
            return datasourceResource.getValueMap()
                    .get("jsonFilePath", String.class);
        }

        return StringUtils.EMPTY;
    }

    /**
     * Reads a JSON file in the JCR and reads it as an InputStream.
     *
     * @param path
     *            the path of the JSON file in the JCR
     * @param resolver
     *            a resource resolver
     * @return an InputStream containing the contents of the JSON file
     */
    private InputStream readStreamFromFile(final String path, final ResourceResolver resolver) {

        try {
            Node contentNode = JcrUtils.getNodeIfExists(path + "/jcr:content/renditions/original/jcr:content",
                    resolver.adaptTo(Session.class));

            if (null != contentNode) {
                return contentNode.getProperty("jcr:data")
                        .getBinary()
                        .getStream();
            }

        } catch (RepositoryException e) {
            log.error("Couldn't read json from path {}", path, e);
        }

        return null;
    }

    /**
     * Deserializes JSON objects to a map.
     *
     * @param jsonStream
     *            a JSON stream containing a list of serialized value maps
     * @return a map of key value pairs
     */
    private Map<String, String> deserialize(final InputStream jsonStream) {
        Map<String, String> data = new HashMap<>();
        
        try {

            final ObjectReader reader = new ObjectMapper().readerFor(Map.class);
            data = reader.readValue(jsonStream);
            
        } catch (final IOException e) {
            log.error("Unexpected exception while retrieving json values from file", e);
        }
        return data;
    }

    /**
     * Converts a list of value maps to a list of resources.
     *
     * @param data
     *            a list of value maps
     * @param resolver
     *            a resource resolver
     * @return a list of resources
     */
    private List<Resource> dataToResources(final Map<String, String> data, final ResourceResolver resolver) {

        List<Resource> resourceList = new ArrayList<>();
        data.entrySet()
                .forEach(entry -> {
                    final ValueMap valueMap = new ValueMapDecorator(new HashMap<>());
                    valueMap.put("value", entry.getValue());
                    valueMap.put("text", entry.getKey());
                    resourceList.add(new ValueMapResource(resolver, new ResourceMetadata(),
                            JcrConstants.NT_UNSTRUCTURED, valueMap));
                });

        return resourceList;
    }
}
