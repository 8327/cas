package org.apereo.cas.mgmt.services.web.factory;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.mgmt.GitUtil;
import org.apereo.cas.mgmt.authentication.CasUserProfile;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by tsschmi on 3/2/17.
 */
@Component("repositoryFactory")
public class RepositoryFactory {

    @Autowired
    CasConfigurationProperties casProperties;

    public RepositoryFactory() {
    }

    /**
     * Method loads the git repository based on the user and their permissions.
     *
     * @param user
     * @return
     * @throws Exception
     */
    public GitUtil getGit(CasUserProfile user) throws Exception {
        return getGit(user,false);
    }

    public GitUtil getGit(CasUserProfile user, boolean returnMaster) throws Exception {
        if (user.isAdministrator())  {
            return masterRepository();
        }
        Path path = Paths.get(casProperties.getMgmt().getUserReposDir()+"/"+user.getId());
        if (Files.exists(path)) {
            return userRepository(user.getId());
        }
        if (returnMaster) {
            return masterRepository();
        }
        return new GitUtil(null);
    }

    public GitUtil masterRepository() throws Exception {
        return new GitUtil(new Git(new FileRepositoryBuilder()
                .setGitDir(new File(casProperties.getMgmt().getServicesRepo()+ "/.git"))
                .setMustExist(true)
                .readEnvironment()
                .findGitDir()
                .build()));
    }

    public GitUtil publishedRepository() throws Exception {
        return new GitUtil(new Git(new FileRepositoryBuilder()
                .setGitDir(new File(casProperties.getMgmt().getPublishedRepo()+"/.git"))
                .setMustExist(true)
                .readEnvironment()
                .findGitDir()
                .build()));
    }

    public GitUtil userRepository(String user) throws Exception {
        String path = casProperties.getMgmt().getUserReposDir()+"/"+user+"/.git";
        return new GitUtil(new Git(new FileRepositoryBuilder()
                .setGitDir(new File(path))
                .setMustExist(true)
                .readEnvironment()
                .findGitDir()
                .build()));
    }

    public void clone(String clone) {
        try {
            Git git = Git.cloneRepository()
                    .setURI(casProperties.getMgmt().getServicesRepo()+"/.git")
                    .setDirectory(new File(clone))
                    .call();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
