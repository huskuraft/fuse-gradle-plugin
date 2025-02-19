package dev.huskuraft.gradle.plugins.fuse.tasks;

import dev.huskuraft.gradle.plugins.fuse.config.FuseSource;
import org.gradle.api.Action;
import org.gradle.api.file.CopySpec;
import org.gradle.api.file.RegularFile;
import org.gradle.api.provider.Provider;

public interface FuseSpec extends CopySpec {

    FuseSpec includeJar(Action<FuseSource> closure);

    default FuseSpec includeJar(Provider<RegularFile> provider) {
        includeJar(fuseSource -> fuseSource.file(provider));
        return this;
    }

}
