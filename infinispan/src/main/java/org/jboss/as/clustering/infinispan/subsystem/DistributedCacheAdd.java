package org.jboss.as.clustering.infinispan.subsystem;

import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.operations.common.Util;
import org.jboss.dmr.ModelNode;

import java.util.List;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;

/**
 * @author Richard Achmatowicz (c) 2011 Red Hat Inc.
 * @author Tristan Tarrant
 */
public class DistributedCacheAdd extends SharedStateCacheAdd {

    static final DistributedCacheAdd INSTANCE = new DistributedCacheAdd();

    // used to create subsystem description
    static ModelNode createOperation(ModelNode address, ModelNode model) throws OperationFailedException {
        ModelNode operation = Util.getEmptyOperation(ADD, address);
        INSTANCE.populate(model, operation);
        return operation;
    }

    private DistributedCacheAdd() {
        super(CacheMode.DIST_SYNC);
    }

    @Override
    void populate(ModelNode fromModel, ModelNode toModel) throws OperationFailedException {
        super.populate(fromModel, toModel);

        DistributedCacheResource.OWNERS.validateAndSet(fromModel, toModel);
        DistributedCacheResource.SEGMENTS.validateAndSet(fromModel, toModel);
        DistributedCacheResource.L1_LIFESPAN.validateAndSet(fromModel, toModel);
    }

    /**
     * Implementation of abstract method processModelNode suitable for distributed cache
     *
     * @param context
     * @param containerName
     * @param builder
     * @param dependencies
     * @return
     */
    @Override
    void processModelNode(OperationContext context, String containerName, ModelNode cache, ConfigurationBuilder builder, List<Dependency<?>> dependencies)
            throws OperationFailedException {

        // process the basic clustered configuration
        super.processModelNode(context, containerName, cache, builder, dependencies);

        final int owners = DistributedCacheResource.OWNERS.resolveModelAttribute(context, cache).asInt();
        final int segments = DistributedCacheResource.SEGMENTS.resolveModelAttribute(context, cache).asInt();
        final long lifespan = DistributedCacheResource.L1_LIFESPAN.resolveModelAttribute(context, cache).asLong();

        // process the additional distributed attributes and elements
        builder.clustering().hash()
            .numOwners(owners)
            .numSegments(segments)
        ;
        if (lifespan > 0) {
            // is disabled by default in L1ConfigurationBuilder
            builder.clustering().l1().enable().lifespan(lifespan);
        } else {
            builder.clustering().l1().disable();
        }
    }
}
