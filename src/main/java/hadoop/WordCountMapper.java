package hadoop;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.SplitLocationInfo;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;
import java.util.StringTokenizer;

/**
 * How to log and debug the errors or malformed input data?
 * you can set job's status and also create a counter for malformed data.
 * You can easily see this information using Resource Manager's UI.
 * See pg 168 of the book.
 */

/*

Mapper syntax

public class Mapper<KEYIN, VALUEIN, KEYOUT, VALUEOUT> {

   protected void map(KEYIN key, VALUEIN value, Context context) throws IOException, InterruptedException {

     context.write((KEYOUT) key, (VALUEOUT) value);

   }
}

HashPartitioner syntax

public class HashPartitioner<K, V> extends Partitioner<K, V> {

    public int getPartition(K key, V value, int numReduceTasks) {

        return (key.hashCode() & Integer.MAX_VALUE) % numReduceTasks;

    }

}

The default partitioner is HashPartitioner, which hashes a record’s key to determine which partition the record belongs in.
Each partition is processed by a reduce task, so the number of partitions is equal to the number of reduce tasks for the job.


Very Important:
There are 3 main components involved to feed key-value pair to mapper.
- InputSplit
- InputFormat
- RecordReader
and
- Context that holds RecordReader

Read in detail from "YARN (MapReduce2).docx".
It is very important to understand difference between HDFS block and InputSplit.
Also understand Mapper's run() method. For more granular control over your Mapper class, you can override run() method.
 */

public class WordCountMapper
        extends Mapper<LongWritable, Text, // data types of input key-value
                Text, IntWritable> { // data types of output key-value

    private final static IntWritable one = new IntWritable(1);
    private Text word = new Text();

    // Every Mapper class overrides map method
    @Override
    public void map(LongWritable key, Text value, // data types of input key-value
                    Context context)
            throws IOException, InterruptedException {

        // You can get InputSplit information in this way

        /*InputSplit inputSplit = context.getInputSplit();
        long inputSplitLength = inputSplit.getLength();
        SplitLocationInfo[] locationInfo = inputSplit.getLocationInfo();
        for (SplitLocationInfo splitLocationInfo : locationInfo) {
            System.out.println(splitLocationInfo.isInMemory());
            System.out.println(splitLocationInfo.isOnDisk());
            System.out.println(splitLocationInfo.getLocation());
        }*/

        /*
        LongWritable currentKey = context.getCurrentKey(); // same as key provided by RecordReader
        Text currentValue = context.getCurrentValue(); // same as value provided by RecordReader
        */

        String line = value.toString();
        StringTokenizer itr = new StringTokenizer(line, ",");

        while (itr.hasMoreTokens()) {
            //just added the below line to convert everything to lower case
            word.set(itr.nextToken().toLowerCase());
            // the following check is that the word starts with an alphabet.
            if(Character.isAlphabetic((word.toString().charAt(0)))){
                context.write(word, one);
            }
        }
    }

}