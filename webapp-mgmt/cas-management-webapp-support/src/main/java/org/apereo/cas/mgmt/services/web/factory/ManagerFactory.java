package org.apereo.cas.mgmt.services.web.factory;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.mgmt.authentication.CasUserProfile;
import org.apereo.cas.services.DomainServicesManager;
import org.apereo.cas.services.JsonServiceRegistryDao;
import org.apereo.cas.services.ServicesManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by tsschmi on 5/2/17.
 */
@Component
public class ManagerFactory {

    @Autowired
    RepositoryFactory repositoryFactory;

    ServicesManager servicesManager;

    @Autowired
    ApplicationEventPublisher eventPublisher;

    @Autowired
    CasConfigurationProperties casProperties;

    public ManagerFactory(final ServicesManager servicesManager) {
        this.servicesManager = servicesManager;
    }

    public ServicesManager manager(final HttpServletRequest request, final CasUserProfile user, boolean clone) throws Exception {
        if(user.isAdministrator()) {
            return this.servicesManager;
        }

        Path path = Paths.get(casProperties.getMgmt().getUserReposDir() + "/" + user.getId());
        if (clone && !Files.exists(path)) {
            repositoryFactory.clone(path.toString());
        } else if (Files.exists(path)) {
            repositoryFactory.userRepository(user.getId()).rebase();
        } else {
            return this.servicesManager;
        }

        DomainServicesManager manager = (DomainServicesManager)request.getSession().getAttribute("servicesManager");
        if (manager != null) {
            manager.load();
            return manager;
        } else {
            JsonServiceRegistryDao dao = new JsonServiceRegistryDao(path,false,eventPublisher);
            manager = new DomainServicesManager(dao, eventPublisher);
            manager.load();
            request.getSession().setAttribute("servicesManager",manager);
            return manager;
        }
    }
}
