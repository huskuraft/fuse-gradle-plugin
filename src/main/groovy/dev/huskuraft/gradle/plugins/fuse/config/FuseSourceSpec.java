package dev.huskuraft.gradle.plugins.fuse.config;

import org.gradle.api.Project;
import org.gradle.api.file.RegularFile;
import org.gradle.api.provider.Provider;

public interface FuseSourceSpec {

    @Deprecated(forRemoval = true)
    default void source(Project project) {}

    @Deprecated(forRemoval = true)
    default void task(String task) {}

    void file(Provider<RegularFile> provider);

    /**
     * Add a package to relocate, instead of duplicating
     *
     * @param from - The original name of the package. For example: com.google.gson
     * @param to   - The new name of the package. For example: forge.com.google.gson
     */
    void relocate(String from, String to);
}
