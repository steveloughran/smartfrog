package org.smartfrog.services.hadoop.bluemine.mr.testtools

import org.smartfrog.services.hadoop.bluemine.events.BlueEvent
import org.smartfrog.services.hadoop.bluemine.mr.EventCountReducer
import org.apache.hadoop.conf.Configuration
import org.smartfrog.services.hadoop.grumpy.GrumpyJob

import org.smartfrog.services.hadoop.grumpy.GrumpyHadoopTestBase

/**
 *
 */
class BluemineTestBase extends GrumpyHadoopTestBase {

    public static final String TEST_INPUT_DATA_DIR = "test.input.data.dir"
    public static final String TEST_OUTPUT_DATA_DIR = "test.output.data.dir"
    public static final String TEST_INPUT_DATA_FILE = "test.input.data.file"
    public static final String GATE1_50K = "gate1-50k.csv"

    /**
     * Add the small gate1 input set to a job as the input
     * @param job job to patch
     */
    void addTestDataset(GrumpyJob job) {
        String sourceFile = System.getProperty(TEST_INPUT_DATA_FILE, GATE1_50K);
        File file = getDataFile(sourceFile)
        addInput(job, file)
    }

    /**
     * Create an initial MR job 
     * @param testname name of the test (which defines the output directoyr too
     * @param mapClass class to use in map
     * @param reduceClass class to use in reduction
     * @return (job: GrumpyJob, output directory: file)
     */
    List createMRJob(String testname, Class mapClass, Class reduceClass) {
        Configuration conf = createJobConfiguration()
        GrumpyJob job = createTextKeyIntValueJob(testname,
                                                 conf,
                                                 mapClass,
                                                 reduceClass)
        addTestDataset(job)
        File outDir = addTestOutputDir(job, testname)
        return [job, outDir]
    }

    /**
     * Run an event job against the specified mapper, using BlueEvent events
     * as the output value of the map, and the EventCount reducer as the reducer
     * @param name job name
     * @param mapper mapper class
     * @return the output directory
     * 
     */
    File runEventCountJob(String name, Class mapper) {
        GrumpyJob job
        File outDir
        (job, outDir) = createMRJob(name,
                                    mapper,
                                    EventCountReducer.class)
        job.mapOutputValueClass = BlueEvent.class
        runJob(job)
        dumpDir(LOG, outDir)
        outDir
    }

}