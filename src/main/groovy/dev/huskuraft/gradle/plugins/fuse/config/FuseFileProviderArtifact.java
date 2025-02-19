package dev.huskuraft.gradle.plugins.fuse.config;

import org.gradle.api.file.RegularFile;
import org.gradle.api.provider.Provider;

import java.io.File;

class FuseFileProviderArtifact implements FuseArtifact {

    private final Provider<RegularFile> provider;

    public FuseFileProviderArtifact(Provider<RegularFile> provider) {
        this.provider = provider;
    }

    @Override
    public File getFile() {
        return provider.get().getAsFile();
    }

}
