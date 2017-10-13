package org.apereo.cas.mgmt.services.web.factory;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.mgmt.GitUtil;
import org.apereo.cas.mgmt.authentication.CasUserProfile;
import org.apereo.cas.mgmt.services.GitServicesManager;
import org.apereo.cas.services.ServicesManager;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
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

    CasConfigurationProperties casProperties;

    public ManagerFactory(final ServicesManager servicesManager,
                          final CasConfigurationProperties casProperties,
                          final RepositoryFactory repositoryFactory) {
        this.repositoryFactory = repositoryFactory;
        this.casProperties = casProperties;
        Path servicesRepo = Paths.get(casProperties.getMgmt().getServicesRepo());
        if (!Files.exists(servicesRepo)) {
            try {
                Git.init().setDirectory(servicesRepo.toFile()).call();
            } catch(Exception e) {
                e.printStackTrace();
                return;
            }
            final GitServicesManager manager = new GitServicesManager(casProperties.getMgmt().getServicesRepo());
            manager.loadFrom(servicesManager);
            try {
                GitUtil git = repositoryFactory.masterRepository();
                git.addWorkingChanges();
                git.getGit().commit().setAll(true).setMessage("Initial commit").call();
                git.setPublished();
                git.close();
            }catch (Exception e) {
                e.printStackTrace();
            }
            /*
            try {
                Git.cloneRepository().setURI(casProperties.getMgmt().getServicesRepo())
                        .setDirectory(new File(casProperties.getMgmt().getPublishedRepo()))
                        .call();
            } catch (Exception e) {
                e.printStackTrace();
            }
            */
        }
    }

    public GitServicesManager manager(final HttpServletRequest request, final CasUserProfile user) throws Exception {
        if(user.isAdministrator()) {
            return new GitServicesManager(casProperties.getMgmt().getServicesRepo());
        }

        Path path = Paths.get(casProperties.getMgmt().getUserReposDir() + "/" + user.getId());
        if (!Files.exists(path)) {
            repositoryFactory.clone(path.toString());
        } else {
            repositoryFactory.userRepository(user.getId()).rebase();
        }

        GitServicesManager manager = (GitServicesManager)request.getSession().getAttribute("servicesManager");
        if (manager != null) {
            manager.load();
        } else {
            manager = new GitServicesManager(path.getFileName().toString());
            request.getSession().setAttribute("servicesManager",manager);
        }

        return manager;
    }
}
