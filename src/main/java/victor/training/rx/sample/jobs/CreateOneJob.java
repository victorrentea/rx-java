//package victor.training.rx.sample.jobs;
//
////Create Job (by Events)
////Controller
//
//import org.checkerframework.checker.nullness.qual.NonNull;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import rx.Completable;
//
//import javax.annotation.ParametersAreNonnullByDefault;
//
//class Controller {
//   /**
//    * Creates a new job.
//    */
//   @POST
//   @Consumes(MediaType.APPLICATION_JSON)
//   @Produces(MediaType.APPLICATION_JSON)
//   @ApiOperation(value = "Creates a new job", response = Job.class)
//   public void createJob(
//       final JobEventData jobCreationEvent,
//       @QueryParam("indexImmediately") final Boolean indexImmediatelyParam,
//       @PathParam(LOCATION_ID) final LocationId locationId,
//       @Context final HttpServletRequest request,
//       @Suspended final AsyncResponse asyncResponse) {
//      final Boolean indexImmediately = indexImmediatelyParam != null ? indexImmediatelyParam : false;
//      FluentRequest.async(request, asyncResponse, RESOURCE, "createJob")
//          .location(locationId)
//          .checkAnyTechnicalRoleOrEntitlement(Entitlement.OF_JOB_WRITE, CAN_WRITE)
//          .mandatory("creationEvent", jobCreationEvent)
//          .run(context -> {
//             if (!jobCreationEvent.isCreation()) {
//                return Single.just(Response.status(Response.Status.BAD_REQUEST));
//             }
//             final Optional<EmployeeId> employee = context.getLoggedInUser().getEmployeeId();
//             return jobService.addEvents(locationId, ImmutableList.of(jobCreationEvent), indexImmediately,
//                 context)
//                 .map(j -> Response.created(getJobUri(locationId, j.getId())).entity(j).build());
//          });
//   }
//
//   SERVICE
//
//   public Single<Job> addEvents(final LocationId locationId,
//                                final List<JobEventData> events,
//                                final RequestContext context,
//                                final boolean indexImmediately) {
//      final Optional<EmployeeId> employee = context.getLoggedInUser().getEmployeeId();
//
//      LOG.debug("Saving {} new events for job {} in {}", events.size(), jobId, locationId);
//
//      return saveEvents(locationId, JobId.generate(), events, employee)
//          .andThen(retrieveAndScheduleDeferredIndex(locationId, jobId, indexImmediately))
//          .map(job -> {
//             sendMetric(job, events, context);
//             return job;
//          });
//   }
//
//   private Completable saveEvents(final LocationId locationId, final JobId jobId,
//                                  final List<JobEventData> events, final Optional<EmployeeId> employee) {
//      return Observable.from(events)
//          .zipWith(IdUtil.generateSortedTimeUuids(events.size()), (data, uuid) ->
//              new JobEvent(jobId, locationId, uuid, employee, data))
//          .flatMap(event -> persistToJobFeed(locationId, jobId, event))
//          .flatMap(event -> dao.save(event).toObservable())
//          .subscribeOn(Schedulers.io())
//          .toCompletable();
//   }
//
//
//   @NotNull
//   private Observable<JobEvent> persistToJobFeed(final LocationId locationId, final JobId jobId, final JobEvent event) {
//      if (event.isCreation()) {
//         final String jobType = determineCreationJobType(event);
//         return dao.saveInJobFeed(locationId, jobId, jobType, JobStatus.PENDING)
//             .subscribeOn(Schedulers.io())
//             .andThen(Observable.just(event));
//      } else {
//         return retrieveJob(locationId, jobId)
//             .flatMapObservable(job -> {
//                final JobEventData jobEventData = event.getData();
//                final JobStatus status = jobEventData != null && jobEventData.getClass().equals(JobStatusChange.class)
//                    ? ((JobStatusChange) jobEventData).getStatus() : job.getStatus();
//
//                return dao.saveInJobFeed(locationId, jobId, job.getJobType(), status)
//                    .subscribeOn(Schedulers.io())
//                    .toSingleDefault(event)
//                    .toObservable();
//             });
//      }
//   }
//
//
////   Dao
//   RxCassandra c;
//   public Completable saveInJobFeed(final LocationId locationId, final JobId argJobId, final String jobType, final JobStatus jobStatus,
//                                    final Optional<LocalDate> localDateValueForTesting, final Optional<UUID> uuidValueForTesting) {
//      final LocalDate dateBucket = localDateValueForTesting.orElseGet(TimestampUtil::getCassandraLocalDateNow);
//      final UUID offset = uuidValueForTesting.orElseGet(UUIDUtil::newTimeUUID);
//      final Single<BoundStatement> bound = jobFeedCreationStatement.map(s -> s.bind()
//          .setString(COLUMN_LOCATION_ID, locationId.asString())
//          .setDate(COLUMN_DATE_BUCKET, dateBucket)
//          .setUUID(COLUMN_OFFSET, offset)
//          .setString(COLUMN_JOB_ID, argJobId.asString())
//          .setString(COLUMN_JOB_TYPE, jobType)
//          .setString(COLUMN_JOB_STATUS, jobStatus.name()));
//      return RxCassandra.executeWithoutResult(cassandraConnector, bound);
//      // statement.flatMap( s -> Single.defer( () -> toSingle( connector.executeAsync( s ) ) ) );
//      // this.session.get().executeAsync( statement );
//   }
//}
//
////CassConnector
//
//@ParametersAreNonnullByDefault
//public class CassandraConnector implements AutoCloseable {
//
//    private static final Logger LOG = LoggerFactory.getLogger(CassandraConnector.class);
//    private static final String CASSANDRA_CALL_TOOK_MSG = "Cassandra call took {} ms: {}";
//    private static final String CALL_DURATION_MDC_LABEL = "callDurationMs";
//
//    private static final String CASSANDRA_REQUEST_METRIC_PREFIX = "cassandra.request";
//    private static final String DURATION = "duration";
//    private static final String COUNTER = "counter";
//    private static final String METRIC_NAME_DELIMITER = ".";
//    private static final String CASSANDRA_REQUEST_DURATION_TIMER_NAME = CASSANDRA_REQUEST_METRIC_PREFIX + METRIC_NAME_DELIMITER + DURATION;
//    private static final String CASSANDRA_REQUEST_COUNTER_NAME = CASSANDRA_REQUEST_METRIC_PREFIX + METRIC_NAME_DELIMITER + COUNTER;
//
//    private final SingletonProvider<DseSession> session;
//    private final MetricRegistryWithTagging metricRegistry;
//    private final Tracer tracer;
//    private final CassandraConnectorLogConfiguration logConfiguration;
//
//   /**
//     * Asynchronously executes the given Statement.
//     *
//     * @param statement
//     *            The Statement to be executed asynchronously.
//     * @return The ResultSetFuture or null.
//     */
//    @NonNull
//    public ResultSetFuture executeAsync( final Statement statement ) {
//        final ResultSetFuture result = this.session.get().executeAsync( statement );
//        try {
//            return result;
//        } finally {
//            measureAsyncStatement( statement, result );
//        }
//    }
//
//    private void measureAsyncStatement( final Object statement, final ResultSetFuture result ) {
//        final long start = System.currentTimeMillis();
//        // Copy diagnostic context map and pass it to the handler to get the correct context, since the callback will probably not be executed on this thread, and not immediately.
//        final Map<String, String> diagnosticContext = MDC.getCopyOfContextMap();
//        final FutureCallback<ResultSet> handler = new FutureCallback<ResultSet>() {
//            @Override
//            public void onSuccess( @Nullable final ResultSet value ) {
//                if ( diagnosticContext != null ) {
//                    MDC.setContextMap( diagnosticContext );
//                }
//                measureStatement( statement, start );
//            }
//
//            @Override
//            public void onFailure( final Throwable error ) {
//                if ( diagnosticContext != null ) {
//                    MDC.setContextMap( diagnosticContext );
//                }
//                measureStatement( statement, start );
//            }
//        };
//        Futures.addCallback( result, handler );
//    }
//}
//
//
////Datastax SessionManager
//class Datastax {
//   @Override
//   public ResultSetFuture executeAsync(final Statement statement) {
//      if (isInit) {
//         DefaultResultSetFuture future = new DefaultResultSetFuture(this, cluster.manager.protocolVersion(), makeRequestMessage(statement, null));
//         new RequestHandler(this, future, statement).sendRequest();
//         return future;
//      } else {
//         // If the session is not initialized, we can't call makeRequestMessage() synchronously, because it
//         // requires internal Cluster state that might not be initialized yet (like the protocol version).
//         // Because of the way the future is built, we need another 'proxy' future that we can return now.
//         final ChainedResultSetFuture chainedFuture = new ChainedResultSetFuture();
//         this.initAsync().addListener(new Runnable() {
//            @Override
//            public void run() {
//               DefaultResultSetFuture actualFuture = new DefaultResultSetFuture(SessionManager.this, cluster.manager.protocolVersion(), makeRequestMessage(statement, null));
//               execute(actualFuture, statement);
//               chainedFuture.setSource(actualFuture);
//            }
//         }, executor());
//         return chainedFuture;
//      }
//   }
//}