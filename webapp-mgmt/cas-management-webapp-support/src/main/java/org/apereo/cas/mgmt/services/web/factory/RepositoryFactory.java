package org.apereo.cas.mgmt.services.web.factory;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.mgmt.GitUtil;
import org.apereo.cas.mgmt.authentication.CasUserProfile;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;

/**
 * Factory class to create repository objects.
 *
 * @author Travis Schmidt
 * @since 5.2.0
 */
public class RepositoryFactory {

    @Autowired
    private CasConfigurationProperties casProperties;

    public RepositoryFactory() {
    }

    /**
     * Method loads the git repository based on the user and their permissions.
     *
     * @param user - CasUserProfile of logged in user
     * @return - GitUtil wrapping the user's repository
     * @throws Exception -failed
     */
    public GitUtil getGit(final CasUserProfile user) throws Exception {
        return masterRepository();
        //return getGit(user,false);
    }

    /*
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
    */

    /**
     * Method returns a GitUtil wrapping the master repository.
     *
     * @return - GitUtil
     * @throws Exception - failed
     */
    public GitUtil masterRepository() throws Exception {
        return new GitUtil(new Git(new FileRepositoryBuilder()
                .setGitDir(new File(casProperties.getMgmt().getServicesRepo() + "/.git"))
                .setMustExist(true)
                .readEnvironment()
                .findGitDir()
                .build()));
    }

    /*
    public GitUtil userRepository(String user) throws Exception {
        String path = casProperties.getMgmt().getUserReposDir()+"/"+user+"/.git";
        return new GitUtil(new Git(new FileRepositoryBuilder()
                .setGitDir(new File(path))
                .setMustExist(true)
                .readEnvironment()
                .findGitDir()
                .build()));
    }
    */

    /**
     * Clones the master repository into the passed in directory.
     *
     * @param clone - String representing dir to create the clone
     */
    public void clone(final String clone) {
        try {
            Git.cloneRepository()
                    .setURI(casProperties.getMgmt().getServicesRepo() + "/.git")
                    .setDirectory(new File(clone))
                    .call();
        } catch (final Exception e) {
        }
    }
}
