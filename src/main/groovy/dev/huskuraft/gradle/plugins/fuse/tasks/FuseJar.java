package dev.huskuraft.gradle.plugins.fuse.tasks;

import com.hypherionmc.jarmanager.JarManager;
import com.hypherionmc.jarrelocator.Relocation;
import dev.huskuraft.gradle.plugins.fuse.config.FuseSource;
import dev.huskuraft.gradle.plugins.fuse.merger.Merger;
import dev.huskuraft.gradle.plugins.fuse.merger.ServiceFileMerger;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.internal.file.copy.CopyAction;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.bundling.AbstractArchiveTask;
import org.gradle.jvm.tasks.Jar;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class FuseJar extends Jar implements FuseSpec {

    public static final String FUSE_JAR_DESCRIPTION = "Merge multiple jars into a single jar, for multi mod loader projects";
    public static final String FUSE_JAR_CLASSIFIER = "fuse";

    private final List<FuseSource> fuseSources = new ArrayList<>();

    private final List<String> duplicateRelocations = new ArrayList<>();

    private final List<Merger> mergers = new ArrayList<>();

    public FuseJar() {
        setDescription(FUSE_JAR_DESCRIPTION);
        getArchiveClassifier().set(FUSE_JAR_CLASSIFIER);
    }

    @Nested
    public List<FuseSource> getFuseConfigurations() {
        return fuseSources;
    }

    @Nested
    public List<String> getDuplicateRelocations() {
        return duplicateRelocations;
    }

    /**
     * Try to locate the correct task to run on the subproject
     *
     * @param project  - Sub project being processed
     * @param taskLike - The name of the task that will be run
     * @param fuseTask - The FuseJars task
     */
    private void resolveInputTasks(Project project, Object taskLike, FuseJar fuseTask) {
        if (taskLike == null) throw new NullPointerException("task name cannot be null");
        if (project == null) throw new NullPointerException("source project cannot be null");

        Task task = null;

        if (taskLike instanceof String string) {
            task = project.getTasks().getByName(string);
        }

        if (!(task instanceof AbstractArchiveTask archiveTask))
            throw new IllegalArgumentException("task must be an AbstractArchiveTask");

        fuseTask.dependsOn(archiveTask);
    }

    @Override
    protected @NotNull CopyAction createCopyAction() {

        return new MergeJarAction(
            getArchiveFile().get().getAsFile(),
            getTemporaryDir(),
            JarManager.getInstance(),
            getFuses(),
            getMergers(),
            getDuplicateRelocations());
    }

    private List<Fuse> getFuses() {

        if (getFuseConfigurations().isEmpty()) getLogger().warn("Fuse sources were not found.");
        if (getFuseConfigurations().size() == 1) getLogger().warn("Only one fuse source was found.");

        var fuses = new ArrayList<Fuse>();

        for (var entry : getFuseConfigurations()) {
            var inputFile = entry.getArtifact().getFile();
            if (inputFile == null) {
                getLogger().warn("Fuse source has no artifacts");
                continue;
            }
            var relocations = entry.getRelocations().entrySet().stream().map(e -> new Relocation(e.getKey(), e.getValue())).toList();
            fuses.add(new Fuse.Impl(inputFile, entry.getArtifact().getFile().getName(), relocations));
        }

        return fuses;

    }

    private List<Merger> getMergers() {
        return mergers;
    }

    @Override
    public FuseSpec includeJar(Action<FuseSource> closure) {
        var fuseConfiguration = new FuseSource();
        getProject().configure(List.of(fuseConfiguration), closure);

        if (fuseConfiguration.getArtifact() == null) {
            throw new IllegalStateException("includeJar has no artifacts");
        }
        fuseSources.add(fuseConfiguration);
        return this;
    }

    public void mergeServiceFiles() {
        try {
            merge(ServiceFileMerger.class, null);
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException |
                 IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public void merge(Class<? extends Merger> clazz, Action<Merger> action) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Merger merger = (Merger) clazz.getDeclaredConstructor().newInstance();
        merge(merger, action);
    }

    public void merge(Merger merger, Action<Merger> action) {
        if (action != null) action.execute(merger);
        this.mergers.add(merger);
    }

}
