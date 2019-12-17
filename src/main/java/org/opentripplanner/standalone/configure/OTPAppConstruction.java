package org.opentripplanner.standalone.configure;

import org.opentripplanner.datastore.OtpDataStore;
import org.opentripplanner.datastore.configure.DataStoreFactory;
import org.opentripplanner.graph_builder.BuildStatusFile;
import org.opentripplanner.graph_builder.GraphBuilder;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.standalone.config.CommandLineParameters;
import org.opentripplanner.graph_builder.GraphBuilderDataSources;
import org.opentripplanner.standalone.server.GrizzlyServer;
import org.opentripplanner.standalone.server.OTPApplication;
import org.opentripplanner.standalone.server.OTPServer;
import org.opentripplanner.standalone.server.Router;
import org.opentripplanner.util.OTPFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Application;

/**
 * This class is responsible for creating the top level services like {@link OTPConfiguration}
 * and {@link OTPServer}. The purpose of this class is to wire the
 * application, creating the necessary Services and modules and putting them together.
 * It is NOT responsible for starting or running the application. The whole idea of this
 * class is to separate application construction from running it.
 *
 * <p> The top level construction class(this class) may delegate to other construction classes
 * to inject configuration and services into sub-modules.
 *
 * <p> THIS CLASS IS NOT THREAD SAFE - THE APPLICATION SHOULD BE CREATED IN ONE THREAD. This
 * should be really fast, since the only IO operations are reading config files and logging.
 * Loading transit or map data should NOT happen during this phase.
 */
public class OTPAppConstruction {

    private static final Logger LOG = LoggerFactory.getLogger(OTPAppConstruction.class);

    private final OTPConfiguration config;

    private OtpDataStore store = null;
    private OTPServer server = null;
    private GraphBuilderDataSources graphBuilderDataSources = null;

    /**
     * Create a new OTP configuration instance for a given directory.
     */
    public OTPAppConstruction(CommandLineParameters commandLineParameters) {
        this.config = new OTPConfiguration(commandLineParameters);
        initializeFeatures();
    }

    /**
     * Create or retrieve a data store witch provide access to files, remote or local.
     */
    public OtpDataStore store() {
        if(store == null) {
            this.store = new DataStoreFactory(config.createDataStoreConfig()).open();
        }
        return store;
    }

    public void startBuilderStatusFile() {
        BuildStatusFile.start(
            store().getOtpStatusDir(),
            config.buildConfig().storage.otpStatusFilename
        );
    }

    /**
     * Create a new Grizzly server - call this method once, the new instance is created
     * every time this method is called.
     */
    public GrizzlyServer createGrizzlyServer(Router router) {
        return new GrizzlyServer(config.getCli(), createApplication(router));
    }

    public void validateConfigAndDatasources() {
        // Load Graph Builder Data Sources to validate it.
        graphBuilderDataSources();
    }

    /**
     * Create the default graph builder.
     * @param baseGraph the base graph to add more data on to of.
     */
    public GraphBuilder createGraphBuilder(Graph baseGraph) {
        LOG.info("Wiring up and configuring graph builder task.");
        return GraphBuilder.create(
                config.buildConfig(),
                graphBuilderDataSources(),
                config.createEmbedConfig(),
                baseGraph
        );
    }

    /**
     * Return router configuration as loaded from the 'router-config.json' file.
     * <p>
     * The method is {@code public} to allow test access.
     */
    public OTPConfiguration config() {
        return config;
    }

    /**
     * Create the top-level objects that represent the OTP server. There is one server and it
     * is created lazy at the first invocation of this method.
     * <p>
     * The method is {@code public} to allow test access.
     */
    public OTPServer server(Router router) {
        if (server == null) {
            server = new OTPServer(config.getCli(), router);
        }
        return server;
    }

    private GraphBuilderDataSources graphBuilderDataSources() {
        if(graphBuilderDataSources == null) {
            graphBuilderDataSources = GraphBuilderDataSources.create(
                    config.getCli(),
                    config.buildConfig(),
                    store()
            );
        }
        return graphBuilderDataSources;
    }

    private void initializeFeatures() {
        // Initialize features from configuration file.
        OTPFeature.configure(config::isFeatureEnabled);
    }

    private Application createApplication(Router router) {
        return new OTPApplication(server(router), !config.getCli().insecure);
    }
}
