/*
 * CraftBook Copyright (C) me4502 <https://matthewmiller.dev/>
 * CraftBook Copyright (C) EngineHub and Contributors <https://enginehub.org/>
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not,
 * see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.craftbook;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.sk89q.craftbook.util.concurrent.EvenMoreExecutors;
import com.sk89q.craftbook.util.profile.cache.HashMapCache;
import com.sk89q.craftbook.util.profile.cache.ProfileCache;
import com.sk89q.craftbook.util.profile.cache.SQLiteCache;
import com.sk89q.craftbook.util.profile.resolver.ProfileService;
import com.sk89q.worldedit.util.io.ResourceLoader;
import com.sk89q.worldedit.util.task.SimpleSupervisor;
import com.sk89q.worldedit.util.task.Supervisor;
import com.sk89q.worldedit.util.translation.TranslationManager;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class CraftBook {

    public static final org.slf4j.Logger logger = LoggerFactory.getLogger(CraftBook.class);

    private static final CraftBook instance = new CraftBook();

    private CraftBookPlatform platform;
    private final ResourceLoader resourceLoader = new CraftBookResourceLoader();
    private final TranslationManager translationManager = new TranslationManager(resourceLoader);

    private final Supervisor supervisor = new SimpleSupervisor();
    private ProfileCache profileCache;
    private ProfileService profileService;
    private ListeningExecutorService executorService;

    public static CraftBook getInstance() {
        return instance;
    }

    public void setup() {
        executorService = MoreExecutors.listeningDecorator(EvenMoreExecutors.newBoundedCachedThreadPool(0, 1, 20,
                "CraftBook Task Executor - %s"));

        Path cacheDir = getPlatform().getConfigDir().resolve("cache");
        if (!Files.exists(cacheDir)) {
            try {
                Files.createDirectories(cacheDir);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            profileCache = new SQLiteCache(cacheDir.resolve("profiles.sqlite").toFile());
        } catch (IOException | UnsatisfiedLinkError ignored) {
            logger.warn("Failed to initialize SQLite profile cache. Cache is memory-only.");
            profileCache = new HashMapCache();
        }

        profileService = getPlatform().createProfileService(profileCache);

        getPlatform().load();
    }

    /**
     * The WorldGuard Platform.
     *
     * @return The platform
     */
    public CraftBookPlatform getPlatform() {
        checkNotNull(this.platform);
        return this.platform;
    }

    public void setPlatform(CraftBookPlatform platform) {
        checkNotNull(platform);
        this.platform = platform;
    }

    /**
     * Gets the CraftBook {@link Supervisor}.
     *
     * @return The supervisor
     */
    public Supervisor getSupervisor() {
        return this.supervisor;
    }

    /**
     * Get the global executor service for internal usage (please use your
     * own executor service).
     *
     * @return the global executor service
     */
    public ListeningExecutorService getExecutorService() {
        return executorService;
    }

    /**
     * Get the profile lookup service.
     *
     * @return the profile lookup service
     */
    public ProfileService getProfileService() {
        return this.profileService;
    }

    /**
     * Get the profile cache.
     *
     * @return the profile cache
     */
    public ProfileCache getProfileCache() {
        return this.profileCache;
    }

    /**
     * Gets the Translation Manager.
     *
     * @return The translation manager
     */
    public TranslationManager getTranslationManager() {
        return this.translationManager;
    }

}
