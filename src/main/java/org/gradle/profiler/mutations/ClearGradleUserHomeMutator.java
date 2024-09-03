package org.gradle.profiler.mutations;

import org.gradle.profiler.BuildMutator;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;

public class ClearGradleUserHomeMutator extends AbstractCleanupMutator {
    private final File gradleUserHome;

    public ClearGradleUserHomeMutator(File gradleUserHome, CleanupSchedule schedule) {
        super(schedule);
        this.gradleUserHome = gradleUserHome;
    }

    @Override
    protected void cleanup() {
        System.out.println(String.format("> Cleaning Gradle user home: %s", gradleUserHome.getAbsolutePath()));
        if (!gradleUserHome.exists()) {
            throw new IllegalArgumentException(String.format(
                "Cannot delete Gradle user home directory (%s) since it does not exist",
                gradleUserHome
            ));
        }
        try {
            Files.list(gradleUserHome.toPath())
                // Don't delete the wrapper dir, since this is where the Gradle distribution we are going to run is located
                .filter(path -> !path.getFileName().toString().equals("wrapper"))
                .forEach(path -> delete(path.toFile()));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static class Configurator extends AbstractCleanupMutator.Configurator {
        @Override
        protected BuildMutator newInstance(BuildMutatorConfiguratorSpec spec, String key, CleanupSchedule schedule) {
            return new ClearGradleUserHomeMutator(spec.getGradleUserHome(), schedule);
        }
    }
}
