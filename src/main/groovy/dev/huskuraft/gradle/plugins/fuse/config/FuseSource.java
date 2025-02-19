package dev.huskuraft.gradle.plugins.fuse.config;

import org.gradle.api.Project;
import org.gradle.api.file.RegularFile;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FuseSource implements FuseSourceSpec {

    Map<String, String> relocations = new LinkedHashMap<>();

    @Internal
    FuseArtifact artifact;

    @Override
    public void file(Provider<RegularFile> provider) {
        this.artifact = new FuseFileProviderArtifact(provider);
    }

    public void relocate(String from, String to) {
        this.relocations.put(from, to);
    }

    @Nested
    public Map<String, String> getRelocations() {
        return relocations;
    }

    public FuseArtifact getArtifact() {
        return artifact;
    }
}
