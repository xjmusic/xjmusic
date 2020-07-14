package io.xj.service.nexus.work;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.xj.lib.entity.Entity;
import io.xj.service.hub.client.HubClientAccess;
import io.xj.service.nexus.dao.ChainDAO;
import io.xj.service.nexus.entity.ChainState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 Janitor Worker reads the entity store for Chains that require work, and makes sure each has a ChainWorker going
 */
@FunctionalInterface
public interface JanitorWorker extends Runnable {
}

