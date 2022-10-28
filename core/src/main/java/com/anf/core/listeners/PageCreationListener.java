package com.anf.core.listeners;

import java.util.Iterator;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.wcm.api.PageEvent;
import com.day.cq.wcm.api.PageModification;
import com.day.cq.wcm.api.PageModification.ModificationType;

/**
 * @author Venkatesh K A
 *
 */
@Component(
        service = EventHandler.class,
        immediate = true,
        configurationPolicy = ConfigurationPolicy.OPTIONAL,
        property = { EventConstants.EVENT_TOPIC + "=" + PageEvent.EVENT_TOPIC })
@Designate(
        ocd = PageCreationListener.Config.class)
public class PageCreationListener implements EventHandler {

    // OSGI configurations
    @ObjectClassDefinition(
            name = "ANF Code Challenge - Page Create Listener",
            description = "OSGI service providing configuration options")
    @interface Config {

        @AttributeDefinition(
                name = "Page Filter Path")
        String filter() default "/content/anf-code-challenge/us/en";

    }

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private String filter;

    @Reference
    private ResourceResolverFactory resolverFactory;

    @Activate
    protected void activate(Config config) {
        this.filter = config.filter();
    }

    public void handleEvent(final Event event) {

        Iterator<PageModification> pageIter = PageEvent.fromEvent(event)
                .getModifications();

        while (pageIter.hasNext()) {
            PageModification modification = pageIter.next();

            logger.debug("Type: {}, Page: {}", modification.getType(), modification.getPath());

            if (modification.getType()
                    .equals(ModificationType.CREATED)
                    && modification.getPath()
                            .startsWith(filter)) {

                try {

                    // Get session based on configured service user for this
                    // bundle
                    Session session = resolverFactory.getServiceResourceResolver(null)
                            .adaptTo(Session.class);

                    Node contentNode = session.getNode(modification.getPath() + "/jcr:content");

                    contentNode.setProperty("pageCreated", true);

                    session.save();

                    logger.debug("SUCCESS");

                } catch (RepositoryException | LoginException e) {
                    logger.error("Error processing page creation event", e);
                }

            }
        }

    }
}
