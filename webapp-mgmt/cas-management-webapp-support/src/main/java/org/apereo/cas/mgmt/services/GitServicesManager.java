package org.apereo.cas.mgmt.services;

import javafx.util.Pair;
import org.apereo.cas.mgmt.GitUtil;
import org.apereo.cas.mgmt.services.web.beans.RegisteredServiceItem;
import org.apereo.cas.mgmt.services.web.factory.RepositoryFactory;
import org.apereo.cas.services.*;
import org.apereo.cas.services.util.DefaultRegisteredServiceJsonSerializer;
import org.apereo.cas.util.DigestUtils;
import org.eclipse.jgit.diff.DiffEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;

import java.io.File;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class GitServicesManager extends DomainServicesManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(GitServicesManager.class);

    private final boolean defaultOnly;

    private RepositoryFactory repositoryFactory;

    private Map<Integer, String> uncommitted;


    public GitServicesManager(final String repository, boolean defaultOnly, RepositoryFactory repositoryFactory) {
        this(new JsonServiceRegistryDao(Paths.get(repository), false, null), null, defaultOnly);
        this.repositoryFactory = repositoryFactory;
    }

    protected GitServicesManager(ServiceRegistryDao registryDao, ApplicationEventPublisher eventPublisher, boolean defaultOnly) {
        super(registryDao,eventPublisher);
        this.defaultOnly = defaultOnly;
        load();
    }

    @Override
    protected Collection<RegisteredService> getCandidateServicesToMatch(String serviceId) {
        if (defaultOnly) {
            return getAllServices();
        }
        return super.getCandidateServicesToMatch(serviceId);
    }

    @Override
    protected String extractDomain(String service) {
        if (defaultOnly) {
            return DEFAULT_DOMAIN_NAME;
        }
        return super.extractDomain(service);
    }

    public void loadFrom(final ServicesManager manager) {
        manager.getAllServices().stream().forEach(svc -> save(svc));
    }

    public List<RegisteredServiceItem> getServiceItemsForDomain(String domain) throws Exception {
        final GitUtil git = repositoryFactory.masterRepository();
        if (git.isNull()) {
            return Collections.EMPTY_LIST;
        }
        this.uncommitted = git.scanWorkingDiffs().stream()
                .map(d -> createChange(d,git))
                .collect(Collectors.toMap(Pair::getKey, Pair::getValue));
        final List<RegisteredServiceItem> serviceItems = new ArrayList<>();
        final List<RegisteredService> services = new ArrayList<>(getServicesForDomain(domain));
        serviceItems.addAll(services.stream().map(this::createServiceItem).collect(Collectors.toList()));
        serviceItems.addAll(checkForDeleted(git));
        return serviceItems;

    }

    private List<RegisteredServiceItem> checkForDeleted(final GitUtil git) {
        try {
            return git.checkForDeletes()
                    .map(d -> getService(git, d))
                    .collect(Collectors.toList());
        }catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    private RegisteredServiceItem getService(final GitUtil git, final DiffEntry d) {
        try {
            String json = git.readObject(d.getOldId().toObjectId());
            DefaultRegisteredServiceJsonSerializer serializer = new DefaultRegisteredServiceJsonSerializer();
            return createServiceItem(serializer.from(json));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public RegisteredServiceItem createServiceItem(final RegisteredService service) {
        final RegisteredServiceItem serviceItem = new RegisteredServiceItem();
        serviceItem.setAssignedId(String.valueOf(service.getId()));
        serviceItem.setEvalOrder(service.getEvaluationOrder());
        serviceItem.setName(service.getName());
        serviceItem.setServiceId(service.getServiceId());
        serviceItem.setDescription(DigestUtils.abbreviate(service.getDescription()));
        if (uncommitted.containsKey(service.getId())) {
            serviceItem.setStatus(uncommitted.get(service.getId()));
        }
        return serviceItem;
    }

    private Pair<Integer, String> createChange(final DiffEntry entry, final GitUtil git) {
        try {
            DefaultRegisteredServiceJsonSerializer ser = new DefaultRegisteredServiceJsonSerializer();
            RegisteredService svc;
            if (entry.getChangeType() == DiffEntry.ChangeType.DELETE) {
                svc = ser.from(git.readObject(entry.getOldId().toObjectId()));
            } else {
                svc = ser.from(new File(git.repoPath() + "/" + entry.getNewPath()));
            }
            return new Pair(svc.getId(),entry.getChangeType().toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
