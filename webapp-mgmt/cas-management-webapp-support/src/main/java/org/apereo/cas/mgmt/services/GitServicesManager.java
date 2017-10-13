package org.apereo.cas.mgmt.services;

import org.apereo.cas.services.AbstractServicesManager;
import org.apereo.cas.services.JsonServiceRegistryDao;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServiceRegistryDao;
import org.apereo.cas.services.ServicesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;

public class GitServicesManager extends AbstractServicesManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(GitServicesManager.class);


    public GitServicesManager(final String repository) {
        this(new JsonServiceRegistryDao(Paths.get(repository), false, null), null);
    }

    protected GitServicesManager(ServiceRegistryDao registryDao, ApplicationEventPublisher eventPublisher) {
        super(registryDao,eventPublisher);
        load();
    }

    @Override
    protected Collection<RegisteredService> getCandidateServicesToMatch(String serviceId) {
        return getAllServices();
    }

    public void loadFrom(final ServicesManager manager) {
        manager.getAllServices().stream().forEach(svc -> save(svc));
    }
}
