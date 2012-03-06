package org.smartfrog.services.hadoop.bluemine.mr

import org.apache.hadoop.io.IntWritable
import org.apache.hadoop.io.LongWritable
import org.apache.hadoop.mapreduce.Mapper

class DeviceCountMap extends MapToDevice {
    IntWritable iw = new IntWritable(1)

    /**
     * When invoked , event is the current event, outputKey is set to the Text to write
     * @param key
     * @param context
     */
    void process(LongWritable key, Mapper.Context context) {
        context.write(outputKey, iw)
    }
}
