package org.apereo.cas.mgmt.services;

import javafx.util.Pair;
import org.apereo.cas.mgmt.GitUtil;
import org.apereo.cas.mgmt.services.web.beans.RegisteredServiceItem;
import org.apereo.cas.mgmt.services.web.factory.RepositoryFactory;
import org.apereo.cas.services.DomainServicesManager;
import org.apereo.cas.services.JsonServiceRegistryDao;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServiceRegistryDao;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.util.DefaultRegisteredServiceJsonSerializer;
import org.apereo.cas.util.DigestUtils;
import org.eclipse.jgit.diff.DiffEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Class used to manage services in Git repository.
 *
 * @author Travis Schmidt
 * @since 5.2.0
 */
public class GitServicesManager extends DomainServicesManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(GitServicesManager.class);

    private final boolean defaultOnly;

    private GitUtil git;

    private Map<Integer, String> uncommitted;


    public GitServicesManager(final GitUtil git, final boolean defaultOnly) {
        this(new JsonServiceRegistryDao(git.getGit().getRepository().getDirectory().toPath(),
                                        false,
                                        null),
                                        null, defaultOnly);
        this.git = git;
    }

    /**
     * Constructor to create instance.
     *
     * @param repository - String path to the repository
     * @param defaultOnly - boolean if the default domain is only used.
     * @param repositoryFactory - RepositoryFactory

    public GitServicesManager(final String repository,
                              final boolean defaultOnly,
                              final RepositoryFactory repositoryFactory) {
        this(new JsonServiceRegistryDao(Paths.get(repository),
                false,
                null),
                null,
                defaultOnly);
        this.repositoryFactory = repositoryFactory;
    }
    */

    /**
     * Protected constructor matching extended class for creating an instance.
     *
     * @param registryDao - JSONRegistryDAO for the git repo
     * @param eventPublisher - ApplicationPublisher
     * @param defaultOnly - boolean if DefaultServicesManager is used.
     */
    protected GitServicesManager(final ServiceRegistryDao registryDao,
                                 final ApplicationEventPublisher eventPublisher,
                                 final boolean defaultOnly) {
        super(registryDao, eventPublisher);
        this.defaultOnly = defaultOnly;
        load();
    }

    @Override
    protected Collection<RegisteredService> getCandidateServicesToMatch(final String serviceId) {
        if (defaultOnly) {
            return getAllServices();
        }
        return super.getCandidateServicesToMatch(serviceId);
    }

    @Override
    protected String extractDomain(final String service) {
        if (defaultOnly) {
            return DEFAULT_DOMAIN_NAME;
        }
        return super.extractDomain(service);
    }

    /**
     * Loads Services form an existing ServiceManger to initialize a new repository.
     *
     * @param manager - ServicesManger to load from
     */
    public void loadFrom(final ServicesManager manager) {
        manager.getAllServices().stream().forEach(svc -> save(svc));
    }

    /**
     * Returns a list of RegisteredServiceItems for the passed domain.
     *
     * @param domain - Name of the domain to lookup.
     * @return - List of RegisteredServiceItems
     * @throws Exception -failed
     */
    public List<RegisteredServiceItem> getServiceItemsForDomain(final String domain) throws Exception {
        if (git.isNull()) {
            return new ArrayList<>();
        }
        this.uncommitted = git.scanWorkingDiffs().stream()
                .map(d -> createChange(d))
                .collect(Collectors.toMap(Pair::getKey, Pair::getValue));
        final List<RegisteredServiceItem> serviceItems = new ArrayList<>();
        final List<RegisteredService> services = new ArrayList<>(getServicesForDomain(domain));
        serviceItems.addAll(services.stream().map(this::createServiceItem).collect(Collectors.toList()));
        //serviceItems.addAll(checkForDeleted(git));
        return serviceItems;

    }

    private List<RegisteredServiceItem> checkForDeleted() {
        try {
            return git.checkForDeletes()
                    .map(d -> getService(d))
                    .collect(Collectors.toList());
        }catch (final Exception e) {
        }
        return new ArrayList<>();
    }

    private RegisteredServiceItem getService(final DiffEntry d) {
        try {
            final String json = git.readObject(d.getOldId().toObjectId());
            final DefaultRegisteredServiceJsonSerializer serializer = new DefaultRegisteredServiceJsonSerializer();
            return createServiceItem(serializer.from(json));
        } catch (final Exception e) {
            return null;
        }
    }

    /**
     * Method creates a RegisteredServiceItem from the passed Service to send to the client.
     *
     * @param service - RegisteredService to create the item.
     * @return - RegisteredServiceItem
     */
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

    private Pair<Integer, String> createChange(final DiffEntry entry) {
        try {
            final DefaultRegisteredServiceJsonSerializer ser = new DefaultRegisteredServiceJsonSerializer();
            final RegisteredService svc;
            if (entry.getChangeType() == DiffEntry.ChangeType.DELETE) {
                svc = ser.from(git.readObject(entry.getOldId().toObjectId()));
            } else {
                svc = ser.from(new File(git.repoPath() + "/" + entry.getNewPath()));
            }
            return new Pair(svc.getId(), entry.getChangeType().toString());
        } catch (final Exception e) {
        }
        return null;
    }
}
